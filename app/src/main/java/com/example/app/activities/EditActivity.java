package com.example.app.activities;

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
import androidx.appcompat.app.AppCompatActivity;
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

    public static final String NAME = "name";
    public static final String EXPDATE = "expdate";
    public static final String STORAGELOCATION = "storagelocation";
    public static final String EXP_TIMESTAMP = "expTimestamp";
    public static final String PATH = "PATH";
    public static final String PRODUCT_NAME = "NAME";
    public static final String EXP_DATE = "EXP_DATE";
    public static final String STORAGE_LOCATION = "STORAGE_LOCATION";
    public static final String CREATED_AT = "CREATED_AT";
    private EditText nameET;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RadioGroup radioGroup;
    private String mPath;
    private TextView expDateTV;

    private final CollectionReference itemRef = db.collection("Items");

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Edit Food Item");

        nameET = findViewById(R.id.nameETE);
        expDateTV = findViewById(R.id.expdateTVE);
        final TextView createdAtTV = findViewById(R.id.createdAtTV);
        final RadioButton fridgeBtn = findViewById(R.id.fridgeBtnE);
        final RadioButton freezerBtn = findViewById(R.id.freezerBtnE);
        final RadioButton cabinetBtn = findViewById(R.id.cabinetBtnE);
        radioGroup = findViewById(R.id.radioGroupE);

        findViewById(R.id.editBtn).setOnClickListener(this);
        findViewById(R.id.chooseDateBtnE).setOnClickListener(this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mPath = extras.getString(PATH);
            final String name = extras.getString(PRODUCT_NAME);
            final String expDate = extras.getString(EXP_DATE);
            final String storageLocation = extras.getString(STORAGE_LOCATION);
            final String createdAt = extras.getString(CREATED_AT);

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
    public void onClick(final View view) {

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
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void editItem() {

        final int selectedID = radioGroup.getCheckedRadioButtonId();
        final RadioButton selectedRadioButton = findViewById(selectedID);
        final String storageLocation = selectedRadioButton.getText().toString();
        final String foodName = nameET.getText().toString();
        final String expDate = expDateTV.getText().toString();

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

            final DateFormat formatter;
            formatter = new SimpleDateFormat("MM/dd/yyyy");
            final Date date = formatter.parse(expDate);
            timestampDate = new Timestamp(date);
        } catch (final ParseException e) {
            System.out.println("Exception :" + e);
        }

        itemRef.document(mPath).update(NAME, foodName, EXPDATE, expDate,
                STORAGELOCATION, storageLocation, EXP_TIMESTAMP, timestampDate);

        Toast.makeText(getApplicationContext(), "Food Item Edited", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EditActivity.this, MainActivity.class));
    }

    @Override
    public void onDateSet(final DatePicker view, final int year, int month, final int dayOfMonth) {
        expDateTV.setText(++month + "/" + dayOfMonth + "/" + year);
    }
}
