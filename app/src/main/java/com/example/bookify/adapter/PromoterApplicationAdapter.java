package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.R;
import com.example.bookify.data.PromoterApplication;
import java.util.List;

public class PromoterApplicationAdapter extends RecyclerView.Adapter<PromoterApplicationAdapter.ViewHolder> {

    private final List<PromoterApplication> applications;
    private final OnApplicationActionListener listener;

    public interface OnApplicationActionListener {
        void onApprove(PromoterApplication application);
        void onReject(PromoterApplication application);
    }

    public PromoterApplicationAdapter(List<PromoterApplication> applications, OnApplicationActionListener listener) {
        this.applications = applications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promoter_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PromoterApplication app = applications.get(position);
        holder.tvHallName.setText(app.getHallName());
        holder.tvApplicant.setText(app.getApplicantName() + " · " + app.getApplicantEmail());
        holder.tvLocation.setText(app.getLocation());

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(app));
        holder.btnReject.setOnClickListener(v -> listener.onReject(app));
    }

    @Override
    public int getItemCount() { return applications.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHallName, tvApplicant, tvLocation;
        Button btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHallName  = itemView.findViewById(R.id.tv_app_hall_name);
            tvApplicant = itemView.findViewById(R.id.tv_app_applicant);
            tvLocation  = itemView.findViewById(R.id.tv_app_location);
            btnApprove  = itemView.findViewById(R.id.btn_approve_app);
            btnReject   = itemView.findViewById(R.id.btn_reject_app);
        }
    }
}
