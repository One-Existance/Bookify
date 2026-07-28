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

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();
    private OnEventClickListener listener;
    private String currentCategory = null;

    public EventAdapter(List<Event> events) {
        updateData(events);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Event> events) {
        this.allEvents.clear();
        this.allEvents.addAll(events);
        applyFilter(currentCategory);
    }

    public void filter(String category) {
        this.currentCategory = category;
        applyFilter(category);
    }

    private void applyFilter(String category) {
        filteredEvents.clear();
        if (category == null || category.equalsIgnoreCase("All")) {
            filteredEvents.addAll(allEvents);
        } else {
            for (Event e : allEvents) {
                if (e.getCategory().equalsIgnoreCase(category)) {
                    filteredEvents.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void search(String query) {
        filteredEvents.clear();
        if (query == null || query.isEmpty()) {
            applyFilter(currentCategory);
        } else {
            String lower = query.toLowerCase();
            for (Event e : allEvents) {
                if (e.getTitle().toLowerCase().contains(lower)
                        || e.getLocation().toLowerCase().contains(lower)
                        || e.getCategory().toLowerCase().contains(lower)) {
                    filteredEvents.add(e);
                }
            }
            notifyDataSetChanged();
        }
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

        // Handle image visibility
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            holder.tvIcon.setVisibility(View.GONE);
            try {
                holder.ivImage.setImageURI(android.net.Uri.parse(event.getImageUrl()));
            } catch (Exception e) {
                holder.ivImage.setVisibility(View.GONE);
                holder.tvIcon.setVisibility(View.VISIBLE);
            }
        } else {
            holder.ivImage.setVisibility(View.GONE);
            holder.tvIcon.setVisibility(View.VISIBLE);
            
            String cat = event.getCategory().toLowerCase();
            if (cat.contains("concert")) holder.tvIcon.setImageResource(R.drawable.ic_music_note);
            else if (cat.contains("sports")) holder.tvIcon.setImageResource(R.drawable.ic_sports);
            else if (cat.contains("conference")) holder.tvIcon.setImageResource(R.drawable.ic_mic);
            else if (cat.contains("gala")) holder.tvIcon.setImageResource(R.drawable.ic_star);
            else if (cat.contains("party")) holder.tvIcon.setImageResource(R.drawable.ic_star);
            else holder.tvIcon.setImageResource(R.drawable.ic_building);
        }

        String cat = event.getCategory().toLowerCase();
        int bgRes = R.drawable.bg_card_purple; // Default
        if (cat.contains("sports")) bgRes = R.drawable.bg_card_green;
        else if (cat.contains("conference")) bgRes = R.drawable.bg_card_purple; // Reuse or find new

        holder.itemView.setBackgroundResource(bgRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });
    }

    @Override
    public int getItemCount() { return filteredEvents.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocationDate, tvCategory, tvPrice;
        android.widget.ImageView ivImage, tvIcon;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tv_event_title);
            tvLocationDate = itemView.findViewById(R.id.tv_event_location_date);
            tvCategory     = itemView.findViewById(R.id.tv_event_category);
            tvPrice        = itemView.findViewById(R.id.tv_event_price);
            tvIcon         = itemView.findViewById(R.id.tv_icon_placeholder);
            ivImage        = itemView.findViewById(R.id.iv_event_image);
        }
    }
}
