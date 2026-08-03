package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookify.R;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import com.example.bookify.util.FieldFormatters;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminViewHolder> {

    private final List<Event> events;
    private final DatabaseHelper db;
    private final OnEventActionListener listener;

    public interface OnEventActionListener {
        void onDeleteClick(Event event);
        void onViewClick(Event event);
        void onEditClick(Event event);
        void onShareClick(Event event);
        void onShareWhatsAppClick(Event event);
        void onScanClick(Event event);
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
        int ticketsSold = db.getTicketCount(event.getId(), true);
        holder.tvInfo.setText(event.getLocation() + " | " + event.getDate() + " | " + ticketsSold + " sold");
        holder.tvStatus.setText(event.getStatus() + (event.isPrivate() ? " · Private" : " · Public"));

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImageUrl())
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event));
        holder.btnView.setOnClickListener(v -> listener.onViewClick(event));

        holder.btnEdit.setVisibility(FieldFormatters.isUpcoming(event.getDate(), event.getTime()) ? View.VISIBLE : View.GONE);
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(event));

        boolean canShare = Event.STATUS_PUBLISHED.equals(event.getStatus()) && event.getAccessCode() != null;
        holder.layoutShareActions.setVisibility(canShare ? View.VISIBLE : View.GONE);
        holder.btnShare.setOnClickListener(v -> listener.onShareClick(event));
        holder.btnShareWhatsApp.setOnClickListener(v -> listener.onShareWhatsAppClick(event));

        holder.btnScanEntry.setVisibility(canShare ? View.VISIBLE : View.GONE);
        holder.btnScanEntry.setOnClickListener(v -> listener.onScanClick(event));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo, tvStatus;
        android.widget.ImageView ivImage;
        Button btnDelete, btnView, btnEdit, btnShare, btnShareWhatsApp, btnScanEntry;
        View layoutShareActions;

        AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_admin_event_title);
            tvInfo  = itemView.findViewById(R.id.tv_admin_event_info);
            tvStatus = itemView.findViewById(R.id.tv_admin_event_status);
            ivImage = itemView.findViewById(R.id.iv_admin_event_image);
            btnDelete = itemView.findViewById(R.id.btn_delete_event);
            btnView = itemView.findViewById(R.id.btn_view_event);
            btnEdit = itemView.findViewById(R.id.btn_edit_event);
            layoutShareActions = itemView.findViewById(R.id.layout_admin_share_actions);
            btnShare = itemView.findViewById(R.id.btn_admin_share_code);
            btnShareWhatsApp = itemView.findViewById(R.id.btn_admin_share_whatsapp);
            btnScanEntry = itemView.findViewById(R.id.btn_admin_scan_entry);
        }
    }
}
