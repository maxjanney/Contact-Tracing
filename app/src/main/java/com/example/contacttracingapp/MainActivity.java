package com.example.contacttracingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DashboardFragment.Dashboard {

    Intent tracingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tracingIntent = new Intent(this, TracingService.class);

        FragmentManager fm = getSupportFragmentManager();
        if (!(fm.findFragmentById(R.id.dashboard_fragment) instanceof DashboardFragment)) {
            Log.d("this is a tag", "adding frag");
            fm.beginTransaction()
                    .add(R.id.dashboard_fragment, new DashboardFragment())
                    .commit();
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.required_permission), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void start() {
        startService(tracingIntent);
    }

    @Override
    public void stop() {
        stopService(tracingIntent);
    }
}