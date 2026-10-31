package com.example.photoboothstudio.features.editor;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photoboothstudio.R;
import com.example.photoboothstudio.models.Frame;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * FrameAdapter - hiển thị danh sách khung ảnh trong EditorActivity.
 * Giai đoạn này khung được lấy từ res/drawable thay vì Firebase Storage.
 */
public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {

    public interface OnFrameSelectedListener {
        void onFrameSelected(Frame frame);
    }

    private final List<Frame> frames;
    private final OnFrameSelectedListener listener;
    private int selectedPosition;

    public FrameAdapter(List<Frame> frames, OnFrameSelectedListener listener) {
        this.frames = frames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_frame, parent, false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
        Frame frame = frames.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(frame.getName());
        Glide.with(context)
                .load(resolveDrawable(context, frame.getImageUrl()))
                .into(holder.imgPreview);

        boolean selected = position == selectedPosition;
        holder.card.setStrokeWidth(dp(context, selected ? 3 : 1));
        holder.card.setStrokeColor(ContextCompat.getColor(context,
                selected ? R.color.coral_pink : R.color.card_stroke_subtle));

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            int oldPosition = selectedPosition;
            selectedPosition = currentPosition;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onFrameSelected(frames.get(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return frames.size();
    }

    private static int resolveDrawable(Context context, String drawableName) {
        if (TextUtils.isEmpty(drawableName)) {
            return R.drawable.ic_frame_none;
        }
        int resId = context.getResources()
                .getIdentifier(drawableName, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.ic_frame_none;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static class FrameViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView imgPreview;
        final TextView tvName;

        FrameViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardFrame);
            imgPreview = itemView.findViewById(R.id.imgFrame);
            tvName = itemView.findViewById(R.id.tvFrameName);
        }
    }
}
