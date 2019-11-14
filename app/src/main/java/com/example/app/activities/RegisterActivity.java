package com.example.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailET, passwordET, passwordET2;
    private Button registerBTN, goToLoginBTN;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        setTitle("Register");


        emailET = (EditText) findViewById(R.id.emailET1);
        passwordET = (EditText) findViewById(R.id.passwordET1);
        passwordET2 = (EditText) findViewById(R.id.passwordET2);
        registerBTN = (Button) findViewById(R.id.registerBTN);
        goToLoginBTN = (Button) findViewById(R.id.goToLoginBTN);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        goToLoginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        registerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password1 = passwordET.getText().toString();
                String password2 = passwordET2.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password1) && !TextUtils.isEmpty(password2)) {

                    progressBar.setVisibility(View.VISIBLE);

                    if (password1.equals(password2)) {

                        mAuth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    sendToMain();

                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.INVISIBLE);

                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

}
