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
import java.util.Set;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private final Set<Integer> promotersWithHall;
    private final OnVerifyClickListener listener;

    public interface OnVerifyClickListener {
        void onVerifyClick(User user);
    }

    /** promotersWithHall: user ids of PROMOTER-role users who already have an approved
     *  promoter_applications row (hall/location) - anyone PROMOTER but missing from this set
     *  was created through the old admin flow that skipped hall info and still needs it. */
    public UserAdapter(List<User> users, Set<Integer> promotersWithHall, OnVerifyClickListener listener) {
        this.users = users;
        this.promotersWithHall = promotersWithHall;
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
        holder.tvRole.setText(user.getRole());

        boolean promoterMissingHall = user.isPromoter() && !promotersWithHall.contains(user.getId());
        if (user.isUser() || promoterMissingHall) {
            holder.btnVerify.setVisibility(View.VISIBLE);
            holder.btnVerify.setText(promoterMissingHall
                    ? R.string.admin_add_hall_info_button
                    : R.string.admin_make_promoter_button);
            holder.btnVerify.setOnClickListener(v -> listener.onVerifyClick(user));
        } else {
            holder.btnVerify.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        Button btnVerify;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            btnVerify = itemView.findViewById(R.id.btn_verify_user);
        }
    }
}
