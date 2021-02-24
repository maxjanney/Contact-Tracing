package edu.temple.contacttracer.Tracing;

import android.location.Location;

import java.util.Date;

public class SedentaryEvent {

    private final String uuid;
    private final double latitude;
    private final double longitude;
    private final long sedentary_begin;
    private final long sedentary_end;
    private Date date;
    private Location location;

    public SedentaryEvent(String uuid, double latitude, double longitude,
                          long sedentary_begin, long sedentary_end) {
        this.uuid = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sedentary_begin = sedentary_begin;
        this.sedentary_end = sedentary_end;
        this.date = new Date();
    }

    public String getUUID() {
        return uuid;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getSedentaryBegin() {
        return sedentary_begin;
    }

    public long getSedentaryEnd() {
        return sedentary_end;
    }

    public Date getDate() {
        return date;
    }

    public Location getLocation() {
        if (location == null) {
            setLocation();
        }
        return location;
    }

    public void setDate() {
        this.date = new Date();
    }

    private void setLocation() {
        location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
    }

    @Override
    public String toString() {
        return "SedentaryEvent{" +
                "uuid='" + uuid + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", sedentary_begin=" + sedentary_begin +
                ", sedentary_end=" + sedentary_end +
                ", date=" + date +
                '}';
    }
}
