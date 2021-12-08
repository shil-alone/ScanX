package com.codershil.scanx;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codershil.scanx.adapters.ImageAdapter;
import com.codershil.scanx.databinding.ActivityConvertToPdfBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ConvertToPdfActivity extends AppCompatActivity implements ImageAdapter.OnDeleteListener {
    private ActivityConvertToPdfBinding binding;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> selectedImageList = new ArrayList<>();
    private ActivityResultLauncher<String> galleryLauncher;
    Uri imageUri;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConvertToPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBar.setVisibility(View.GONE);

        // setting up recycler view
        Bundle bundle = getIntent().getExtras();
        selectedImageList = (ArrayList<Uri>) bundle.get("imageUriData");
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ConvertToPdfActivity.this, 3);
        imageAdapter = new ImageAdapter(selectedImageList, ConvertToPdfActivity.this,this);
        binding.selectedImagesRV.setLayoutManager(gridLayoutManager);
        binding.selectedImagesRV.setAdapter(imageAdapter);


        // handling onclick events and building dialog box
        binding.btnConvertToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(ConvertToPdfActivity.this).inflate(R.layout.rename_dialog, null);
                Button btnCancel = view.findViewById(R.id.btnCancel);
                Button btnRename = view.findViewById(R.id.btnRename);
                EditText edtFileName = view.findViewById(R.id.edtFileName);

                AlertDialog dialog = new AlertDialog.Builder(ConvertToPdfActivity.this)
                        .setView(view)
                        .setCancelable(false)
                        .create();
                dialog.show();

                btnRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileName = edtFileName.getText().toString()+".pdf";
                        dialog.dismiss();
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.btnConvertToPdf.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                convertImageToPdf(selectedImageList);
                            }
                        }).start();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileName = String.format("%d.pdf", System.currentTimeMillis());
                        dialog.dismiss();
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.btnConvertToPdf.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                convertImageToPdf(selectedImageList);
                            }
                        }).start();
                    }
                });
            }
        });

        binding.addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryLauncher.launch("image/*");
            }
        });

        // gallery launcher is to get image from gallery
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    imageUri = uri;
                    selectedImageList.add(uri);
                    imageAdapter.updateData(selectedImageList,selectedImageList.size()-1);
                });

    }


    // a method that converts image into pdf format and saves it into external storage
    public void convertImageToPdf(ArrayList<Uri> uriList) {

        // converting uris to bitmaps
        ArrayList<Bitmap> imageBitmapList = new ArrayList<>();
        for (int i = 0; i < uriList.size(); i++) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriList.get(i));
                imageBitmapList.add(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PdfDocument pdfDocument = new PdfDocument();

        // adding pages to pdf document
        for (int i = 0; i < imageBitmapList.size(); i++) {
            Bitmap bitmap = imageBitmapList.get(i);
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), i + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            page.getCanvas().drawBitmap(bitmap, 0, 0, null);
            pdfDocument.finishPage(page);
        }

        // for storing image in external storage in android q and above and else part for less than android q
        OutputStream outputStream;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "files/pdf");
            contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "ScanX" + File.separator + "Pdf Documents");

            Uri pdfUri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
            try {
                outputStream = contentResolver.openOutputStream(pdfUri);
                pdfDocument.writeTo(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File file = new File(Environment.DIRECTORY_DOCUMENTS + File.separator + "ScanX" + File.separator + "Pdf Documents");
            if (!file.mkdirs()) {
                file.mkdirs();
            }
            String filePath = file.getAbsolutePath() + File.separator + fileName;
            try {
                pdfDocument.writeTo(new FileOutputStream(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        pdfDocument.close();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConvertToPdfActivity.this, "pdf saved to \"Documents/ScanX/Pdf Documents\"", Toast.LENGTH_LONG).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.btnConvertToPdf.setEnabled(true);
            }
        });
    }

    // method to delete an item from selected image list and update recycler view
    @Override
    public void onDeleteClicked(int position) {
        selectedImageList.remove(position);
        imageAdapter.updateData(selectedImageList,position);

    }
}