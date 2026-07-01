package com.example.bookify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.R;
import com.example.bookify.data.Booking;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<Booking> bookings;

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking b = bookings.get(position);
        holder.tvTitle.setText(b.getEventTitle());
        holder.tvDate.setText(b.getEventDate());
        holder.tvCategory.setText(b.getEventCategory());
        holder.tvTicketNumber.setText(b.getTicketNumber());
        holder.tvPrice.setText(b.getEventPrice());

        int bgRes = b.getEventCategory().equalsIgnoreCase("Concert")
                ? R.drawable.bg_card_purple : R.drawable.bg_card_green;
        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCategory, tvTicketNumber, tvPrice;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tv_booking_title);
            tvDate         = itemView.findViewById(R.id.tv_booking_date);
            tvCategory     = itemView.findViewById(R.id.tv_booking_category);
            tvTicketNumber = itemView.findViewById(R.id.tv_booking_ticket);
            tvPrice        = itemView.findViewById(R.id.tv_booking_price);
        }
    }
}