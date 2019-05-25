package com.yshare.file.share;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

public class Splash extends BaseActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        themeColorHeader(R.color.colorPrimary);

        new Handler().postDelayed(new Runnable() {

// Using handler with postDelayed called runnable run method

            @Override

            public void run() {

                Intent i = new Intent(Splash.this, MainActivity.class);

                startActivity(i);

                // close this activity

                finish();

            }

        }, 2 * 1000); // wait for 5 seconds
    }
}
