package com.example.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Spinner spinner;
    private TextView totalItemsTV;
    private TextView expiredItemsTV;
    private Button saveButton;
    private String spinnerTxt = "0";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userItemRef = db.document("User Settings/" + mAuth.getUid());
    private CollectionReference itemRef = db.collection("Items");
    private CollectionReference itemRefSet = db.collection("User Settings");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("Account Settings");

        totalItemsTV = findViewById(R.id.accountItemsNumber);
        expiredItemsTV = findViewById(R.id.accountExpItems);
        saveButton = findViewById(R.id.accountSave);
        spinner = findViewById(R.id.accountSpinner);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.account, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        setQueries();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveSettings();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(!(position == Integer.parseInt(spinnerTxt)))
            saveButton.setVisibility(View.VISIBLE);
        else
            saveButton.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setQueries () {
        Query query1 = itemRef.orderBy("expTimestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid());
        query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    totalItemsTV.setText("Total Number of Food Items: " + task.getResult().size());

                }
            }
        });

        Query query2 = itemRef.orderBy("expTimestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid())
                .whereLessThan("expTimestamp", new Timestamp(Calendar.getInstance().getTime()));
        query2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    expiredItemsTV.setText("Total Number of Expired Food Items: " + task.getResult().size());
                }
            }
        });

        userItemRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()) {

                    spinner.setSelection(Integer.parseInt(documentSnapshot.getString("notification_settings")));
                    spinnerTxt = spinner.getSelectedItem().toString();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            }
        });

    }

    private void saveSettings() {


        if(!spinner.getSelectedItem().toString().equals(spinnerTxt)) {

            itemRefSet.document(mAuth.getUid()).update("notification_settings", spinner.getSelectedItem().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Account Settings Saved", Toast.LENGTH_SHORT).show();
                                MainActivity.settings = Integer.parseInt(spinner.getSelectedItem().toString());
                                startActivity(new Intent(SettingsActivity.this, MainActivity.class));

                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }

    }
}
