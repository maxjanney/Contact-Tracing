package edu.temple.contacttracer.Tracing;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class TracingID implements Serializable {

    private final LocalDate date;
    private final UUID id;

    public TracingID() {
        this.date = LocalDate.now();
        this.id = UUID.randomUUID();
    }

    public LocalDate getDate() {
        return date;
    }

    public UUID getUUID() {
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