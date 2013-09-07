package com.smasher.andora.libpandora;

/**
 * Pandora sent an error message.
 * You better find a way to catch and handle it.
 * <p>
 * See <a href="http://pan-do-ra-api.wikia.com/wiki/Json/5#Error_codes">Error Codes</a>.
 */

public class PandoraException extends Exception {
    public static final int ERROR_LOGIN = 1002;

    private int code = -1;

    public PandoraException(int code) {
        super(getMessage(code));
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static String getMessage(int code) {
        String message;
        switch (code) {
            case 0:
                message = "Internal Error. Pandora may have temporarily blocked your account";
                break;
            case 1:
                message = "Pandora is in \"Maintenance Mode\". Try again later.";
                break;
            case 2:
                message = "URL_PARAM_MISSING_METHOD";
                break;
            case 3:
                message = "URL_PARAM_MISSING_AUTH_TOKEN";
                break;
            case 4:
                message = "URL_PARAM_MISSING_PARTNER_ID";
                break;
            case 5:
                message = "URL_PARAM_MISSING_USER_ID";
                break;
            case 6:
                message = "SECURE_PROTOCOL_REQUIRED";
                break;
            case 7:
                message = "CERTIFICATE_REQUIRED";
                break;
            case 8:
                message = "PARAMETER_TYPE_MISMATCH";
                break;
            case 9:
                message = "PARAMETER_MISSING";
                break;
            case 10:
                message = "PARAMETER_VALUE_INVALID";
                break;
            case 11:
                message = "The current API version is no longer supported. The app must be updated :(";
                break;
            case 12:
                message = "Licensing Restriction. Pandora may not be available in your country.";
                break;
            case 13:
                message = "Insufficient Connectivity. Make sure your clock is correct.";
                break;
            case 14:
                message = "UNKNOWN_METHOD_NAME?";
                break;
            case 15:
                message = "WRONG_PROTOCOL?";
                break;
            case 1000:
                message = "Pandora is in \"Read Only\" Mode. Please try again later.";
                break;
            case 1001:
                message = "Invalid or Expired Auth Token.";
                break;
            case ERROR_LOGIN:
                message = "Invalid email or password";
                break;
            case 1003: //LISTENER_NOT_AUTHORIZED
                message = "You do not have a Pandora One account.";
                break;
            case 1004:
                message = "USER_NOT_AUTHORIZED";
                break;
            case 1005:
                message = "Max number of stations reached.";
                break;
            case 1006:
                message = "STATION_DOES_NOT_EXIST";
                break;
            case 1007:
                message = "COMPLIMENTARY_PERIOD_ALREADY_IN_USE";
                break;
            case 1008:
                message = "CALL_NOT_ALLOWED";
                break;
            case 1009:
                message = "DEVICE_NOT_FOUND";
                break;
            case 1010:
                message = "Partner Not Authorized. The app may need to be updated.";
                break;
            case 1011:
                message = "Invalid Username";
                break;
            case 1012:
                message = "Invalid Password";
                break;
            case 1013:
                message = "Username Already Exists";
                break;
            case 1014:
                message = "DEVICE_ALREADY_ASSOCIATED_TO_ACCOUNT";
                break;
            case 1015:
                message = "UPGRADE_DEVICE_MODEL_INVALID";
                break;
            case 1018:
                message = "EXPLICIT_PIN_INCORRECT";
                break;
            case 1020:
                message = "EXPLICIT_PIN_MALFORMED";
                break;
            case 1023:
                message = "DEVICE_MODEL_INVALID";
                break;
            case 1024:
                message = "Zip Code Invalid";
                break;
            case 1025:
                message = "Birth Year Invalid";
                break;
            case 1026:
                message = "Birth Year Too Young";
                break;
            case 1027:
                message = "Invalid Country Code/Gender?";
                break;
            case 1034:
                message = "DEVICE_DISABLED";
                break;
            case 1035:
                message = "Daily Trial Limit Reached";
                break;
            case 1036:
                message = "INVALID_SPONSOR";
                break;
            case 1037:
                message = "User Already Used Trial";
                break;
            default:
                message = "Unknown Error! code="+code;
        }
        return message;
    }
}
