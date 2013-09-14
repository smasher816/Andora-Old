package com.smasher.andora.libpandora;

/**
 * You must first authenticate as an valid partner before you can authenticate a user.
 * This class just provides the necessary information about a selected partner.
 * <p>
 * See <a href="http://pan-do-ra-api.wikia.com/wiki/Json/5/partners">Partners List</a>.
 * <p>
 * <strong>NOTE: If Pandora changes their keys it must be reflected here.</strong>
 */

public class Partner {
    public static final int PARTNER_ANDROID = 1;
    public static final int PARTNER_PANDORA_ONE = 2;
    public static final int PARTNER_IOS = 3;
    public static final int PARTNER_PALM = 4;
    public static final int PARTNER_WINDOWS_MOBILE = 5;
    public static final int PARTNER_VISTA_WIDGET = 6;

    private int id;
    private String host;
    private String username;
    private String password;
    private String device;
    private String version;
    private String key_decrypt;
    private String key_encrypt;

    Partner(int id) {
        setPartner(id);
    }

    public void setPartner(int id) {
        this.id = id;
        setHost("tuner.pandora.com");
        setVersion("5");

        switch (id) {
            case PARTNER_ANDROID:
                setUsername("android");
                setPassword("AC7IBG09A3DTSYM4R41UJWL07VLN8JI7");
                setDevice("android-generic");
                setDecrypt("R=U!LH$O2B#");
                setEncrypt("6#26FRL$ZWD");
                break;
            case PARTNER_PANDORA_ONE:
                setHost("internal-tuner.pandora.com");
                setUsername("pandora one");
                setPassword("TVCKIBGS9AO9TSYLNNFUML0743LH82D");
                setDevice("D01");
                setDecrypt("U#IO$RZPAB%VX2");
                setEncrypt("2%3WCL*JU$MP]4");
                break;
            case PARTNER_IOS:
                setUsername("iphone");
                setPassword("P2E4FC0EAD3*878N92B2CDp34I0B1@388137C");
                setDevice("IP01");
                setDecrypt("20zE1E47BE57$51");
                setEncrypt("721^26xE22776");
                break;
            case PARTNER_PALM:
                setUsername("palm");
                setPassword("IUC7IBG09A3JTSYM4N11UJWL07VLH8JP0");
                setDevice("pre");
                setDecrypt("E#U$MY$O2B=");
                setEncrypt("%526CBL$ZU3");
                break;
            case PARTNER_WINDOWS_MOBILE:
                setUsername("winmo");
                setPassword("ED227E10a628EB0E8Pm825Dw7114AC39");
                setDevice("VERIZON_MOTOQ9C");
                setDecrypt("7D671jt0C5E5d251");
                setEncrypt("v93C8C2s12E0EBD");
                break;
            case PARTNER_VISTA_WIDGET:
                setUsername("windowsgadget");
                setPassword("EVCCIBGS9AOJTSYMNNFUML07VLH8JYP0");
                setDevice("WG01");
                setDecrypt("E#IO$MYZOAB%FVR2");
                setEncrypt("%22CML*ZU$8YXP[1");
                break;
            default:
                //unknown partner
                throw new IndexOutOfBoundsException("setPartner("+id+")");
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDecrypt(String key) {
        this.key_decrypt = key;
    }

    public void setEncrypt(String key) {
        this.key_encrypt = key;
    }

    public int getId() { return id; }
    public String getHost() { return host; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDevice() { return device; }
    public String getVersion() { return version; }
    public String getDecryption() { return key_decrypt; }
    public String getEncryption() { return key_encrypt; }
}