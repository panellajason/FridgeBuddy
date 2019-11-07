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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app.models.Item;
import com.example.app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private Bitmap mBitmap = null;
    private ImageView mImageView;
    private EditText nameET;
    private TextView expDateTV;
    private RadioGroup radioGroup;
    private Uri postURI = null;
    private RequestQueue mQueue;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = db.collection("Items");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Add Food Item");

        nameET = findViewById(R.id.nameET);
        expDateTV = findViewById(R.id.expdateTV);
        mImageView = findViewById(R.id.imageView);
        radioGroup = findViewById(R.id.radioGroup);

        findViewById(R.id.imageLabelerBtn).setOnClickListener(this);
        findViewById(R.id.textRecBtn).setOnClickListener(this);
        findViewById(R.id.saveBtn).setOnClickListener(this);
        findViewById(R.id.chooseDateBtn).setOnClickListener(this);
        findViewById(R.id.barcodeScanBtn).setOnClickListener(this);

        mQueue = Volley.newRequestQueue(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_or_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_gallery:
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(3, 2)
                        .start(AddActivity.this);
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
            case R.id.barcodeScanBtn:
                nameET.setText(null);
                runBarcodeScanner();
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
        if (mBitmap != null) {

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);

            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

            detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {

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
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
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
                    if(labels.size() != 0) {
                        for (FirebaseVisionLabel label : labels) {

                            nameET.append(label.getLabel() + "\n");
                            //mTextView.append(label.getConfidence() + "\n\n");
                        }
                    } else {
                        nameET.setText("No results");
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void runBarcodeScanner() {
        if (mBitmap != null) {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);

            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector();

            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {

                            if (barcodes.size() != 0) {
                                for (FirebaseVisionBarcode barcode : barcodes) {

                                    String rawValue = barcode.getRawValue();

                                    String url = "https://world.openfoodfacts.org/api/v0/product/" + rawValue + ".json";

                                    final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,                new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String foodName = response.getJSONObject("product").getString("product_name");
                                                nameET.setText(foodName);

                                            } catch (JSONException e) {

                                                nameET.setText(e.getMessage());
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                        }
                                    });

                                    mQueue.add(request);
                                }
                            } else {
                                nameET.setText("No results");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                postURI = result.getUri();
                mImageView.setImageURI(postURI);

                mImageView.invalidate();
                BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
                mBitmap = drawable.getBitmap();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveItem() {

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

        itemRef.add(new Item(foodName, expDate, mAuth.getUid(), storageLocation, timestampDate));


        Toast.makeText(getApplicationContext(), "Food Item Saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AddActivity.this, MainActivity.class));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        expDateTV.setText(++month + "/" + dayOfMonth + "/" + year);

    }

}
