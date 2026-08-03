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
import com.example.bookify.util.FieldFormatters;
import java.util.List;

public class MyEventRequestAdapter extends RecyclerView.Adapter<MyEventRequestAdapter.ViewHolder> {

    private final List<Event> events;
    private final OnShareClickListener shareListener;
    private final OnShareClickListener whatsAppListener;
    private final OnShareClickListener scanListener;
    private final OnShareClickListener editListener;

    public interface OnShareClickListener {
        void onShareClick(Event event);
    }

    public MyEventRequestAdapter(List<Event> events, OnShareClickListener shareListener,
                                  OnShareClickListener whatsAppListener, OnShareClickListener scanListener,
                                  OnShareClickListener editListener) {
        this.events = events;
        this.shareListener = shareListener;
        this.whatsAppListener = whatsAppListener;
        this.scanListener = scanListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_event_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle() + (event.isPrivate() ? " 🔒" : " 🌐"));
        holder.tvInfo.setText(event.getCategory() + " · " + event.getDate() + " · " + event.getPrice());
        holder.tvStatus.setText(event.getStatus());

        boolean canShare = Event.STATUS_PUBLISHED.equals(event.getStatus())
                && event.getAccessCode() != null;
        holder.layoutShareActions.setVisibility(canShare ? View.VISIBLE : View.GONE);
        holder.btnShare.setOnClickListener(v -> shareListener.onShareClick(event));
        holder.btnShareWhatsApp.setOnClickListener(v -> whatsAppListener.onShareClick(event));

        holder.btnScanEntry.setVisibility(canShare ? View.VISIBLE : View.GONE);
        holder.btnScanEntry.setOnClickListener(v -> scanListener.onShareClick(event));

        boolean editable = FieldFormatters.isUpcoming(event.getDate(), event.getTime());
        holder.btnEdit.setVisibility(editable ? View.VISIBLE : View.GONE);
        holder.btnEdit.setOnClickListener(v -> editListener.onShareClick(event));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo, tvStatus;
        View layoutShareActions;
        Button btnShare, btnShareWhatsApp, btnScanEntry, btnEdit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tv_my_request_title);
            tvInfo   = itemView.findViewById(R.id.tv_my_request_info);
            tvStatus = itemView.findViewById(R.id.tv_my_request_status);
            layoutShareActions = itemView.findViewById(R.id.layout_share_actions);
            btnShare = itemView.findViewById(R.id.btn_share_code);
            btnShareWhatsApp = itemView.findViewById(R.id.btn_share_whatsapp);
            btnScanEntry = itemView.findViewById(R.id.btn_scan_entry);
            btnEdit = itemView.findViewById(R.id.btn_edit_request);
        }
    }
}
