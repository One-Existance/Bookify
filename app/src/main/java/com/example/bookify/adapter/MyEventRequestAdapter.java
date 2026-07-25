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

public class MyEventRequestAdapter extends RecyclerView.Adapter<MyEventRequestAdapter.ViewHolder> {

    private final List<Event> events;
    private final OnShareClickListener listener;

    public interface OnShareClickListener {
        void onShareClick(Event event);
    }

    public MyEventRequestAdapter(List<Event> events, OnShareClickListener listener) {
        this.events = events;
        this.listener = listener;
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
                && event.isPrivate() && event.getAccessCode() != null;
        holder.btnShare.setVisibility(canShare ? View.VISIBLE : View.GONE);
        holder.btnShare.setOnClickListener(v -> listener.onShareClick(event));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo, tvStatus;
        Button btnShare;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tv_my_request_title);
            tvInfo   = itemView.findViewById(R.id.tv_my_request_info);
            tvStatus = itemView.findViewById(R.id.tv_my_request_status);
            btnShare = itemView.findViewById(R.id.btn_share_code);
        }
    }
}
