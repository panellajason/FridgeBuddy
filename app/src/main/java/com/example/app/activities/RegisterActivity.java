package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db;
    private CollectionReference itemRef;

    private EditText emailET, passwordET, passwordET2;
    private Button registerBTN, goToLoginBTN;
    private ProgressBar progressBar;
    private Spinner spinner;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle("Register");

        emailET = findViewById(com.example.app.R.id.emailET1);
        passwordET = findViewById(com.example.app.R.id.passwordET1);
        passwordET2 = findViewById(com.example.app.R.id.passwordET2);
        registerBTN = findViewById(com.example.app.R.id.registerBTN);
        goToLoginBTN = findViewById(com.example.app.R.id.goToLoginBTN);
        progressBar = findViewById(com.example.app.R.id.progressBar1);
        spinner = findViewById(R.id.registerSpinner);

        final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.account,
                android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        goToLoginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        registerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String email = emailET.getText().toString();
                final String password1 = passwordET.getText().toString();
                final String password2 = passwordET2.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password1) && !TextUtils.isEmpty(password2)) {

                    progressBar.setVisibility(View.VISIBLE);

                    if (password1.equals(password2)) {

                        mAuth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull final Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    db = FirebaseFirestore.getInstance();
                                    itemRef = db.collection("User Settings");

                                    final Map<String, String> map = new HashMap<>();
                                    map.put("notification_settings", spinner.getSelectedItem() + "");

                                    itemRef.document(mAuth.getUid()).set(map);

                                    sendToMain();

                                } else {

                                    final String error = task.getException().getMessage();
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

        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        final Intent i = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {

    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {

    }
}
