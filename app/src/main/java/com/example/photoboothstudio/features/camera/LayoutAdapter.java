package com.example.photoboothstudio.features.camera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoboothstudio.R;
import com.example.photoboothstudio.models.LayoutConfig;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * LayoutAdapter - hiển thị danh sách bố cục chụp ảnh trong CameraActivity.
 */
public class LayoutAdapter extends RecyclerView.Adapter<LayoutAdapter.LayoutViewHolder> {

    public interface OnLayoutSelectedListener {
        void onLayoutSelected(LayoutConfig layout);
    }

    private final List<LayoutConfig> layouts;
    private final OnLayoutSelectedListener listener;
    private int selectedPosition;

    public LayoutAdapter(List<LayoutConfig> layouts, OnLayoutSelectedListener listener) {
        this(layouts, 0, listener);
    }

    public LayoutAdapter(List<LayoutConfig> layouts, int selectedPosition,
                         OnLayoutSelectedListener listener) {
        this.layouts = layouts;
        this.selectedPosition = selectedPosition;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout_config, parent, false);
        return new LayoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LayoutViewHolder holder, int position) {
        LayoutConfig layout = layouts.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(layout.getName());
        holder.imgPreview.setImageResource(layout.getPreviewDrawableRes());

        boolean selected = position == selectedPosition;
        holder.card.setStrokeWidth(dp(context, selected ? 3 : 1));
        holder.card.setStrokeColor(ContextCompat.getColor(context,
                selected ? R.color.coral_pink : R.color.card_stroke_subtle));

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION || currentPosition == selectedPosition) {
                return;
            }
            int oldPosition = selectedPosition;
            selectedPosition = currentPosition;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onLayoutSelected(layouts.get(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return layouts.size();
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static class LayoutViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView imgPreview;
        final TextView tvName;

        LayoutViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardLayout);
            imgPreview = itemView.findViewById(R.id.imgPreview);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
