package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.R;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminViewHolder> {

    private final List<Event> events;
    private final DatabaseHelper db;
    private final OnEventActionListener listener;

    public interface OnEventActionListener {
        void onDeleteClick(Event event);
        void onViewClick(Event event);
    }

    public AdminEventAdapter(List<Event> events, DatabaseHelper db, OnEventActionListener listener) {
        this.events = events;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvInfo.setText(event.getLocation() + " | " + event.getDate());

        int taken = db.getTicketCount(event.getId(), true);
        int total = 0;
        try {
            total = Integer.parseInt(event.getSlots());
        } catch (Exception ignored) {}
        int left = Math.max(0, total - taken);
        holder.tvStats.setText("Tickets: " + taken + " taken | " + left + " left");
        
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                holder.ivImage.setImageURI(android.net.Uri.parse(event.getImageUrl()));
            } catch (Exception e) {
                holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event));
        holder.itemView.setOnClickListener(v -> listener.onViewClick(event));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo, tvStats;
        android.widget.ImageView ivImage;
        Button btnDelete;

        AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_admin_event_title);
            tvInfo  = itemView.findViewById(R.id.tv_admin_event_info);
            tvStats = itemView.findViewById(R.id.tv_admin_event_stats);
            ivImage = itemView.findViewById(R.id.iv_admin_event_image);
            btnDelete = itemView.findViewById(R.id.btn_delete_event);
        }
    }
}
