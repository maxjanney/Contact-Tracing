package edu.temple.contacttracer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import edu.temple.contacttracer.Tracing.SedentaryEvent;
import edu.temple.contacttracer.Tracing.SedentaryEventContainer;
import edu.temple.contacttracer.Tracing.TracingIdContainer;

public class TracingService extends Service {

    private static final String CHANNEL_ID = "tracing_service_channel_id";
    private static final String CHANNEL_NAME = "Tracing_service_channel";
    private static final String TAG = "TracingService";
    private static final String URL = "https://kamorris.com/lab/ct_tracking.php";

    private static final long SEDENTARY_TIME = 60 * 1000;   // 60 seconds
    private static final long UPDATE_DISTANCE = 10;         // location updates every 10 meters

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location previousLocation;

    private TracingIdContainer tracingIdContainer;
    SedentaryEventContainer container;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tracingIdContainer = TracingIdContainer.getInstance(this);
        container = SedentaryEventContainer.getSedentaryEventContainer(this, Keys.SEDENTARY_EVENTS_FILE);

        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (previousLocation != null) {
                    long elapsedTime = location.getTime() - previousLocation.getTime();
                    if (elapsedTime >= SEDENTARY_TIME) {
                        Log.d(TAG, "Longer than 60 seconds.");
                        tracePointDetected(location);
                    }
                }
                previousLocation = location;
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) { }

            @Override
            public void onProviderDisabled(@NonNull String provider) { }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, UPDATE_DISTANCE, locationListener);
        }

        Intent startMainActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, startMainActivity, 0);

        createNotificationChannel();
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    private void tracePointDetected(Location location) {
        SedentaryEvent se = new SedentaryEvent(
                tracingIdContainer.getCurrentID().getUUID().toString(),
                previousLocation.getLatitude(),
                previousLocation.getLongitude(),
                previousLocation.getTime(),
                location.getTime()
        );
        container.addSedentaryEvent(se);
        postSedentaryEvent(se);
    }

    private void postSedentaryEvent(SedentaryEvent se) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
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
                params.put("uuid", se.getUUID());
                params.put("latitude", String.valueOf(se.getLatitude()));
                params.put("longitude", String.valueOf(se.getLongitude()));
                params.put("sedentary_begin", String.valueOf(se.getSedentaryBegin()));
                params.put("sedentary_end", String.valueOf(se.getSedentaryEnd()));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
        }
    }
}