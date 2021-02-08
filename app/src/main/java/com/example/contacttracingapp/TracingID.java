package com.example.contacttracingapp;

import java.time.LocalDate;
import java.util.UUID;

public class TracingID {

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
}