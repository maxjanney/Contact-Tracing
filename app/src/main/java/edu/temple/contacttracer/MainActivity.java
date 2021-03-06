package edu.temple.contacttracer;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.temple.contacttracer.Tracing.SedentaryEvent;
import edu.temple.contacttracer.Tracing.SedentaryEventContainer;
import edu.temple.contacttracer.Tracing.TracingID;
import edu.temple.contacttracer.Tracing.TracingIDContainer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DashboardFragment.Dashboard {

    static final String TRACKING_TOPIC = "TRACKING";
    static final String TRACING_TOPIC = "TRACING";
    static final String TAG = "MainActivity";

    RequestQueue requestQueue;
    TracingIDContainer tracingIDList;
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

        tracingIDList = TracingIDContainer.getInstance(this);
        tracingIntent = new Intent(this, TracingService.class);
        requestQueue = Volley.newRequestQueue(this);

        generateDailyID();

        subscribeToTopic(TRACKING_TOPIC);
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
            tracingIDList.generateID();
        }
        Log.d(TAG, tracingIDList.getIds().toString());
    }

    @Override
    public void report() {
        DatePickerDialog d = new DatePickerDialog(this);
        d.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                long date = calendar.getTimeInMillis();

                ArrayList<String> uuids = new ArrayList<>();
                SedentaryEventContainer container = SedentaryEventContainer.getSedentaryEventContainer(MainActivity.this, Constants.SEDENTARY_EVENTS_FILE);
                for (SedentaryEvent se : container.getSedentaryEvents()) {
                    uuids.add(se.getUUID());
                }

                StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.TRACING_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("date", String.valueOf(date));
                        params.put("uuids", uuids.toString());
                        return params;
                    }
                };
                requestQueue.add(stringRequest);
            }
        });
        d.show();
    }

    private void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, topic + (task.isSuccessful() ? "success" : "fail"));
                    }
                });
    }
}