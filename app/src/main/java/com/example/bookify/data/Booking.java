package com.example.bookify.data;

public class Booking {
    private final String ticketNumber;
    private final String eventTitle;
    private final String eventDate;
    private final String eventCategory;
    private final String eventPrice;
    private final String status;

    public Booking(String ticketNumber, String eventTitle, String eventDate,
                   String eventCategory, String eventPrice, String status) {
        this.ticketNumber  = ticketNumber;
        this.eventTitle    = eventTitle;
        this.eventDate     = eventDate;
        this.eventCategory = eventCategory;
        this.eventPrice    = eventPrice;
        this.status        = status;
    }

    public String getTicketNumber()  { return ticketNumber; }
    public String getEventTitle()    { return eventTitle; }
    public String getEventDate()     { return eventDate; }
    public String getEventCategory() { return eventCategory; }
    public String getEventPrice()    { return eventPrice; }
    public String getStatus()        { return status; }
}
