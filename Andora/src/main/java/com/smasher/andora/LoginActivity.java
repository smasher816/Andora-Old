package com.smasher.andora;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smasher.andora.libpandora.PandoraException;
import com.smasher.andora.libpandora.PandoraSession;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.smasher.andora.libpandora.PandoraException.Error.BIRTH_YEAR_INVALID;
import static com.smasher.andora.libpandora.PandoraException.Error.BIRTH_YEAR_TOO_YOUNG;
import static com.smasher.andora.libpandora.PandoraException.Error.INVALID_PASSWORD;
import static com.smasher.andora.libpandora.PandoraException.Error.INVALID_USERNAME;
import static com.smasher.andora.libpandora.PandoraException.Error.UNKNOWN_ERROR;
import static com.smasher.andora.libpandora.PandoraException.Error.ZIP_CODE_INVALID;

/**
 * Log into Pandora
 */
public class LoginActivity extends Activity {
    // Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask mAuthTask = null;

    EditText mEmailView;
    EditText mPasswordView;
    EditText mYearView;
    EditText mZipView;
    private Spinner mGenderView;
    private CheckBox mCheckView;

    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    private enum LoginType { SIGN_IN, CREATE_ACCOUNT, FORGOT_PASSWORD }
    LoginType loginMode = LoginType.SIGN_IN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(android.R.style.Theme_Holo);
        //setTheme(android.R.style.Theme_Holo_Light);
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mYearView = (EditText) findViewById(R.id.year);
        mZipView = (EditText) findViewById(R.id.zip);

        mGenderView = (Spinner) findViewById(R.id.gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_dropdown_item);
        mGenderView.setAdapter(adapter);

        mCheckView = (CheckBox) findViewById(R.id.optIn);
        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_sign_in:
                showScreen(LoginType.SIGN_IN);
                return true;
            case R.id.action_forgot_password:
                showScreen(LoginType.FORGOT_PASSWORD);
                return true;
            case R.id.action_create_account:
                showScreen(LoginType.CREATE_ACCOUNT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mYearView.setError(null);
        mZipView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String year = mYearView.getText().toString();
        String zip = mZipView.getText().toString();
        String gender = (mGenderView.getSelectedItemPosition()==0) ? "male" : "female";
        boolean opt = mCheckView.isChecked();

        //View focusView = null;
        boolean cancel = false;
        int iYear = 0;

        if (loginMode == LoginType.CREATE_ACCOUNT) {
            // Check for a valid zip code.
            if (!zip.matches("^\\d{5}([\\-]?\\d{4})?$")) {
                cancel = handleError(ZIP_CODE_INVALID);
            }

            // Check for a valid birth year.
            if (!year.matches("^\\d{4}")) {
                cancel = handleError(BIRTH_YEAR_INVALID);
            } else {
                try {
                    iYear = Integer.parseInt(year);
                    Time time = new Time();
                    time.setToNow();

                    if (iYear<1900)
                        cancel = handleError(BIRTH_YEAR_INVALID);
                    else if (iYear>time.year-13)
                        cancel = handleError(BIRTH_YEAR_TOO_YOUNG);
                } catch (NumberFormatException e) {
                    cancel = handleError(BIRTH_YEAR_INVALID);
                }
            }
        }

        if (loginMode != LoginType.FORGOT_PASSWORD) {
            // Check for a valid password.
            if (password.length() < 5) {
                cancel = handleError(INVALID_PASSWORD);
            }
        }

        // Check for a valid email address.
        if (!email.contains("@") || !email.contains(".") || email.contains(" ")) {
            cancel = handleError(INVALID_USERNAME);
        }

        if (!cancel) {
            // Show a progress spinner, and kick off a background task
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask(loginMode, email, password, iYear, zip, gender, opt);
            mAuthTask.execute();
        }
    }

    private void showScreen(final LoginType type) {
        loginMode = type;

        String str;
        boolean showPassword = false;
        boolean showCreation = false;

        switch (type) {
            case FORGOT_PASSWORD:
                str = getString(R.string.action_forgot_password);
                showPassword = true;
                break;
            case CREATE_ACCOUNT:
                str = getString(R.string.action_create_account);
                showCreation = true;
                break;
            default:
                str = getString(R.string.action_sign_in);
        }

        mPasswordView.setVisibility(showPassword ? View.GONE : View.VISIBLE);
        mYearView.setVisibility(showCreation ? View.VISIBLE : View.GONE);
        mZipView.setVisibility(showCreation ? View.VISIBLE : View.GONE);
        mGenderView.setVisibility(showCreation ? View.VISIBLE : View.GONE);
        mCheckView.setVisibility(showCreation ? View.VISIBLE : View.GONE);
        ((Button)findViewById(R.id.sign_in_button)).setText(str);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    protected boolean handleError(PandoraException.Error error) {
        switch (error) {
            case INVALID_LOGIN:
                Toast.makeText(LoginActivity.this, getString(R.string.error_login_failure), Toast.LENGTH_LONG).show();
                return true;
            case INVALID_USERNAME:
                mEmailView.setError(getString(R.string.error_invalid_username));
                mEmailView.requestFocus();
                return true;
            case INVALID_PASSWORD:
                mPasswordView.setError(getString(R.string.error_invalid_password));
                mPasswordView.requestFocus();
                return true;
            case ZIP_CODE_INVALID:
                mZipView.setError(getString(R.string.error_zip_code_invalid));
                mZipView.requestFocus();
                return true;
            case BIRTH_YEAR_INVALID:
                mYearView.setError(getString(R.string.error_birth_year_invalid));
                mYearView.requestFocus();
                return true;
            case BIRTH_YEAR_TOO_YOUNG:
                mYearView.setError(getString(R.string.error_birth_year_too_young));
                mYearView.requestFocus();
                return true;
            case USERNAME_ALREADY_EXISTS:
                mEmailView.setError(getString(R.string.error_username_already_exists));
                mEmailView.requestFocus();
                return true;
            default:
                ErrorActivity.errorPopup(this, error);
                return true;
        }
    }

    public static SecretKey generateKey() {
        //this method has been purposely omitted to keep the release key (more) private
        //if you are building this app on your own, you must generate and return your own key
    }

    public static byte[] generateIV() {
        //generate a random salt to protect against rainbow tables

        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    public static String encrypt(SecretKey key, byte iv[], String data) {
        try {
            Cipher encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getEncoded(), "AES"), new IvParameterSpec(iv));
            byte[] bytes = encryptionCipher.doFinal(data.getBytes());
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(SecretKey key, byte iv[], String data) {
        try {
            Cipher encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptionCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getEncoded(), "AES"), new IvParameterSpec(iv));
            byte[] bytes = encryptionCipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        PandoraSession pandora;
        LoginType mode;
        String email, password, zip, gender;
        int year;
        boolean opt;

        boolean premium;
        PandoraException.Error error;

        UserLoginTask(LoginType mode, String email, String password, int year, String zip, String gender, boolean opt) {
            pandora = new PandoraSession();
            this.mode = mode;
            this.email = email;
            this.password = password;
            this.year = year;
            this.zip = zip;
            this.gender = gender;
            this.opt = opt;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                throw new PandoraException(UNKNOWN_ERROR);
                /*pandora.partnerLogin(Partner.PARTNER_ANDROID);

                switch (mode) {
                    case SIGN_IN: {
                        pandora.userLogin(email, password);
                        premium = pandora.isPremium();
                        if (premium) {
                            Log.d("Andora", "/Login: " + "User supports Pandora One. Upgrading connection.");
                            pandora.partnerLogin(Partner.PARTNER_PANDORA_ONE);
                            pandora.userLogin(email, password);
                            message = "Thank you for using Pandora One!";
                        } else {
                            message = "Please consider upgrading to Pandora One.";
                        }

                        /* Store the account credentials in the apps private preferences.
                           However, anyone with root access can read this file making it somewhat insecure.
                           Therefore it is stored encrypted to add another layer of security.
                           Note: Someone with enough motivation and skill could still determine the key.
                         *//*
                        byte salt[] = generateIV();
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit()
                        .putString("email", email)
                        .putString("password", encrypt(generateKey(), salt, password))
                        .putString("salt", Base64.encodeToString(salt, Base64.DEFAULT))
                        .putBoolean("premium", premium)
                        .commit();

                        return true;
                    }
                    case FORGOT_PASSWORD: {
                        pandora.emailPassword(email);
                        message = "Password emailed to " + email;
                        return false;
                    }
                    case CREATE_ACCOUNT: {
                        pandora.createUser(email, password, year, zip, gender, opt);
                        message = "Welcome to Andora";
                        return true;
                    }
                }*/
            } catch (PandoraException e) {
                error = e.getError();
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("loggedIn", true);
                startActivity(intent);
                LoginActivity.this.finish(); //kill the login page
            } else {
                handleError(error);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
