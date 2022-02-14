package com.codershil.scanx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codershil.scanx.R;
import com.codershil.scanx.databinding.ActivityMainBinding;
import com.codershil.scanx.fragments.HomeFragment;
import com.codershil.scanx.fragments.ImageToolsFragment;
import com.codershil.scanx.fragments.PdfToolsFragment;
import com.codershil.scanx.fragments.UserFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestPermissions();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        initializeViews();
    }

    // a method to setup bottom navigation bar and other views
    private void initializeViews() {
        binding.mainBottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                int id = item.getItemId();
                switch (id) {
                    case R.id.homeItem:
                        transaction.replace(R.id.fragmentContainer, new HomeFragment()).commit();
                        break;
                    case R.id.pdfItem:
                        transaction.replace(R.id.fragmentContainer, new PdfToolsFragment()).commit();
                        break;
                    case R.id.imageTools:
                        transaction.replace(R.id.fragmentContainer, new ImageToolsFragment()).commit();
                        break;
                    case R.id.userItem:
                        transaction.replace(R.id.fragmentContainer, new UserFragment()).commit();
                        break;
                }
                return true;
            }
        });
    }

    // a method for asking permission required for application
    private void requestPermissions() {
        // requesting for external storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                }, PackageManager.PERMISSION_GRANTED);
            }

            // requesting for camera permission
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                }, PackageManager.PERMISSION_GRANTED);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.aboutUs:
                Toast.makeText(MainActivity.this, "Made by G29 Group PCE", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}