package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.R;
import com.example.bookify.data.EventRequest;
import java.util.List;

public class EventRequestAdapter extends RecyclerView.Adapter<EventRequestAdapter.RequestViewHolder> {

    private final List<EventRequest> requests;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(EventRequest request);
        void onReject(EventRequest request);
    }

    public EventRequestAdapter(List<EventRequest> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        EventRequest request = requests.get(position);
        holder.tvTitle.setText(request.getEvent().getTitle()
                + (request.getEvent().isPrivate() ? " 🔒" : " 🌐"));
        holder.tvOrganizer.setText("Requested by " + request.getOrganizerName());
        holder.tvInfo.setText(request.getEvent().getCategory() + " · " + request.getEvent().getDate()
                + " · " + request.getEvent().getTime() + " · " + request.getEvent().getPrice());

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(request));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request));
    }

    @Override
    public int getItemCount() { return requests.size(); }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvOrganizer, tvInfo;
        Button btnAccept, btnReject;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle     = itemView.findViewById(R.id.tv_request_title);
            tvOrganizer = itemView.findViewById(R.id.tv_request_organizer);
            tvInfo      = itemView.findViewById(R.id.tv_request_info);
            btnAccept   = itemView.findViewById(R.id.btn_accept_request);
            btnReject   = itemView.findViewById(R.id.btn_reject_request);
        }
    }
}
