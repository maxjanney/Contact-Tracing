package edu.temple.contacttracer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.temple.contacttracer.Tracing.SedentaryEvent;
import edu.temple.contacttracer.Tracing.SedentaryEventContainer;
import edu.temple.contacttracer.Tracing.TracingIDContainer;

public class TracingMessengerService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "tracing_messenger_service_idd";
    private static final String CHANNEL_NAME = "tracing_messenger_name";
    private static final String TAG = "TracingMessengerService";
    private static final String TRACKING = "/topics/TRACKING";
    private static final String TRACING = "/topics/TRACING";
    private static final double TRACING_DISTANCE = 1.83;    // 1.83 meters ~ 6 feet

    private TracingIDContainer tracingIds;
    private NotificationManager nm;
    private MyApplication myApp;

    @Override
    public void onCreate() {
        super.onCreate();
        tracingIds = TracingIDContainer.getInstance(this);
        myApp = (MyApplication) getApplicationContext();
        nm = getSystemService(NotificationManager.class);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String from = remoteMessage.getFrom();
        if (from.equals(TRACKING)) {
            handleTrackingMsg(remoteMessage);
        } else if (from.equals(TRACING)) {
            // handle tracing message
            handleTracingMsg(remoteMessage);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void handleTrackingMsg(RemoteMessage remoteMessage) {
        JsonObject jsonObj = JsonParser.parseString(remoteMessage.getData().get("payload")).getAsJsonObject();
        SedentaryEvent recvSedentaryEvent = new Gson().fromJson(jsonObj, SedentaryEvent.class);
        recvSedentaryEvent.setDate();

        SedentaryEvent mySedentaryEvent
                = SedentaryEventContainer.getSedentaryEventContainer(this, Constants.SEDENTARY_EVENTS_FILE).getLatestSedentaryEvent();

        // ignore our own UUID
        if (mySedentaryEvent != null && isExternalID(recvSedentaryEvent.getUUID())) {
            Location recvLocation = recvSedentaryEvent.getLocation();
            Location myLocation = mySedentaryEvent.getLocation();
            // ignore sedentary event if it was not within range
            if (myLocation.distanceTo(recvLocation) <= TRACING_DISTANCE) {
                SedentaryEventContainer container
                        = SedentaryEventContainer.getSedentaryEventContainer(this, Constants.REPORTS_FILE);
                container.addSedentaryEvent(recvSedentaryEvent);
                Log.d(TAG, container.toString());
            }
        }
    }

    private void handleTracingMsg(RemoteMessage remoteMessage) {
        Log.d(TAG, "received");
        try {
            String json = remoteMessage.getData().get("payload");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray uuids = jsonObject.getJSONArray("uuids");
            SedentaryEventContainer recvSedentaryEvents
                    = SedentaryEventContainer.getSedentaryEventContainer(this, Constants.REPORTS_FILE);

            if (!isExternalMsg(uuids))
                return;

            for (int i = 0; i < uuids.length(); i++) {
                String uuid = uuids.getString(i);
                for (SedentaryEvent se : recvSedentaryEvents.getSedentaryEvents()) {
                    if (uuid.equals(se.getUUID())) {
                        Intent launchTraceFrag = new Intent(this, MainActivity.class);
                        launchTraceFrag.putExtra("date", jsonObject.getLong("date"));
                        launchTraceFrag.putExtra("location", se.getLocation());
                        if (myApp.isInForeground()) {
                            startActivity(launchTraceFrag);
                        } else {
                            // notify user of positive result
                            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchTraceFrag, 0);
                            createNotificationChannel();
                            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                                    .setContentTitle("Contact Tracing")
                                    .setContentText("Someone you recently came in contact with tested positive!")
                                    .setContentIntent(pendingIntent)
                                    .build();

                            nm.notify(1, notification);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isExternalID(String uuid) {
        return !(tracingIds.getCurrentID().getUUID().toString().equals(uuid));
    }

    private boolean isExternalMsg(JSONArray uuids) throws JSONException {
        SedentaryEventContainer mySedentaryEvents
                = SedentaryEventContainer.getSedentaryEventContainer(this, Constants.SEDENTARY_EVENTS_FILE);
        for (int i = 0; i < uuids.length(); i++) {
            String otherUUID = uuids.getString(i);
            for (SedentaryEvent se : mySedentaryEvents.getSedentaryEvents()) {
                String myUUID = se.getUUID();
                if (myUUID.equals(otherUUID))
                    return false;
            }
        }
        return true;
    }

    private void createNotificationChannel() {
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
    }
}