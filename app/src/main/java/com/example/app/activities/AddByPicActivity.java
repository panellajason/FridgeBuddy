package com.example.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.app.R;
import com.example.app.models.Item;
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
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class AddByPicActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    public static final String PRODUCT = "product";
    public static final String PRODUCT_NAME = "product_name";
    private Bitmap mBitmap = null;
    private ImageView mImageView;
    private EditText nameET;
    private TextView expDateTV;
    private RadioGroup radioGroup;
    private Uri postURI = null;
    private RequestQueue mQueue;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = db.collection("Items");

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_by_pic);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Add with Photo");

        nameET = findViewById(R.id.nameET);
        expDateTV = findViewById(R.id.expdateTV);
        mImageView = findViewById(R.id.imageView);
        radioGroup = findViewById(R.id.radioGroup);

        findViewById(R.id.textRecBtn).setOnClickListener(this);
        findViewById(R.id.saveBtn).setOnClickListener(this);
        findViewById(R.id.chooseDateBtn).setOnClickListener(this);
        findViewById(R.id.barcodeScanBtn).setOnClickListener(this);

        mQueue = Volley.newRequestQueue(this);

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(3, 2)
                .start(AddByPicActivity.this);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(3, 2)
                        .start(AddByPicActivity.this);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                startActivity(new Intent(this, AddByPicActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
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
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void runTextRecognition() {
        if (mBitmap != null) {

            final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);

            final FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

            detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(final FirebaseVisionText firebaseVisionText) {

                    if (firebaseVisionText.getTextBlocks().size() == 0) {
                        nameET.setText("No text");
                        return;
                    }

                    for (final FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {

                        for (final FirebaseVisionText.Line line : block.getLines()) {
                            nameET.append(line.getText() + "\n");
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull final Exception exception) {
                    exception.printStackTrace();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void runBarcodeScanner() {
        if (mBitmap != null) {
            final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);

            final FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector();

            final Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(final List<FirebaseVisionBarcode> barcodes) {

                            if (!barcodes.isEmpty()) {
                                for (final FirebaseVisionBarcode barcode : barcodes) {

                                    final String rawValue = barcode.getRawValue();

                                    final String url = "https://world.openfoodfacts.org/api/v0/product/" + rawValue + ".json";

                                    final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                                            new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(final JSONObject response) {
                                            try {
                                                final String foodName = response.getJSONObject(PRODUCT).getString(PRODUCT_NAME);
                                                nameET.setText(foodName);

                                            } catch (final JSONException e) {

                                                nameET.setText(e.getMessage());
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(final VolleyError error) {
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
                        public void onFailure(@NonNull final Exception e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                postURI = result.getUri();
                mImageView.setImageURI(postURI);

                mImageView.invalidate();
                final BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
                mBitmap = drawable.getBitmap();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                final Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveItem() {

        final int selectedID = radioGroup.getCheckedRadioButtonId();
        final RadioButton selectedRadioButton = findViewById(selectedID);
        final String storageLocation = selectedRadioButton.getText().toString();
        final String foodName = nameET.getText().toString();
        final String expDate = expDateTV.getText().toString();

        if (foodName.trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a food name", Toast.LENGTH_LONG).show();
            return;
        }

//
//        Intent intent = new Intent(this, MainActivity.class);
//        final int alarm = Integer.parseInt(Timestamp.now().toString());
//        PendingIntent intent2 = PendingIntent.getBroadcast(this, alarm,intent,0);

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

        }

        itemRef.add(new Item(foodName, expDate, mAuth.getUid(), storageLocation, timestampDate, Timestamp.now()));

        Toast.makeText(getApplicationContext(), "Food Item Saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AddByPicActivity.this, MainActivity.class));
    }

    @Override
    public void onDateSet(final DatePicker view, final int year, int month, final int dayOfMonth) {
        expDateTV.setText(++month + "/" + dayOfMonth + "/" + year);

    }

}
