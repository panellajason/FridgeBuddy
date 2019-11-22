package com.example.app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.R;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private EditText nameET;
    private TextView createdAtTV;
    private TextView expDateTV;
    private RadioButton fridgeBtn;
    private RadioButton freezerBtn;
    private RadioButton cabinetBtn;
    private RadioGroup radioGroup;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = db.collection("Items");
    private String mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Edit Food Item");

        nameET = findViewById(R.id.nameETE);
        expDateTV = findViewById(R.id.expdateTVE);
        createdAtTV = findViewById(R.id.createdAtTV);
        fridgeBtn = findViewById(R.id.fridgeBtnE);
        freezerBtn = findViewById(R.id.freezerBtnE);
        cabinetBtn = findViewById(R.id.cabinetBtnE);
        radioGroup = findViewById(R.id.radioGroupE);


        findViewById(R.id.editBtn).setOnClickListener(this);
        findViewById(R.id.chooseDateBtnE).setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mPath = extras.getString("PATH");
            String name = extras.getString("NAME");
            String expDate = extras.getString("EXP_DATE");
            String storageLocation = extras.getString("STORAGE_LOCATION");
            String createdAt = extras.getString("CREATED_AT");

            createdAtTV.setText("Added on: " + createdAt);
            nameET.setText(name);
            expDateTV.setText(expDate);
            mPath = mPath.substring(6);
            setTitle("Edit: " + name);


            switch (storageLocation) {
                case "Refrigerator":
                    fridgeBtn.setChecked(true);
                    break;
                case "Freezer":
                    freezerBtn.setChecked(true);
                    break;
                case "Cabinet":
                    cabinetBtn.setChecked(true);
                    break;
                default:
                    break;

            }

        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.chooseDateBtnE:
                showDatePicker();
                break;
            case R.id.editBtn:
                editItem();
                break;
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }


    private void editItem() {

        int selectedID = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedID);
        String storageLocation = selectedRadioButton.getText().toString();
        String foodName = nameET.getText().toString();
        String expDate = expDateTV.getText().toString();

        if (foodName.trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a food name", Toast.LENGTH_LONG).show();
            return;
        }

        if (expDate.equals("Expiration Date:")) {
            Toast.makeText(getApplicationContext(), "Please enter an expiration date", Toast.LENGTH_LONG).show();
            return;
        }

        Timestamp timestampDate = new Timestamp(new Date());
        try {

            DateFormat formatter;
            formatter = new SimpleDateFormat("MM/dd/yyyy");
            Date date = formatter.parse(expDate);
            timestampDate = new Timestamp(date);
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
        }

        itemRef.document(mPath).update("name", foodName, "expdate", expDate,
                "storagelocation", storageLocation ,"expTimestamp", timestampDate);


        Toast.makeText(getApplicationContext(), "Food Item Edited", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EditActivity.this, MainActivity.class));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        expDateTV.setText(++month + "/" + dayOfMonth + "/" + year);
    }
}
