package com.example.app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;


import com.example.app.models.Item;
import com.example.app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.List;

public class NewItemActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private Bitmap mBitmap;
    private ImageView mImageView;
    private EditText nameET;
    private TextView expDateTV;
    private RadioButton fridgeBtn;
    private RadioButton freezerBtn;
    private RadioButton cabinetBtn;
    private RadioGroup radioGroup;
    private Button chooseDateBtn;
    private Button saveBtn;
    private Uri postURI = null;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Add Food Item");

        mAuth = FirebaseAuth.getInstance();

        nameET = findViewById(R.id.nameET);
        expDateTV = findViewById(R.id.expdateTV);
        mImageView = findViewById(R.id.imageView);
        fridgeBtn = findViewById(R.id.fridgeBtn);
        freezerBtn = findViewById(R.id.freezerBtn);
        cabinetBtn = findViewById(R.id.cabinetBtn);
        chooseDateBtn = findViewById(R.id.chooseDateBtn);
        saveBtn = findViewById(R.id.saveBtn);
        radioGroup = findViewById(R.id.radioGroup);

        findViewById(R.id.imageLabelerBtn).setOnClickListener(this);
        findViewById(R.id.textRecBtn).setOnClickListener(this);
        findViewById(R.id.saveBtn).setOnClickListener(this);
        findViewById(R.id.chooseDateBtn).setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_gallery:
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1,1)
                        .start(NewItemActivity.this);

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.imageLabelerBtn:
                nameET.setText(null);
                runImageLabeler();
                break;
            case R.id.textRecBtn:
                nameET.setText(null);
                runTextRecognition();
                break;
            case R.id.chooseDateBtn:
                showDatePicker();
                break;
            case R.id.saveBtn:
                saveItem();
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

    private void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processExtractedText(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(NewItemActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processExtractedText(FirebaseVisionText firebaseVisionText) {
        if (firebaseVisionText.getTextBlocks().size() == 0) {
            nameET.setText("No text");
            return;
        }

        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {

            for (FirebaseVisionText.Line line : block.getLines()) {
                nameET.append(line.getText() + "\n");
            }
        }
    }

    private void runImageLabeler() {
        if (mBitmap != null) {

            FirebaseVisionLabelDetectorOptions options = new FirebaseVisionLabelDetectorOptions.Builder()
                    .setConfidenceThreshold(0.7f)
                    .build();

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
            FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(options);

            detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                @Override
                public void onSuccess(List<FirebaseVisionLabel> labels) {
                    for (FirebaseVisionLabel label : labels) {

                        nameET.append(label.getLabel() + "\n");
                        //mTextView.append(label.getConfidence() + "\n\n");
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                postURI = result.getUri();
                mImageView.setImageURI(postURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        mImageView.invalidate();
        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
        mBitmap = drawable.getBitmap();

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveItem() {

        int selectedID = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton)findViewById(selectedID);
        String storageLocation = selectedRadioButton.getText().toString();
        String foodName = nameET.getText().toString();
        String expDate = expDateTV.getText().toString();

        if(foodName.trim().isEmpty() || expDate.equals("Expiration Date:")) {
            Toast.makeText(getApplicationContext(), "Please enter a food name and/or expiration date.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference itemRef = FirebaseFirestore.getInstance()
                .collection("Items");
        itemRef.add(new Item(foodName, expDate, mAuth.getUid()));

        Toast.makeText(getApplicationContext(), "Food Item Saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NewItemActivity.this, MainActivity.class));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        expDateTV.setText(month + "/" + dayOfMonth + "/" + year);
    }
}
