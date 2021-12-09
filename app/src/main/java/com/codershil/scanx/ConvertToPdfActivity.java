package com.codershil.scanx;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.codershil.scanx.adapters.ImageAdapter;
import com.codershil.scanx.databinding.ActivityConvertToPdfBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class ConvertToPdfActivity extends AppCompatActivity implements ImageAdapter.OnDeleteListener {

    private ActivityConvertToPdfBinding binding;
    private ActivityResultLauncher<String> galleryLauncher;
    public ImageAdapter imageAdapter;
    public ArrayList<Uri> selectedImageList = new ArrayList<>();
    Uri imageUri, pdfUri;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConvertToPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBar.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        selectedImageList = (ArrayList<Uri>) bundle.get("imageUriData");
        setUpRecyclerView();

        // handling onclick events and building dialog box
        binding.btnConvertToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // building dialog box
                View view = LayoutInflater.from(ConvertToPdfActivity.this).inflate(R.layout.rename_dialog, null);
                Button btnAutoRename = view.findViewById(R.id.btnAuto);
                Button btnRename = view.findViewById(R.id.btnRename);
                ImageView imgCancel = view.findViewById(R.id.imgCancel);
                EditText edtFileName = view.findViewById(R.id.edtFileName);

                AlertDialog dialog = new AlertDialog.Builder(ConvertToPdfActivity.this)
                        .setView(view)
                        .setCancelable(false)
                        .create();
                dialog.show();

                // setting up listeners on dialog box
                imgCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                btnRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String nameOfFile = edtFileName.getText().toString();
                        if (nameOfFile.isEmpty()) {
                            edtFileName.setError("please enter name first");
                            return;
                        }
                        fileName = nameOfFile + ".pdf";
                        startPdfConversion(dialog);
                    }
                });
                btnAutoRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileName = String.format("%d.pdf", System.currentTimeMillis());
                        startPdfConversion(dialog);
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
                    imageAdapter.updateData(selectedImageList, selectedImageList.size() - 1);
                });

    }


    private void setUpRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ConvertToPdfActivity.this, 3);
        imageAdapter = new ImageAdapter(selectedImageList, ConvertToPdfActivity.this, this);
        binding.selectedImagesRV.setLayoutManager(gridLayoutManager);
        binding.selectedImagesRV.setAdapter(imageAdapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
                ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(selectedImageList,fromPosition,toPosition);
                imageAdapter.updateList(selectedImageList,fromPosition,toPosition);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.selectedImagesRV);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("uriList", selectedImageList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedImageList = savedInstanceState.getParcelableArrayList("uriList");
    }

    // a method that converts image into pdf format and saves it into external storage
    public void convertImageToPdf(ArrayList<Uri> uriList) {

        PdfDocument pdfDocument = new PdfDocument();
        OutputStream outputStream;

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

        // adding pages to pdf document
        for (int i = 0; i < imageBitmapList.size(); i++) {
            Bitmap bitmap = imageBitmapList.get(i);
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), i + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            page.getCanvas().drawBitmap(bitmap, 0, 0, null);
            pdfDocument.finishPage(page);
        }

        // for storing image in external storage in android q and above and else part for less than android q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "files/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "ScanX" + File.separator + "Pdf Documents");

            pdfUri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
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
                Intent intent = new Intent(ConvertToPdfActivity.this, PdfViewerActivity.class);
                intent.putExtra("pdfUri", pdfUri);
                startActivity(intent);
            }
        });

    }

    public void startPdfConversion(Dialog dialog) {
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

    // method to delete an item from selected image list and update recycler view
    @Override
    public void onDeleteClicked(int position) {
        selectedImageList.remove(position);
        imageAdapter.updateData(selectedImageList, position);

    }

    @Override
    public void onImageClicked(int position) {
        // showing dialog box with image that clicked
        View view = LayoutInflater.from(ConvertToPdfActivity.this).inflate(R.layout.image_dialog, null);
        ImageView btnCancel = view.findViewById(R.id.btnCancel);
        ImageView imageToShow = view.findViewById(R.id.imageToShow);
        imageToShow.setImageURI(selectedImageList.get(position));

        AlertDialog dialog = new AlertDialog.Builder(ConvertToPdfActivity.this)
                .setView(view)
                .setCancelable(false)
                .create();
        dialog.show();

        // setting up listeners on dialog box
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}