package com.codershil.scanx.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codershil.scanx.R;
import com.codershil.scanx.imageTools.ImageTools;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class OCRActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 9;
    private TextView txtOcrText;
    private Button btnCaptureOcr, btnCopyText;
    Bitmap bitmap;
    public CameraView cameraView;
    private ImageView btnCaptureImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocractivity);
        txtOcrText = findViewById(R.id.txtOcrText);
        btnCaptureOcr = findViewById(R.id.btnCaptureOcr);
        btnCopyText = findViewById(R.id.btnCopyText);
        cameraView = findViewById(R.id.camera);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        requestPermissions();
        cameraView.setVisibility(View.GONE);
        btnCaptureImage.setVisibility(View.GONE);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }

            @Override
            public void onError(CameraKitError cameraKitError) {
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                bitmap = cameraKitImage.getBitmap();
                cameraView.setVisibility(View.GONE);
                btnCaptureImage.setVisibility(View.GONE);
                getTextFromImage(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
            }
        });
        cameraView.start();


        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });

        btnCaptureOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.setVisibility(View.VISIBLE);
                btnCaptureImage.setVisibility(View.VISIBLE);
            }
        });

        btnCopyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipBoard(txtOcrText.getText().toString());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            bitmap = (Bitmap) data.getExtras().get("data");
            if (resultCode == RESULT_OK) {
                getTextFromImage(bitmap);
            }
        }
    }

    private void getTextFromImage(Bitmap bitmap) {
        TextRecognizer recognizer = new TextRecognizer.Builder(OCRActivity.this).build();
        StringBuilder stringBuilder = new StringBuilder();
        if (!recognizer.isOperational()) {
            Toast.makeText(OCRActivity.this, "error occurred", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            for (int i = 0; i < textBlockSparseArray.size(); i++) {
                TextBlock textBlock = textBlockSparseArray.get(i);
                stringBuilder.append(textBlock.getValue());
            }
            stringBuilder.append("\n");
            txtOcrText.setText(stringBuilder.toString());
        }
    }

    private void copyToClipBoard(String data) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text copied", data);
        clipboardManager.setPrimaryClip(clip);
        Toast.makeText(OCRActivity.this, "text copied !!", Toast.LENGTH_SHORT).show();
    }


    // a method for asking permission required for application
    private void requestPermissions() {
        // requesting for external storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // requesting for camera permission
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                }, PackageManager.PERMISSION_GRANTED);
            }
        }

    }
}