package com.smasher.andora.libpandora;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is a java implementation of the Pandora API.<br>
 * See <a href="http://pan-do-ra-api.wikia.com/wiki/Json/5">pan-do-ra-api.wikia.com</a>
 * <p>
 * This class simply handles communicating with Pandora.
 * More code is required to keep track of everything and provide a usable experience.
 * <p>
 * Note: This class was built for Android but could easily be adapted.
 * You just need to use standard Java logging and HTTP connections.
 *
 * @author Rowan Decker
 */
public class PandoraSession {
    public static final boolean DEBUG_JSON = true;

    public static final String path = "/services/json/";
    public static final String cipher = "Blowfish/ECB/PKCS5Padding";
    public static final String charSet = "UTF-8";

    private Partner partner;

    private long syncTime;
    private int partnerId, userId;
    private String partnerToken, userToken;

    private void reset() {
        // We must clear the tokens so that they are not sent with the next login attempt
        partner = null;
        syncTime = 0;
        partnerId = 0;
        userId = 0;
        partnerToken = null;
        userToken = null;
    }

    public boolean login(String email, String password, boolean premium) throws PandoraException {
        reset();
        int id = premium?Partner.PARTNER_PANDORA_ONE:Partner.PARTNER_ANDROID;
        if (partner==null || partner.getId()!=id) {
            if (!partnerLogin(id))
                return false;
        }

        return userLogin(email, password);
    }

    public boolean partnerLogin(int id) throws PandoraException {
        try {
            partner = new Partner(id);
            JSONObject json = new JSONObject();
            json.put("username", partner.getUsername());
            json.put("password", partner.getPassword());
            json.put("deviceModel", partner.getDevice());
            json.put("version", partner.getVersion());
            json.put("includeUrls", true);
            JSONObject result = sendRequest("auth.partnerLogin", json, true, false);
            if (result != null) {
                partnerId = result.getInt("partnerId");
                partnerToken = result.getString("partnerAuthToken");
                syncTime = Long.valueOf(decrypt(result.getString("syncTime"))) - System.currentTimeMillis();
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        log("Partner login failed.");
        return false;
    }

    public boolean userLogin(String email, String password) throws PandoraException {
        try {
            JSONObject json = new JSONObject();
            json.put("loginType", "user");
            json.put("username", email);
            json.put("password", password);
            JSONObject result = sendRequest("auth.userLogin", json, true, true);
            if (result != null) {
                userId = result.getInt("userId");
                userToken = result.getString("userAuthToken");
                log("User login successful.");
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        log("User login failed.");
        return false;
    }

    public boolean isPremium() {
        try {
            JSONObject result = sendRequest("user.canSubscribe", null, true, true);
            if (result != null) {
                return result.getBoolean("isSubscriber");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createUser(String email, String password, int year, String zip, String gender, boolean opt) throws PandoraException {
        try {
            JSONObject json = new JSONObject();
            json.put("accountType", "registered");
            json.put("registeredType", "user");
            json.put("countryCode", "US");
            json.put("username", email);
            json.put("password", password);
            json.put("birthYear", year);
            json.put("zipCode", zip);
            json.put("gender", gender);
            json.put("emailOptIn", opt);
            JSONObject result = sendRequest("user.createUser", json, true, true);
            if (result != null) {
                userId = result.getInt("userId");
                userToken = result.getString("userAuthToken");
                log("Account creation successful.");
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        log("Account creation failed.");
        return false;
    }

    public boolean emailPassword(String email) throws PandoraException {
        try {
            JSONObject json = new JSONObject();
            json.put("username", email);
            sendRequest("user.emailPassword", json, true, true);
            log("Password emailed.");
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    //////////

    JSONObject sendRequest(String method, JSONObject data, boolean tls, boolean enc) throws PandoraException {
        JSONObject result;

        try {
            if (partner==null) {
                log("Error: Must call parter login first");
                return null;
            }
            String url = (tls?"https://":"http://") + partner.getHost() + path + "?method="+method;
            if (userToken != null) url += "&auth_token="+urlEncode(userToken);
            else if (partnerToken != null) url += "&auth_token="+urlEncode(partnerToken);
            if (userId != 0) url += "&user_id="+userId;
            if (partnerId != 0) url += "&partner_id="+partnerId;

            if (data==null) {
                data = new JSONObject();
            }

            if (userToken != null) data.put("userAuthToken", userToken);
            if (partnerToken != null) data.put("partnerAuthToken", partnerToken);
            if (syncTime != 0) data.put("syncTime", syncTime());

            log("##### HTTP #####");
            log("URL: " + url);
            if (DEBUG_JSON)
                log("Send: " + data.toString(2));

            String str = enc ? encrypt(data.toString()) : data.toString();
            log("Encrypted = "+str);

            String ret = post(url, str);
            if (ret == null) {
                log("Error: No response");
                return null;
            }

            JSONObject json = new JSONObject(ret);
            if (DEBUG_JSON)
                log("Receive: " + json.toString(2));

            if ("ok".equals(json.getString("stat"))) {
                result = json.getJSONObject("result");
                return result;
            } else {
                int err = json.getInt("code");
                throw new PandoraException(err);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String post(String url, String data) {
        HttpURLConnection http = null;

        try {
            int len = data.length();
            System.setProperty("http.keepAlive", "false");
            http = (HttpURLConnection) new URL(url).openConnection();
            http.setRequestProperty("Content-length", String.valueOf(len));
            http.setRequestProperty("Content-Type", "text/plain");
            http.setRequestProperty("Accept", "*/*");
            http.setRequestProperty("Connection", "close");

            http.setFixedLengthStreamingMode(len);
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setDoInput(true);
            http.connect();

            DataOutputStream out = new DataOutputStream(http.getOutputStream());
            out.writeBytes(data);
            out.flush(); out.close();

            String line, ret="";
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream(), charSet));
            while ((line = reader.readLine()) != null) {
                ret += line + "\n";
            }

            return ret;
        } catch (Exception e) {
            log("!!!!! HTTP EXCEPTION !!!!!");
            log("Error: " + e.toString());
            e.printStackTrace();
        } finally {
            if (http != null)
                http.disconnect();
        }

        return null;
    }

    long syncTime() {
        return syncTime + System.currentTimeMillis();
    }

    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String input) {
        byte[] result;
        try {
            Cipher decryptionCipher = Cipher.getInstance(cipher);
            SecretKeySpec key = new SecretKeySpec(partner.getDecryption().getBytes(), "Blowfish");
            decryptionCipher.init(Cipher.DECRYPT_MODE, key);
            result = decryptionCipher.doFinal(hexToBytes(input));

            byte[] chopped = new byte[result.length - 4];
            System.arraycopy(result, 4, chopped, 0, chopped.length);
            return new String(chopped);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encrypt(String input) {
        try {
            Cipher encryptionCipher = Cipher.getInstance(cipher);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(partner.getEncryption().getBytes(), "Blowfish"));
            byte[] bytes = encryptionCipher.doFinal(input.getBytes());
            return bytesToHex(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i=0; i<len; i+=2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    | Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int i=0; i<bytes.length; i++) {
            v = bytes[i] & 0xFF;
            hexChars[i*2] = hexArray[v >>> 4];
            hexChars[i*2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static void log(String msg) {
        Log.d("LibPandora", "/PandoraSession: " + msg);
    }
}
