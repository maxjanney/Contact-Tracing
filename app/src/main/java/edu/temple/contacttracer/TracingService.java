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

import edu.temple.contacttracer.Tracing.TracingIDList;
import edu.temple.contacttracer.Tracing.SedentaryEvent;

public class TracingService extends Service {

    static final String CHANNEL_ID = "tracing_service_channel_id";
    static final String CHANNEL_NAME = "tracing_service_channel";
    static final String TAG = "TracingService";

    static final long SEDENTARY_TIME = 60 * 1000;   // 60 seconds
    static final long UPDATE_DISTANCE = 10;         // location updates every 10 meters

    LocationManager locationManager;
    LocationListener locationListener;
    Location previousLocation;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        TracingIDList ids = TracingIDList.getInstance(this);
        SedentaryEvent se = new SedentaryEvent(
                ids.getCurrentID().getUUID().toString(),
                previousLocation.getLatitude(),
                previousLocation.getLongitude(),
                previousLocation.getTime(),
                location.getTime()
        );
        postSedentaryEvent(se);
    }

    private void postSedentaryEvent(SedentaryEvent se) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "https://kamorris.com/lab/ct_tracking.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
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