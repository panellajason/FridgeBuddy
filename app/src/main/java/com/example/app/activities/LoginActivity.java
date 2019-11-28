package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private EditText emailET, passwordET;
    private Button loginBTN, registerBTN;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Login");

        mAuth = FirebaseAuth.getInstance();
        emailET = (EditText) findViewById(R.id.emailET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        loginBTN = (Button) findViewById(R.id.loginBTN);
        registerBTN = (Button) findViewById(R.id.goToRegisterBTN);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String email = emailET.getText().toString();
                final String password = passwordET.getText().toString();

                if (!(TextUtils.isEmpty(email)) && !(TextUtils.isEmpty(password))) {

                    progressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull final Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                sendToMain();
                            } else {

                                final String errorMess = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: " + errorMess, Toast.LENGTH_SHORT).show();
                            }

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }

            }
        });

        registerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            sendToMain();
        }
    }

    private void sendToMain() {
        final Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}



