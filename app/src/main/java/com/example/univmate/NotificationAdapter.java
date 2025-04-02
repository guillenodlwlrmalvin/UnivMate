package com.example.univmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final Context context;
    private List<Notification> notifications;
    private final OnNotificationClickListener listener;
    private final SimpleDateFormat dateFormat;

    public NotificationAdapter(Context context, List<Notification> notifications,
                               OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView messageTextView;
        private final TextView timeTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notification_title);
            messageTextView = itemView.findViewById(R.id.notification_message);
            timeTextView = itemView.findViewById(R.id.notification_time);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        public void bind(Notification notification) {
            titleTextView.setText(notification.getTitle());
            messageTextView.setText(notification.getMessage());
            timeTextView.setText(dateFormat.format(notification.getTimestamp()));
        }


    }
}