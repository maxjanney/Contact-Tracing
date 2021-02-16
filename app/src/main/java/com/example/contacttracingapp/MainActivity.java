package com.example.contacttracingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.contacttracingapp.Tracing.TracingID;
import com.example.contacttracingapp.Tracing.TracingIDList;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity implements DashboardFragment.Dashboard {

    TracingIDList tracingIDList;
    Intent tracingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tracingIDList = TracingIDList.getInstance(this);
        tracingIntent = new Intent(this, TracingService.class);

        generateDailyID();

        FragmentManager fm = getSupportFragmentManager();
        if (!(fm.findFragmentById(R.id.dashboard_fragment) instanceof DashboardFragment)) {
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

    private void generateDailyID() {
        LocalDate today = LocalDate.now();
        TracingID currentID = tracingIDList.getCurrentID();
        // no previous ID or current ID is expired, so generate a new one
        if (currentID == null || today.isAfter(currentID.getDate())) {
            tracingIDList.generateID(today, this);
        }
        Log.d("MainActivity", tracingIDList.getIds().toString());
    }
}