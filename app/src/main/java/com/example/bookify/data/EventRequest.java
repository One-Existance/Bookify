package com.example.bookify.data;

public class EventRequest {
    private Event event;
    private String organizerName;

    public EventRequest(Event event, String organizerName) {
        this.event = event;
        this.organizerName = organizerName;
    }

    public Event getEvent()          { return event; }
    public String getOrganizerName() { return organizerName; }
}
