package edu.temple.contacttracer.Tracing;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;

public class TracingIdContainer {

    private static final String LIST_FILE = "list";
    private static final int MAX_IDS = 14;

    private static TracingIdContainer instance;
    private ArrayDeque<TracingId> ids;
    private static Context ctx;

    public static TracingIdContainer getInstance(Context context) {
        if (instance == null) {
            context = context.getApplicationContext();
            instance = new TracingIdContainer(context);
        }
        return instance;
    }

    public void generateID() {
        ids.addFirst(new TracingId());
        if (ids.size() > MAX_IDS) {
            ids.pollLast();
        }
        saveIDs();
    }

    public ArrayDeque<TracingId> getIds() {
        return ids;
    }

    public TracingId getCurrentID() {
        return ids.peekFirst();
    }

    @SuppressWarnings("unchecked")
    private TracingIdContainer(Context context) {
        ctx = context;
        FileInputStream fis;
        ObjectInput ois;
        try {
            // try and restore list from storage
            fis = context.openFileInput(LIST_FILE);
            ois = new ObjectInputStream(fis);
            ids = (ArrayDeque<TracingId>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception ignored) {
            // no previously stored list, so create it
            ids = new ArrayDeque<>();
        }
    }

    private void saveIDs() {
        FileOutputStream fos;
        ObjectOutput oos;
        try {
            fos = ctx.openFileOutput(LIST_FILE, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ids);
            oos.close();
            fos.close();
        } catch (Exception ignored) { }
    }
}
