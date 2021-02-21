package edu.temple.contacttracer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;

import edu.temple.contacttracer.Tracing.TracingID;
import edu.temple.contacttracer.Tracing.TracingIDList;
import edu.temple.contacttracer.Tracing.SedentaryEvent;

public class TracingMessengerService extends FirebaseMessagingService {

    private static final String TAG = "TracingMessengerService";
    private static final String TRACKING = "/topics/TRACKING";
    private static final double TRACING_DISTANCE = 1.83;    // 1.83 meters ~ 6 feet

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getFrom().equals(TRACKING)) {
            handleSedentaryEvent(remoteMessage);
        }
    }

    private void handleSedentaryEvent(RemoteMessage remoteMessage) {
        JsonObject jsonObj = JsonParser.parseString(remoteMessage.getData().get("payload")).getAsJsonObject();
        SedentaryEvent se = new Gson().fromJson(jsonObj, SedentaryEvent.class);
        Log.d(TAG, se.toString());
        // ignore our own UUIDs
        if (isExternalID(se.getUUID())) {
            Location currLocation = currentLocation();
            Location sedentaryLocation = se.getLocation();
            // ignore sedentary event if it was not within range
            if (currLocation != null && currLocation.distanceTo(sedentaryLocation) <= TRACING_DISTANCE) {
                saveSedentaryEvent(se);
            }
        }
    }

    private void saveSedentaryEvent(SedentaryEvent se) {
        Log.d(TAG, "Saving sedentary event");
    }

    private boolean isExternalID(String uuid) {
        TracingIDList tracingIDList = TracingIDList.getInstance(this);
        for (TracingID tid : tracingIDList.getIds()) {
            String myId = tid.getUUID().toString();
            if (myId.equals(uuid)) {
                return false;
            }
        }
        return true;
    }

    private Location currentLocation() {
        LocationManager lm = getSystemService(LocationManager.class);
        Location location = null;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return location;
    }

}