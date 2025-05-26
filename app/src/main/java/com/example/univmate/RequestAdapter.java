package com.example.univmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Request request);
    }

    private Context context;
    private List<Request> requests;
    private final OnItemClickListener listener;

    public RequestAdapter(Context context, @NonNull List<Request> requests,
                          @NonNull OnItemClickListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
    }





    public void updateRequests(@NonNull List<Request> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Request request = requests.get(position);
        holder.bind(request, context);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory, tvStatus, tvUrgencyLevel, tvLocation, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUrgencyLevel = itemView.findViewById(R.id.tvUrgencyLevel);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(Request request, Context context) {
            // Set basic text fields
            tvCategory.setText(request.getCategory());
            tvStatus.setText(request.getStatus());
            tvUrgencyLevel.setText(request.getUrgencyLevel());
            tvLocation.setText(request.getLocation());
            tvDescription.setText(request.getDescription());

            // Set status color
            @ColorRes int statusColor = getStatusColor(request.getStatus());
            tvStatus.setTextColor(ContextCompat.getColor(context, statusColor));

            // Set urgency color
            @ColorRes int urgencyColor = getUrgencyColor(request.getUrgencyLevel());
            tvUrgencyLevel.setTextColor(ContextCompat.getColor(context, urgencyColor));
        }

        private @ColorRes int getStatusColor(String status) {
            if (status == null) return R.color.primary;

            switch (status.toLowerCase()) {
                case "pending": return R.color.warning_yellow;
                case "completed": return R.color.success_green;
                case "rejected": return R.color.error_red;
                default: return R.color.primary;
            }
        }

        private @ColorRes int getUrgencyColor(String urgencyLevel) {
            if (urgencyLevel == null) return R.color.success_green;

            switch (urgencyLevel.toLowerCase()) {
                case "high": return R.color.error_red;
                case "medium": return R.color.warning_yellow;
                default: return R.color.success_green;
            }
        }
    }
}