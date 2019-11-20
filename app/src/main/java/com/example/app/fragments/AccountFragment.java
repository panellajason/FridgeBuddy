package com.example.app.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.R;
import com.example.app.activities.MainActivity;
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


public class AccountFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private TextView totalItemsTV;
    private TextView expiredItemsTV;
    private Button saveButton;
    private String spinnerTxt;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userItemRef = db.document("User Settings/" + mAuth.getUid());
    private CollectionReference itemRef = db.collection("Items");
    private CollectionReference itemRefSet = db.collection("User Settings");



    public AccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        getActivity().setTitle("Account Settings");

        totalItemsTV = view.findViewById(R.id.accountItemsNumber);
        expiredItemsTV = view.findViewById(R.id.accountExpItems);
        saveButton = view.findViewById(R.id.accountSave);
        spinner = view.findViewById(R.id.accountSpinner);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.account, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!spinner.getSelectedItem().toString().equals(spinnerTxt)) {

                    itemRefSet.document(mAuth.getUid()).update("notification_settings", spinner.getSelectedItem().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Account Settings Saved", Toast.LENGTH_SHORT).show();
                                        MainActivity.settings = Integer.parseInt(spinner.getSelectedItem().toString());

                                    }
                                    else {
                                        Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }

                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));

            }
        });

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onStart() {
        super.onStart();

        Query query1 = itemRef.orderBy("timestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid());

        query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    totalItemsTV.setText("Total Number of Food Items: " + task.getResult().size());

                } else {
                    Toast.makeText(getActivity(), "Data doesn't exist1", Toast.LENGTH_SHORT).show();

                }

            }
        });

        Query query2 = itemRef.orderBy("timestamp", Query.Direction.ASCENDING).whereEqualTo("userID", mAuth.getUid())
                .whereLessThan("timestamp", new Timestamp(Calendar.getInstance().getTime()));

        query2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    expiredItemsTV.setText("Total Number of Expired Food Items: " + task.getResult().size());

                } else {
                    Toast.makeText(getActivity(), "Data doesn't exist2", Toast.LENGTH_SHORT).show();

                }

            }
        });

        userItemRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()) {

                    spinner.setSelection(Integer.parseInt(documentSnapshot.getString("notification_settings")));

                } else {
                    Toast.makeText(getActivity(), "Document doesn't exist", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}