package edu.temple.contacttracer;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.temple.contacttracer.Tracing.SedentaryEvent;
import edu.temple.contacttracer.Tracing.SedentaryEventContainer;
import edu.temple.contacttracer.Tracing.TracingIdContainer;

public class TracingMessengerService extends FirebaseMessagingService {

    private static final String TAG = "TracingMessengerService";
    private static final String TRACKING = "/topics/TRACKING";
    private static final String TRACING = "/topics/TRACING";
    private static final double TRACING_DISTANCE = 1.83;    // 1.83 meters ~ 6 feet

    private SedentaryEventContainer container;
    private TracingIdContainer tracingIds;

    @Override
    public void onCreate() {
        super.onCreate();
        tracingIds = TracingIdContainer.getInstance(this);
        container = SedentaryEventContainer.getSedentaryEventContainer(this, Keys.REPORTS_FILE);
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
    }

    private void handleReport(RemoteMessage remoteMessage) {
        JsonObject jsonObj = JsonParser.parseString(remoteMessage.getData().get("payload")).getAsJsonObject();
        SedentaryEvent recvSedentaryEvent = new Gson().fromJson(jsonObj, SedentaryEvent.class);
        recvSedentaryEvent.setDate();
        SedentaryEvent mySedentaryEvent = container.getLatestSedentaryEvent();
        // ignore our own UUIDs
        Log.d(TAG, recvSedentaryEvent.toString());
        if (mySedentaryEvent != null && isExternalID(recvSedentaryEvent.getUUID())) {
            Location recvLocation = recvSedentaryEvent.getLocation();
            Location myLocation = mySedentaryEvent.getLocation();
            // ignore sedentary event if it was not within range
            if (myLocation.distanceTo(recvLocation) <= TRACING_DISTANCE) {
                container.addSedentaryEvent(recvSedentaryEvent);
                Log.d(TAG, container.toString());
            }
        }
    }

    private boolean isExternalID(String uuid) {
        return !(tracingIds.getCurrentID().getUUID().toString().equals(uuid));
    }

}