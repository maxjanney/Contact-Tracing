package edu.temple.contacttracer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.temple.contacttracer.Tracing.TracingID;
import edu.temple.contacttracer.Tracing.TracingIDList;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity implements DashboardFragment.Dashboard {

    static final String TRACING_TOPIC = "TRACKING";
    static final String TAG = "MainActivity";

    TracingIDList tracingIDList;
    Intent tracingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        FragmentManager fm = getSupportFragmentManager();
        if (!(fm.findFragmentById(R.id.dashboard_fragment) instanceof DashboardFragment)) {
            fm.beginTransaction()
                    .add(R.id.dashboard_fragment, new DashboardFragment())
                    .commit();
        }

        tracingIDList = TracingIDList.getInstance(this);
        tracingIntent = new Intent(this, TracingService.class);

        generateDailyID();
        subscribeToTopic(TRACING_TOPIC);
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
    }

    private void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                   String msg = task.isSuccessful() ? "success" : "fail";
                   Log.d(TAG, msg);
                });
    }
}