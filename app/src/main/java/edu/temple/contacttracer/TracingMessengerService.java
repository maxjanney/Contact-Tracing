package edu.temple.contacttracer;

import android.Manifest;
import android.content.SharedPreferences;
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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

import edu.temple.contacttracer.Tracing.SedentaryEvent;
import edu.temple.contacttracer.Tracing.TracingID;
import edu.temple.contacttracer.Tracing.TracingIDList;

public class TracingMessengerService extends FirebaseMessagingService {

    private static final String TAG = "TracingMessengerService";
    private static final String TRACKING = "/topics/TRACKING";
    private static final String TRACING = "/topics/TRACING";
    private static final double TRACING_DISTANCE = 1.83;    // 1.83 meters ~ 6 feet

    Deque<SedentaryEvent> reports;
    SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(Keys.REPORTS_FILE, MODE_PRIVATE);
        getReports();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String from = remoteMessage.getFrom();
        if (from.equals(TRACKING)) {
            handleReport(remoteMessage);
        } else if (from.equals(TRACING)) {
            // handle tracing message
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveReports();
    }

    private void handleReport(RemoteMessage remoteMessage) {
        JsonObject jsonObj = JsonParser.parseString(remoteMessage.getData().get("payload")).getAsJsonObject();
        SedentaryEvent se = new Gson().fromJson(jsonObj, SedentaryEvent.class);
        se.setDate();
        // ignore our own UUIDs
        if (isExternalID(se.getUUID())) {
            Location currLocation = currentLocation();
            Location sedentaryLocation = se.getLocation();
            // ignore sedentary event if it was not within range
            if (currLocation != null && currLocation.distanceTo(sedentaryLocation) <= TRACING_DISTANCE) {
                reports.add(se);
            }
        }
    }

    private void getReports() {
        String json = preferences.getString(Keys.REPORTS, "");
        // no previously saved list, so create it
        if (json.isEmpty()) {
            reports = new ArrayDeque<>();
        } else {
            // restore list
            Type type = new TypeToken<ArrayDeque<SedentaryEvent>>() {
            }.getType();
            reports = new Gson().fromJson(json, type);
            removeExpiredReports();
        }
    }

    private void removeExpiredReports() {
        long TWO_WEEKS_IN_MILLIS = 1209600000;
        Date twoWeeks = new Date((new Date()).getTime() - TWO_WEEKS_IN_MILLIS);
        for (SedentaryEvent se : reports) {
            if (se.getDate().before(twoWeeks)) {
                reports.remove(se);
            }
        }
    }

    private void saveReports() {
        preferences.edit()
                .putString(Keys.REPORTS, new Gson().toJson(reports))
                .apply();
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