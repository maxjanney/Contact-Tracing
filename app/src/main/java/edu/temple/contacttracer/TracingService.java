package edu.temple.contacttracer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

public class TracingService extends Service {

    static final String CHANNEL_ID = "Tracing Service Channel ID";
    static final String CHANNEL_NAME = "Tracing Service Channel";
    static final String DEBUG_TAG = "Service";

    static final long TRANSMISSION_TIME = 60 * 1000;    // 60 seconds, minimum time of transmission
    static final long UPDATE_DISTANCE = 10;             // location updates every 10 meters

    LocationManager locationManager;
    LocationListener locationListener;
    Location previousLocation;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (previousLocation != null) {
                    long elapsedTime = location.getTime() - previousLocation.getTime();
                    if (elapsedTime >= TRANSMISSION_TIME) {
                        Log.d(DEBUG_TAG, "Longer than 60 seconds.");
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

        createNotificationChannel();
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .build();

        startForeground(1, notification);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, UPDATE_DISTANCE, locationListener);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
        }
    }
}