package com.codershil.scanx.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.fonts.Font;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.codershil.scanx.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechToPdfActivity extends AppCompatActivity {
    private EditText edtPdfText;
    private Button btnConvertTextToPdf;
    private ImageView btnStartRecording;
    private SpeechRecognizer speechRecognizer;
    private boolean isMicOn = false;
    public String resultText = "";
    Uri pdfUri;
    String fileName;
    LottieAnimationView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_pdf);
        edtPdfText = findViewById(R.id.edtPdfText);
        btnConvertTextToPdf = findViewById(R.id.btn_convert_text_to_pdf);
        btnStartRecording = findViewById(R.id.btn_start_recording);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        // building dialog box to show rename option
        View view = LayoutInflater.from(SpeechToPdfActivity.this).inflate(R.layout.mic_dialog, null);
        ImageView imgCancel = view.findViewById(R.id.imgCancel);
        LottieAnimationView lottieAnimationView = view.findViewById(R.id.mic_anim);

        AlertDialog dialog = new AlertDialog.Builder(SpeechToPdfActivity.this)
                .setView(view)
                .setCancelable(false)
                .create();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions();
        }
        else{
            Toast.makeText(this, "Your Device Don't Have this Feature Access", Toast.LENGTH_SHORT).show();
            return;
        }

        // initializing speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String res = data.get(0);
                resultText += " ";
                resultText += res;
                edtPdfText.setText(resultText);
                btnStartRecording.setImageResource(R.drawable.ic_mic_off);
                isMicOn = false;
                speechRecognizer.stopListening();
                dialog.dismiss();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        btnStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMicOn){
                    isMicOn = true;
                    btnStartRecording.setImageResource(R.drawable.ic_mic_on);
                    // Start Listening
                    speechRecognizer.startListening(speechRecognizerIntent);
                    Toast.makeText(SpeechToPdfActivity.this, "started listening...", Toast.LENGTH_SHORT).show();

                    dialog.show();

                    imgCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnStartRecording.setImageResource(R.drawable.ic_mic_off);
                            isMicOn = false;
                            speechRecognizer.stopListening();
                            dialog.dismiss();
                        }
                    });

                }
                else{
                    isMicOn = false;
                    btnStartRecording.setImageResource(R.drawable.ic_mic_off);
                    // Stop Listening
                    speechRecognizer.stopListening();
                    dialog.dismiss();
                }
            }
        });

        btnConvertTextToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultText = edtPdfText.getText().toString();
                if(resultText.trim().isEmpty()){
                    Toast.makeText(SpeechToPdfActivity.this, "please enter text first", Toast.LENGTH_SHORT).show();
                    return;
                }
                // setting auto name
                fileName = String.format("%d.pdf", System.currentTimeMillis());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        convertTextToPdf(resultText, fileName);
                    }
                }).start();
            }
        });
    }

    public void convertTextToPdf(String data, String fileName){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnConvertTextToPdf.setEnabled(false);
            }
        });

        // creating a pdf document
        PdfDocument pdfDocument = new PdfDocument();
        OutputStream outputStream;
        // writing text inside pdf document
        PdfDocument.PageInfo pageInfo = new
                PdfDocument.PageInfo.Builder(300, 400, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        canvas.drawText(data, 10, 10, paint);
        pdfDocument.finishPage(page);

        // storing pdf in external storage
        // for storing pdf in external storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // storing pdf in android q and above
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
            // storing pdf in android version less than android q
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
                Toast.makeText(SpeechToPdfActivity.this, "pdf saved to \"Documents/ScanX/Pdf Documents\"", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                btnConvertTextToPdf.setEnabled(true);
                Intent intent = new Intent(SpeechToPdfActivity.this, PdfViewerActivity.class);
                intent.putExtra("pdfUri", pdfUri);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    // a method for asking permission required for application
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {
        // requesting for record audio permission
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "record audio permission is required", Toast.LENGTH_LONG).show();
        }
    }
}