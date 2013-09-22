package com.smasher.andora;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.widget.Toast;

import com.smasher.andora.libpandora.PandoraException;
import com.smasher.andora.libpandora.PandoraSession;
import com.smasher.andora.libpandora.Partner;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setContentView(R.layout.activity_main);

        if (getIntent().getBooleanExtra("loggedIn", false)) {
            postLogin();
        } else {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

            UserLoginTask login = new UserLoginTask(
                    settings.getString("email", null),
                    settings.getString("password", null),
                    settings.getString("salt", null),
                    settings.getBoolean("premium", false));
            login.execute();
        }
    }

    protected void postLogin() {
        //do stuff
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        String email, password;
        boolean premium;

        String message;
        PandoraException.Error error;

        UserLoginTask(String email, String password, String salt, boolean premium) {
            this.email = email;
            if (password!=null && salt!=null)
                this.password = LoginActivity.decrypt(LoginActivity.generateKey(), Base64.decode(salt, Base64.DEFAULT), password);
            else
                this.password = null;
            this.premium = premium;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
                return false;

            try {
                PandoraSession pandora = new PandoraSession();
                pandora.partnerLogin(premium ? Partner.PARTNER_PANDORA_ONE : Partner.PARTNER_ANDROID);
                pandora.userLogin(email, password);
                return true;
            } catch (PandoraException e) {
                error = e.getError();
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                postLogin();
            } else {
                if (error != null)
                    Toast.makeText(MainActivity.this, error.getMessage(MainActivity.this), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        }
    }
}
