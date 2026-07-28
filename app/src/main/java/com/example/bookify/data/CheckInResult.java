package com.example.bookify.data;

/** Result of scanning a ticket QR at the door via ScanEntryActivity. */
public class CheckInResult {

    public enum Status { CHECKED_IN, ALREADY_CHECKED_IN, WRONG_EVENT, UNPAID, INVALID_TICKET }

    public final Status status;
    public final String attendeeName;
    public final String eventTitle;

    public CheckInResult(Status status, String attendeeName, String eventTitle) {
        this.status = status;
        this.attendeeName = attendeeName;
        this.eventTitle = eventTitle;
    }
}
