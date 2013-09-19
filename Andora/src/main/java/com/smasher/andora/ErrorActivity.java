package com.smasher.andora;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.smasher.andora.libpandora.PandoraException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ErrorActivity extends Activity implements View.OnClickListener {
    String file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        Intent intent = getIntent();
        file = intent.getStringExtra("file");

        String msg = intent.getStringExtra("exception") + ": " + intent.getStringExtra("message");

        String error = intent.getStringExtra("error");
        if (!TextUtils.isEmpty(error))
            msg += "\nError code: " + error;

        String info = intent.getStringExtra("info");
        if (!TextUtils.isEmpty(info))
            msg += "\n\n" + info;

        ((TextView)findViewById(R.id.crash_msg)).setText(msg);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.crash_report:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Smasher816@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Andora Crash");
                intent.putExtra(Intent.EXTRA_TEXT, "Please describe what you were doing: ");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file));
                startActivity(intent);
                break;
        }
    }
}

class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Context context;

    CrashHandler(Context context) {
        this.context = context;
    }

    private String getInfo(Throwable exception) {
        /* TODO: Provide some exception specific messages */

        return "Congratulations! Through some series of events you have successfully found an issue " +
        "I could not have expected. Please consider clicking the button below to email me this problem. " +
        "The more detail you provide the quicker I can fix this issue and make Andora an even better app.\n";
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        String file = context.getExternalCacheDir()+"/andora_crash_log.txt";
        Time time = new Time();
        time.setToNow();

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            out.append(" ----- CRASH " + time.format("%m/%d/%y %H:%M:%S") + " ----- \n" + Log.getStackTraceString(exception) + "\n");
            out.close();
        } catch (Exception e) {

        }

        //I want to know the cause if it is available
        Throwable cause = exception.getCause();
        if (cause != null) exception = cause;

        intent.putExtra("exception", exception.getClass().getName());
        intent.putExtra("message", exception.getMessage());
        if (exception instanceof PandoraException)
            intent.putExtra("code", ((PandoraException)exception).getCode());
        intent.putExtra("info", getInfo(exception));
        intent.putExtra("file", file);
        context.startActivity(intent);
        System.exit(1);
    }
}