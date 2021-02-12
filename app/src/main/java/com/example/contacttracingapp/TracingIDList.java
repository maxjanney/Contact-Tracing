package com.example.contacttracingapp;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;

public class TracingIDList {
    private static final String LIST_FILE = "list";
   
    private static TracingIDList instance;
    private ArrayList<TracingID> ids;

    public static TracingIDList getInstance(Context context) {
        if (instance == null) {
            instance = new TracingIDList(context);
        }
        return instance;
    }

    public void generateID(LocalDate date, Context context) {
        ids.add(new TracingID(date));
        saveIDs(context);
    }

    @SuppressWarnings("unchecked")
    private TracingIDList(Context context) {
        FileInputStream fis;
        ObjectInput ois;
        try {
            // try and restore list from storage
            fis = context.openFileInput(LIST_FILE);
            ois = new ObjectInputStream(fis);
            ids = (ArrayList<TracingID>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception ignored) {
            // no previously stored list, so create it
            ids = new ArrayList<>();
        }
    }

    private void saveIDs(Context context) {
        FileOutputStream fos;
        ObjectOutput oos;
        try {
            fos = context.openFileOutput(LIST_FILE, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ids);
            oos.close();
            fos.close();
        } catch (Exception ignored) { }
    }
}
