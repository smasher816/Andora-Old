package com.smasher.andora.libpandora;

import android.content.Context;

import com.smasher.andora.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Pandora sent an error message.
 * You better find a way to catch and handle it.
 * <p>
 * See <a href="http://pan-do-ra-api.wikia.com/wiki/Json/5#Error_codes">Error Codes</a>.
 */

public class PandoraException extends Exception {
    public enum Error {
        UNKNOWN_ERROR(-1, R.string.error_unknown),

        //protocol issues
        INTERNAL(0, R.string.error_internal),
        MAINTENANCE_MODE(1, R.string.error_maintenance_mode),
        URL_PARAM_MISSING_METHOD(2, R.string.error_api_changed),
        URL_PARAM_MISSING_AUTH_TOKEN(3, R.string.error_api_changed),
        URL_PARAM_MISSING_PARTNER_ID(4, R.string.error_api_changed),
        URL_PARAM_MISSING_USER_ID(5, R.string.error_api_changed),
        SECURE_PROTOCOL_REQUIRED(6, R.string.error_api_changed),
        CERTIFICATE_REQUIRED(7, R.string.error_api_changed),
        PARAMETER_TYPE_MISMATCH(8, R.string.error_api_changed),
        PARAMETER_MISSING(9, R.string.error_api_changed),
        PARAMETER_VALUE_INVALID(10, R.string.error_api_changed),
        API_VERSION_NOT_SUPPORTED(11, R.string.error_api_changed),
        LICENSING_RESTRICTIONS(12, R.string.error_licensing_restrictions),
        INSUFFICIENT_CONNECTIVITY(13, R.string.error_insufficient_connectivity),
        UNKNOWN_METHOD(14, R.string.error_api_changed),
        WRONG_PROTOCOL(15, R.string.error_api_changed),

        //input error
        READ_ONLY_MODE(1000, R.string.error_read_only_mode),
        INVALID_AUTH_TOKEN(1001, R.string.error_invalid_auth_token),
        INVALID_LOGIN(1002, R.string.error_invalid_partner_login),
        LISTENER_NOT_AUTHORIZED(1003, R.string.error_listener_not_authorized),
        USER_NOT_AUTHORIZED(1004, R.string.error_user_not_authorized),
        MAX_STATIONS_REACHED(1005, R.string.error_max_stations_reached),
        STATION_DOES_NOT_EXIST(1006, R.string.error_station_does_not_exist),
        COMPLIMENTARY_PERIOD_ALREADY_IN_USE(1007, R.string.error_complimentary_period_already_in_use),
        CALL_NOT_ALLOWED(1008, R.string.error_call_not_allowed),
        DEVICE_NOT_FOUND(1009, R.string.error_device_not_found),
        PARTNER_NOT_AUTHORIZED(1010, R.string.error_partner_not_authorized),
        INVALID_USERNAME(1011, R.string.error_invalid_username),
        INVALID_PASSWORD(1012, R.string.error_invalid_password),
        USERNAME_ALREADY_EXISTS(1013, R.string.error_username_already_exists),
        DEVICE_ALREADY_ASSOCIATED_TO_ACCOUNT(1014, R.string.error_device_already_associated_to_account),
        UPGRADE_DEVICE_MODEL_INVALID(1015, R.string.error_upgrade_device_model_invalid),
        EXPLICIT_PIN_INCORRECT(1018, R.string.error_explicit_pin_incorrect),
        EXPLICIT_PIN_MALFORMED(1020, R.string.error_explicit_pin_malformed),
        DEVICE_MODEL_INVALID(1023, R.string.error_device_model_invalid),
        ZIP_CODE_INVALID(1024, R.string.error_zip_code_invalid),
        BIRTH_YEAR_INVALID(1025, R.string.error_birth_year_invalid),
        BIRTH_YEAR_TOO_YOUNG(1026, R.string.error_birth_year_too_young),
        INVALID_COUNTRY_GENDER(1027, R.string.error_invalid_country_gender),
        USER_NOT_REGISTERED(1029, R.string.error_user_not_registered), //not documented
        DEVICE_DISABLED(1034, R.string.error_device_disabled),
        DAILY_TRIAL_LIMIT_REACHED(1035, R.string.error_daily_trial_limit_reached),
        INVALID_SPONSOR(1036, R.string.error_invalid_sponsor),
        USER_ALREADY_USED_TRIAL(1037, R.string.error_user_already_used_trial),

        //custom error
        NO_CONNECTION(2001, R.string.error_user_already_used_trial);

        private int code;
        private int resId;
        private static Map<Integer, Error> map;

        private Error(int code, int resId) {
            this.code = code;
            this.resId = resId;
        }

        public  static Error getEnum(int code) {
            if (map == null) {
                map = new HashMap<Integer, Error>();
                for (Error s : values()) {
                    map.put(s.code, s);
                }
            }

            Error error =  map.get(code);
            if (error == null)
                error = Error.UNKNOWN_ERROR;
            return error;
        }

        public int getCode() {
            return code;
        }

        public String getMessage(Context context) {
            return context.getString(resId);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            return builder.append("Error ").append(code).append(": ").append(name()).toString();
        }
    };

    private Error error;

    public PandoraException(Error error) {
        super(error.name());
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
