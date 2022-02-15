package com.codershil.scanx.activities;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageView;
import android.widget.Toast;
import com.codershil.scanx.R;
import com.codershil.scanx.adapters.ImageAdapter;
import com.codershil.scanx.imageTools.ImageTools;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraView;

import java.util.ArrayList;

public class ImageCaptureActivity extends AppCompatActivity implements ImageAdapter.OnDeleteListener {

    private ImageView btnCaptureImage, btnFlash;
    private RecyclerView recyclerView;
    private ArrayList<Uri> selectedImageList = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private CameraView cameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_capture);
        cameraView = findViewById(R.id.camera);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnFlash = findViewById(R.id.btnFlash);
        recyclerView = findViewById(R.id.recyclerView);

        setUpRecyclerView();

        CameraKitView.ImageCallback imageCallback = new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                Toast.makeText(ImageCaptureActivity.this, "onImage", Toast.LENGTH_SHORT).show();
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Uri imageUri = ImageTools.getImageUri(ImageCaptureActivity.this,imageBitmap);
                Toast.makeText(ImageCaptureActivity.this, "uri : " + imageUri, Toast.LENGTH_SHORT).show();

                selectedImageList.add(imageUri);
                imageAdapter.notifyItemChanged(selectedImageList.size());
            }
        };


        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraKitView.captureImage(imageCallback);
                Toast.makeText(ImageCaptureActivity.this, "onClick", Toast.LENGTH_SHORT).show();

            }
        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraKitView.getFlash() != CameraKit.FLASH_ON) {
                    cameraKitView.setFlash(CameraKit.FLASH_ON);
                    btnFlash.setImageResource(R.drawable.ic_flash_off);
                }
                else{
                    cameraKitView.setFlash(CameraKit.FLASH_OFF);
                    btnFlash.setImageResource(R.drawable.ic_flash_on);
                }
            }
        });

    }


    // method to show all the selected images from the gallery
    private void setUpRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        imageAdapter = new ImageAdapter(selectedImageList, ImageCaptureActivity.this, this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(imageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDeleteClicked(int position) {
        selectedImageList.remove(position);
        imageAdapter.updateData(selectedImageList, position);
    }

    @Override
    public void onImageClicked(int position) {
    }
}