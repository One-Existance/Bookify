package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.R;
import com.example.bookify.data.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private final OnVerifyClickListener listener;

    public interface OnVerifyClickListener {
        void onVerifyClick(User user);
    }

    public UserAdapter(List<User> users, OnVerifyClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        
        String roleText = "User";
        if (user.isAdmin()) roleText = "Admin";
        else if (user.isPromoter()) {
            roleText = "Promoter" + (user.isVerified() ? " (Verified)" : " (Unverified)");
        }
        holder.tvRole.setText(roleText);

        if (user.isPromoter() && !user.isVerified()) {
            holder.btnVerify.setVisibility(View.VISIBLE);
            holder.btnVerify.setOnClickListener(v -> listener.onVerifyClick(user));
        } else {
            holder.btnVerify.setVisibility(View.GONE);
        }

        // Color coding
        if (user.isAdmin()) holder.roleIndicator.setBackgroundColor(0xFFBB86FC);
        else if (user.isPromoter()) holder.roleIndicator.setBackgroundColor(0xFF03DAC5);
        else holder.roleIndicator.setBackgroundColor(0xFF6200EE);
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        View roleIndicator;
        Button btnVerify;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            roleIndicator = itemView.findViewById(R.id.v_role_indicator);
            btnVerify = itemView.findViewById(R.id.btn_verify_promoter);
        }
    }
}
