package com.example.bookify.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Events live in Cloud Firestore (collection "events") so they sync across devices, unlike the
 * rest of the app's data which stays in local SQLite (see DatabaseHelper). Every method returns
 * a Task, matching the addOnSuccessListener/addOnFailureListener idiom already used for Firebase
 * Auth calls elsewhere in the app, so callers don't need a new callback pattern.
 *
 * Ordering is always done client-side (never via Query.orderBy() combined with a where() clause)
 * to avoid requiring a Firestore composite index for every filtered query.
 */
public class EventsRepository {

    private static final String COLLECTION = "events";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private com.google.firebase.firestore.CollectionReference collection() {
        return db.collection(COLLECTION);
    }

    public Task<List<Event>> getAllEvents() {
        return collection()
                .whereEqualTo("isPrivate", false)
                .whereEqualTo("status", Event.STATUS_PUBLISHED)
                .get()
                .continueWith(task -> sortedByCreatedAtDesc(task.getResult()));
    }

    public Task<List<Event>> getAllEventsForAdmin() {
        return collection().get().continueWith(task -> sortedByCreatedAtDesc(task.getResult()));
    }

    /** Admin direct-publish path: status is PUBLISHED immediately and an access code is generated. */
    public Task<String> addEvent(Event draft) {
        Map<String, Object> fields = baseFields(draft);
        fields.put("status", Event.STATUS_PUBLISHED);
        fields.put("accessCode", generateAccessCode());
        return collection().add(fields).continueWith(task -> task.getResult().getId());
    }

    /** Organizer submission path: status is PENDING, no access code until a promoter approves it. */
    public Task<String> requestEvent(Event draft) {
        Map<String, Object> fields = baseFields(draft);
        fields.put("status", Event.STATUS_PENDING);
        fields.put("accessCode", null);
        return collection().add(fields).continueWith(task -> task.getResult().getId());
    }

    public Task<List<Event>> getPendingRequestsForPromoter(String promoterUid) {
        return collection()
                .whereEqualTo("promoterId", promoterUid)
                .whereEqualTo("status", Event.STATUS_PENDING)
                .get()
                .continueWith(task -> sortedByCreatedAtDesc(task.getResult()));
    }

    public Task<Void> approveEventRequest(String eventId, String finalPrice, String finalDate,
                                           String finalTime, boolean isPrivate) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("price", finalPrice);
        fields.put("date", finalDate);
        fields.put("time", finalTime);
        fields.put("status", Event.STATUS_PUBLISHED);
        fields.put("isPrivate", isPrivate);
        fields.put("accessCode", generateAccessCode());
        return collection().document(eventId).update(fields);
    }

    public Task<Void> rejectEventRequest(String eventId) {
        return collection().document(eventId).update("status", Event.STATUS_REJECTED);
    }

    public Task<List<Event>> getEventsByOrganizer(String organizerUid) {
        return collection()
                .whereEqualTo("organizerId", organizerUid)
                .get()
                .continueWith(task -> sortedByCreatedAtDesc(task.getResult()));
    }

    public Task<Event> getEventByAccessCode(String code) {
        return collection()
                .whereEqualTo("accessCode", code)
                .whereEqualTo("status", Event.STATUS_PUBLISHED)
                .limit(1)
                .get()
                .continueWith(task -> {
                    QuerySnapshot snap = task.getResult();
                    if (snap == null || snap.isEmpty()) return null;
                    return fromDocument(snap.getDocuments().get(0));
                });
    }

    public Task<Event> getEventById(String eventId) {
        return collection().document(eventId).get().continueWith(task -> {
            DocumentSnapshot doc = task.getResult();
            if (doc == null || !doc.exists()) return null;
            return fromDocument(doc);
        });
    }

    /** Used by the edit-event feature. Only pass the fields that actually changed. */
    public Task<Void> updateEvent(String eventId, Map<String, Object> fields) {
        return collection().document(eventId).update(fields);
    }

    public Task<Void> deleteEvent(String eventId) {
        return collection().document(eventId).delete();
    }

    private Map<String, Object> baseFields(Event draft) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", draft.getTitle());
        map.put("location", draft.getLocation());
        map.put("date", draft.getDate());
        map.put("time", draft.getTime());
        map.put("category", draft.getCategory());
        map.put("price", draft.getPrice());
        map.put("slots", draft.getSlots());
        map.put("description", draft.getDescription());
        map.put("imageUrl", draft.getImageUrl());
        map.put("isPrivate", draft.isPrivate());
        map.put("organizerId", nullToEmpty(draft.getOrganizerId()));
        map.put("organizerName", nullToEmpty(draft.getOrganizerName()));
        map.put("promoterId", nullToEmpty(draft.getPromoterId()));
        map.put("promoterName", nullToEmpty(draft.getPromoterName()));
        map.put("latitude", draft.getLatitude());
        map.put("longitude", draft.getLongitude());
        map.put("createdAt", FieldValue.serverTimestamp());
        return map;
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private Event fromDocument(DocumentSnapshot doc) {
        Double lat = doc.getDouble("latitude");
        Double lng = doc.getDouble("longitude");
        Boolean isPrivate = doc.getBoolean("isPrivate");
        return new Event(
                doc.getId(),
                doc.getString("title"),
                doc.getString("location"),
                doc.getString("date"),
                doc.getString("category"),
                doc.getString("price"),
                Boolean.TRUE.equals(isPrivate),
                doc.getString("imageUrl"),
                doc.getString("time"),
                doc.getString("slots"),
                doc.getString("description"),
                doc.getString("organizerId"),
                doc.getString("organizerName"),
                doc.getString("promoterId"),
                doc.getString("promoterName"),
                doc.getString("status"),
                doc.getString("accessCode"),
                lat != null ? lat : 0,
                lng != null ? lng : 0);
    }

    private List<Event> sortedByCreatedAtDesc(QuerySnapshot snapshot) {
        List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
        Collections.sort(docs, (a, b) -> {
            Timestamp ta = a.getTimestamp("createdAt");
            Timestamp tb = b.getTimestamp("createdAt");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        List<Event> events = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            events.add(fromDocument(doc));
        }
        return events;
    }

    private String generateAccessCode() {
        Random rand = new Random();
        return "EVT-" + String.format("%04d", rand.nextInt(9000) + 1000)
                + "-" + String.format("%04d", rand.nextInt(9000) + 1000);
    }
}
