package com.codershil.scanx.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codershil.scanx.R;
import com.codershil.scanx.activities.SpeechToPdfActivity;

public class PdfToolsFragment extends Fragment {

    private CardView cardSpeechToPdf;

    public PdfToolsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_tools, container, false);

        cardSpeechToPdf = view.findViewById(R.id.card_speech_to_pdf);

        cardSpeechToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SpeechToPdfActivity.class));
            }
        });

        return view;

    }
}