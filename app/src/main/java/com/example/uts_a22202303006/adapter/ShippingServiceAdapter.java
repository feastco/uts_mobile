package com.example.uts_a22202303006.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202303006.R;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ShippingServiceAdapter extends ListAdapter<Map<String, String>, ShippingServiceAdapter.ViewHolder> {

    private int selectedPosition = -1;
    private final OnServiceSelectedListener listener;

    public interface OnServiceSelectedListener {
        void onServiceSelected(Map<String, String> service);
    }

    public ShippingServiceAdapter(OnServiceSelectedListener listener) {
        super(new ShippingServiceDiffCallback());
        this.listener = listener;
    }

    @Override
    public void submitList(List<Map<String, String>> list) {
        // Reset selected position whenever list changes
        selectedPosition = -1;
        super.submitList(list);
    }

    // Add method to reset selection state
    public void resetSelection() {
        int oldSelectedPosition = selectedPosition;
        selectedPosition = -1;
        if (oldSelectedPosition != -1) {
            notifyItemChanged(oldSelectedPosition);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipping_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> service = getItem(position);

        // Set service details
        holder.tvServiceName.setText(service.get("service") != null ? service.get("service") : "");
        holder.tvDescription.setText(service.get("description") != null ? service.get("description") : "");

        // Format cost as Rupiah
        try {
            double cost = Double.parseDouble(service.get("cost"));
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            holder.tvCost.setText(formatRupiah.format(cost).replace(",00", ""));
        } catch (NumberFormatException e) {
            holder.tvCost.setText("Rp 0");
        }

        // Format estimated delivery time
        String etd = service.get("etd");
        if (etd != null && !etd.isEmpty()) {
            etd = etd.replace(" ", "").replace("hari", "").trim();
            holder.tvEtd.setText(String.format("Estimasi: %s hari", etd));
        } else {
            holder.tvEtd.setText("Estimasi: -");
        }

        // Update selection state
        boolean isSelected = position == selectedPosition;
        holder.rbService.setChecked(isSelected);

        // Style the card based on selection
        if (isSelected) {
            holder.cardView.setStrokeColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.primary));
            holder.cardView.setStrokeWidth(3);
            holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(
                    holder.cardView.getContext(), android.R.color.white)));
            holder.tvCost.setTextColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.primary));
        } else {
            holder.cardView.setStrokeColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.grey_400));
            holder.cardView.setStrokeWidth(1);
            holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(
                    holder.cardView.getContext(), android.R.color.white)));
            holder.tvCost.setTextColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.grey_800));
        }

        // Handle card click
        holder.cardView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.item_selection_anim));

            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            if (selectedPosition != RecyclerView.NO_POSITION) {
                listener.onServiceSelected(getItem(selectedPosition));
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        RadioButton rbService;
        TextView tvServiceName, tvDescription, tvCost, tvEtd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardService);
            rbService = itemView.findViewById(R.id.rbService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCost = itemView.findViewById(R.id.tvCost);
            tvEtd = itemView.findViewById(R.id.tvEtd);
        }
    }

    /**
     * DiffUtil implementation for more efficient updates
     */
    private static class ShippingServiceDiffCallback extends DiffUtil.ItemCallback<Map<String, String>> {
        @Override
        public boolean areItemsTheSame(@NonNull Map<String, String> oldItem, @NonNull Map<String, String> newItem) {
            return Objects.equals(oldItem.get("service"), newItem.get("service")) &&
                    Objects.equals(oldItem.get("description"), newItem.get("description"));
        }

        @Override
        public boolean areContentsTheSame(@NonNull Map<String, String> oldItem, @NonNull Map<String, String> newItem) {
            return Objects.equals(oldItem.get("cost"), newItem.get("cost")) &&
                    Objects.equals(oldItem.get("etd"), newItem.get("etd")) &&
                    Objects.equals(oldItem.get("description"), newItem.get("description"));
        }
    }
}
