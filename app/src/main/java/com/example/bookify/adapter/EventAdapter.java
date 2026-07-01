package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.R;
import com.example.bookify.data.Event;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final List<Event> allEvents;
    private List<Event> filteredEvents;
    private OnEventClickListener listener;

    public EventAdapter(List<Event> events) {
        this.allEvents      = new ArrayList<>(events);
        this.filteredEvents = new ArrayList<>(events);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void filter(String category) {
        filteredEvents.clear();
        if (category == null) {
            filteredEvents.addAll(allEvents);
        } else {
            for (Event e : allEvents)
                if (e.getCategory().equalsIgnoreCase(category)) filteredEvents.add(e);
        }
        notifyDataSetChanged();
    }

    public void search(String query) {
        filteredEvents.clear();
        if (query == null || query.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lower = query.toLowerCase();
            for (Event e : allEvents) {
                if (e.getTitle().toLowerCase().contains(lower)
                        || e.getLocation().toLowerCase().contains(lower)
                        || e.getCategory().toLowerCase().contains(lower)) {
                    filteredEvents.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = filteredEvents.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvLocationDate.setText(event.getLocation() + " · " + event.getDate());
        holder.tvCategory.setText(event.getCategory());
        holder.tvPrice.setText(event.getPrice());

        int bgRes = event.getCategory().equalsIgnoreCase("Concert")
                ? R.drawable.bg_card_purple : R.drawable.bg_card_green;
        holder.itemView.setBackgroundResource(bgRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });
    }

    @Override
    public int getItemCount() { return filteredEvents.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocationDate, tvCategory, tvPrice;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tv_event_title);
            tvLocationDate = itemView.findViewById(R.id.tv_event_location_date);
            tvCategory     = itemView.findViewById(R.id.tv_event_category);
            tvPrice        = itemView.findViewById(R.id.tv_event_price);
        }
    }
}