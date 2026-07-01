package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.R;
import com.example.bookify.data.Event;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminViewHolder> {

    private final List<Event> events;
    private final OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public AdminEventAdapter(List<Event> events, OnDeleteClickListener listener) {
        this.events = events;
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
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo;
        Button btnDelete;

        AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_admin_event_title);
            tvInfo  = itemView.findViewById(R.id.tv_admin_event_info);
            btnDelete = itemView.findViewById(R.id.btn_delete_event);
        }
    }
}
