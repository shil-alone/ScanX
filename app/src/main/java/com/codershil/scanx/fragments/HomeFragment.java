package com.codershil.scanx.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codershil.scanx.ConvertToPdfActivity;
import com.codershil.scanx.R;
import com.codershil.scanx.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    public static final int SELECT_PICTURES = 10;

    public HomeFragment() {
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
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // to get multiple images from gallery
        ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> result) {
                        ArrayList<Uri> imageList = new ArrayList<>(result);
                        imageUriList = imageList;
                        //passing arraylist to ConvertToPdfActivity via intent
                        Intent intent = new Intent(getActivity(), ConvertToPdfActivity.class);
                        intent.putExtra("imageUriData", imageUriList);
                        startActivity(intent);
                        imageUriList.clear();
                    }
                });

        // handling onclick
        binding.selectGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("image/*");
            }
        });

        binding.cameraCaputure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return binding.getRoot();
    }


}