package com.example.bookify.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.R;
import com.example.bookify.data.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
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
        Event event = events.get(position);
        holder.title.setText(event.title);
        holder.location.setText(event.location + " · " + event.date);
        holder.category.setText(event.category);
        holder.price.setText(event.price);

        try {
            int color = Color.parseColor("#" + event.cardColorHex);
            holder.card.setCardBackgroundColor(color);
        } catch (Exception e) {
            holder.card.setCardBackgroundColor(Color.parseColor("#251F5C"));
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView title, location, category, price;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardEvent);
            title = itemView.findViewById(R.id.textEventTitle);
            location = itemView.findViewById(R.id.textEventLocation);
            category = itemView.findViewById(R.id.textEventCategory);
            price = itemView.findViewById(R.id.textEventPrice);
        }
    }
}
