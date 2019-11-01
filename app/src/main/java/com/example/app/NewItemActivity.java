package com.example.app;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class NewItemActivity extends HelperActivity implements View.OnClickListener {

    private Bitmap mBitmap;
    private ImageView mImageView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        mTextView = findViewById(R.id.textView);
        mImageView = findViewById(R.id.imageView);
        findViewById(R.id.btn_imlabeler).setOnClickListener(this);
        findViewById(R.id.btn_textrec).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mTextView.setText(null);
        switch (view.getId()) {
            case R.id.btn_imlabeler:
                runImageLabeler();
                break;
            case R.id.btn_textrec:
                runTextRecognition();
                break;
        }
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
        mTextView.setText(null);
        if (firebaseVisionText.getTextBlocks().size() == 0) {
            mTextView.setText("No text");
            return;
        }

//        List<FirebaseVisionText.TextBlock> results = firebaseVisionText.getTextBlocks();
//        if(results.size() > 2) {
//            for (int i = 0; i < 3; i++) {
//                mTextView.append("block: " + results.get(i).getLines() + "\n");
//            }
//        } else {
//            for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
//                mTextView.append("block: " + block.getLines() + "\n");
//            }
//        }


        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {

            mTextView.append("block: " + block.getText() + "\n");
            for (FirebaseVisionText.Line line : block.getLines()) {
                mTextView.append("line: " + line.getText() + "\n");
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

                        mTextView.append(label.getLabel() + "\n");
                        mTextView.append(label.getConfidence() + "\n\n");
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mTextView.setText(e.getMessage());
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        //mBitmap = MyHelper.resizeImage(imageFile, this, dataUri, mImageView);
                    } else {
                        //mBitmap = MyHelper.resizeImage(imageFile, path, mImageView);
                    }
                    //if (mBitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageURI(dataUri);
                    //}
                    mImageView.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
                    mBitmap = drawable.getBitmap();
                    break;

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_item:
                saveItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveItem() {


    }
}
