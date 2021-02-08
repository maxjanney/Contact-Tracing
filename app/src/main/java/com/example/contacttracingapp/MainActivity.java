package com.example.contacttracingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DashboardFragment.Dashboard {

    DashboardFragment dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment tempFrag;

        if ((tempFrag = fm.findFragmentById(R.id.dashboard_fragment)) instanceof DashboardFragment) {
            dashboardFragment = (DashboardFragment) tempFrag;
        } else {
            dashboardFragment = new DashboardFragment();
            fm.beginTransaction()
                    .add(R.id.dashboard_fragment, dashboardFragment)
                    .commit();
        }

        if (!hasGPSPermission()) {
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.app_name), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void start() {
        Toast.makeText(this, "Starting Service", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stop() {
        Toast.makeText(this, "Stopping Service", Toast.LENGTH_SHORT).show();
    }

    private boolean hasGPSPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
    }
}