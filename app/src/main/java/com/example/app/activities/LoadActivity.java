package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;

public class LoadActivity extends AppCompatActivity {

    private static int SPLASH_TIMEOUT = 2000;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent splash = new Intent(LoadActivity.this, MainActivity.class);
                startActivity(splash);
                finish();
            }
        }, SPLASH_TIMEOUT);
    }
}
