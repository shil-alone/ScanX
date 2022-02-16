package com.codershil.scanx.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codershil.scanx.R;
import com.codershil.scanx.adapters.ImageAdapter;
import com.codershil.scanx.imageTools.ImageTools;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageCaptureActivity extends AppCompatActivity {

    private ImageView btnCaptureImage, btnFlash, btnConvert, btnSwitchCamera;
    public CameraView cameraView;
    private boolean isFlash = false;
    public ArrayList<Uri> selectedImageList = new ArrayList<>();
    private TextView txtImageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_capture);
        cameraView = findViewById(R.id.camera);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnFlash = findViewById(R.id.btnFlash);
        btnConvert = findViewById(R.id.btnConvert);
        txtImageCount = findViewById(R.id.txtImageCount);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);


        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }

            @Override
            public void onError(CameraKitError cameraKitError) {
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap bitmap = cameraKitImage.getBitmap();
                Uri uri = ImageTools.getImageUri(ImageCaptureActivity.this, bitmap);
                selectedImageList.add(uri);
                txtImageCount.setText(String.valueOf(selectedImageList.size()));
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

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFlash) {
                    startTorch(true);
                    btnFlash.setImageResource(R.drawable.ic_flash_off);
                    isFlash = true;
                } else {
                    startTorch(false);
                    btnFlash.setImageResource(R.drawable.ic_flash_on);
                    isFlash = false;
                }
            }
        });

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageCaptureActivity.this, ConvertToPdfActivity.class);
                ConvertToPdfActivity.selectedImageList.addAll(selectedImageList);
                startActivity(intent);
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // switching camera from front to rear or rear to front
                if (cameraView != null) {
                    int facing = cameraView.getFacing();
                    cameraView.setFacing(facing == CameraKit.Constants.FACING_FRONT ?
                            CameraKit.Constants.FACING_BACK : CameraKit.Constants.FACING_FRONT);
                }
            }
        });

    }

    // method for turning on torch
    private void startTorch(boolean state) {
        try {
            // turning flash on
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = cameraManager.getCameraIdList()[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, state);
            } else {
                Toast.makeText(ImageCaptureActivity.this, "Required Android 6 and above", Toast.LENGTH_SHORT).show();
            }
        } catch (CameraAccessException e) {

            Toast.makeText(ImageCaptureActivity.this, "unable to start torch : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        cameraView.start();
    }

}