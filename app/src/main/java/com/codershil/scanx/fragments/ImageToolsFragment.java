package com.codershil.scanx.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.codershil.scanx.R;
import com.codershil.scanx.activities.ConvertToPdfActivity;
import com.codershil.scanx.activities.CropperActivity;
import com.codershil.scanx.imageTools.ImageTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageToolsFragment extends Fragment {

    private static final int REQUEST_IMAGE_EDIT = 2;
    private ImageView imgSelectedImage, imgEditImage;
    private EditText edtSizeInKB, edtImageWidth, edtImageHeight;
    private Button btnSaveImage;
    Uri selectedImageUri;
    String fileName;


    public ImageToolsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_tools, container, false);
        imgSelectedImage = view.findViewById(R.id.img_selected_image);
        imgEditImage = view.findViewById(R.id.img_edit_image);
        edtSizeInKB = view.findViewById(R.id.edtSizeInKB);
        edtImageWidth = view.findViewById(R.id.edtImageWidth);
        edtImageHeight = view.findViewById(R.id.edtImageHeight);
        btnSaveImage = view.findViewById(R.id.btn_save_image);

        // to get image from gallery
        ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        imgSelectedImage.setImageURI(result);
                        selectedImageUri = result;
                    }
                });
        imgSelectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("image/*");
            }
        });

        imgEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CropperActivity.class);
                intent.putExtra("DATA", selectedImageUri.toString());
                startActivityForResult(intent, REQUEST_IMAGE_EDIT);
            }
        });


        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // building dialog box to show rename option
                View view = LayoutInflater.from(getContext()).inflate(R.layout.rename_image_dialog, null);
                Button btnAutoRename = view.findViewById(R.id.btnAutoRename);
                Button btnRename = view.findViewById(R.id.btnRenameImage);
                ImageView imgCancel = view.findViewById(R.id.imgCancel);
                EditText edtFileName = view.findViewById(R.id.edtImgFileName);

                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setView(view)
                        .setCancelable(false)
                        .create();
                dialog.show();

                btnRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String fileName = edtFileName.getText().toString();
                        if (fileName.isEmpty()) {
                            edtFileName.setError("please enter name first");
                            return;
                        }
                        saveFileToExternalStorage(dialog);
                    }
                });
                btnAutoRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileName = String.format("%d", System.currentTimeMillis());
                        saveFileToExternalStorage(dialog);
                    }
                });

            }
        });

        return view;
    }

    public void saveFileToExternalStorage(Dialog dialog) {
        Bitmap bitmap = ImageTools.UriToBitmap(selectedImageUri, getContext());
        try {
            ImageTools.saveImage(bitmap, fileName, getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        dialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1 && requestCode == REQUEST_IMAGE_EDIT) {
            String result = data.getStringExtra("RESULT");
            Uri resultUri = null;
            if (result != null) {
                resultUri = Uri.parse(result);
                imgSelectedImage.setImageURI(resultUri);
                selectedImageUri = resultUri;
            }
        }
    }
}