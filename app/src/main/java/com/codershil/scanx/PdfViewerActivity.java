package com.codershil.scanx;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.util.ArrayList;

public class PdfViewerActivity extends AppCompatActivity {
    PDFView pdfView;
    Uri pdfUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        pdfView = findViewById(R.id.pdfView);

        Bundle bundle = getIntent().getExtras();
        pdfUri = (Uri) bundle.get("pdfUri");
        displayFromUri(pdfUri);
    }

    private void displayFromUri(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(PdfViewerActivity.this, "error", Toast.LENGTH_LONG).show();
                    }
                })
                .onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        Toast.makeText(PdfViewerActivity.this, "page error", Toast.LENGTH_SHORT).show();
                    }
                })
                .load();
    }

}