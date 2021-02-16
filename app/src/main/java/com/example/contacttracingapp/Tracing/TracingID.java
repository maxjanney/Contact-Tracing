package com.example.contacttracingapp.Tracing;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class TracingID implements Serializable {
    // date id was generated
    private final LocalDate date;

    // actual id
    private final UUID id;

    public TracingID(LocalDate date) {
        this.date = date;
        this.id = UUID.randomUUID();
    }

    public LocalDate getDate() {
        return date;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "TracingID{" +
                "date=" + date +
                ", id=" + id +
                '}';
    }
}
