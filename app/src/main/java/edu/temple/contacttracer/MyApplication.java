package edu.temple.contacttracer;

import android.app.Application;

public class MyApplication extends Application {

    private boolean foreground;

    public void setForeground(boolean status) {
        foreground = status;
    }

    public boolean isInForeground() {
        return foreground;
    }

}
