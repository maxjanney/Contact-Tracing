package edu.temple.contacttracer.Tracing;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Date;

public class SedentaryEventContainer implements Serializable {

    private final String fileName;
    final transient private Context context;
    private ArrayDeque<SedentaryEvent> sedentaryEvents;

    public static SedentaryEventContainer getSedentaryEventContainer(Context context, String fileName) {
        return new SedentaryEventContainer(context, fileName);
    }

    public ArrayDeque<SedentaryEvent> getSedentaryEvents() {
        return sedentaryEvents;
    }

    public SedentaryEvent getLatestSedentaryEvent() {
        return sedentaryEvents.peekFirst();
    }

    public void addSedentaryEvent(SedentaryEvent se) {
        sedentaryEvents.addFirst(se);
        save();
    }

    private SedentaryEventContainer(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        sedentaryEvents = new ArrayDeque<>();
        SedentaryEventContainer container = null;

        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = context.openFileInput(fileName);
            ois = new ObjectInputStream(fis);
            container = (SedentaryEventContainer) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (container == null) {
            sedentaryEvents = new ArrayDeque<>();
        } else {
            sedentaryEvents.addAll(container.getSedentaryEvents());
            removeExpiredSedentaryEvents();
        }
    }

    private void removeExpiredSedentaryEvents() {
        boolean removed = false;
        long twoWeeksInMillis = 1209600000;
        Date twoWeeks = new Date((new Date()).getTime() - twoWeeksInMillis);
        for (SedentaryEvent se : sedentaryEvents) {
            if (se.getDate().before(twoWeeks)) {
                sedentaryEvents.remove(se);
                removed = true;
            }
        }

        if (removed) {
            save();
        }
    }

    private void save() {
        FileOutputStream fos;
        ObjectOutputStream oos;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
