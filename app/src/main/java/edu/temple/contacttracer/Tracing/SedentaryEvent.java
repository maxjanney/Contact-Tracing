package edu.temple.contacttracer.Tracing;

import android.location.Location;

import java.time.LocalDate;

public class SedentaryEvent {

    private final String uuid;
    private final double latitude;
    private final double longitude;
    private final long sedentary_begin;
    private final long sedentary_end;
    private final Location location;
    private final LocalDate date;

    public SedentaryEvent(String uuid, double latitude, double longitude,
                          long sedentary_begin, long sedentary_end) {
        this.uuid = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sedentary_begin = sedentary_begin;
        this.sedentary_end = sedentary_end;
        location = new Location("");
        location.setLongitude(this.latitude);
        location.setLongitude(this.longitude);
        date = LocalDate.now();
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

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "TrackingMessage{" +
                "uuid='" + uuid + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", sedentaryStart=" + sedentary_begin +
                ", sedentaryStop=" + sedentary_end +
                '}';
    }
}
