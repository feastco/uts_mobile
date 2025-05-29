package com.example.uts_a22202303006.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.profile.ShippingAddress;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ShippingAddressAdapter extends RecyclerView.Adapter<ShippingAddressAdapter.ViewHolder> {

    private final List<ShippingAddress> addresses;
    private final Context context;
    private OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onSetDefaultClick(ShippingAddress address);
        void onDeleteClick(ShippingAddress address);
        void onEditClick(ShippingAddress address);
    }



    public void updateData(List<ShippingAddress> newAddresses) {
        addresses.clear();
        addresses.addAll(newAddresses);
        notifyDataSetChanged();
    }

    public ShippingAddressAdapter(Context context, List<ShippingAddress> addresses) {
        this.context = context;
        this.addresses = addresses;
    }

    public void setOnAddressClickListener(OnAddressClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_shipping_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShippingAddress address = addresses.get(position);

        holder.tvRecipientName.setText(address.getNamaPenerima());
        holder.tvPhone.setText(address.getNomorTelepon());
        holder.tvAddress.setText(address.getFullAddress());

        // Set default indicator visibility
        if (address.isDefault()) {
            holder.tvDefaultIndicator.setVisibility(View.VISIBLE);
            holder.btnSetDefault.setVisibility(View.GONE);
        } else {
            holder.tvDefaultIndicator.setVisibility(View.GONE);
            holder.btnSetDefault.setVisibility(View.VISIBLE);
        }

        // Set card highlight if default
        if (address.isDefault()) {
            holder.cardView.setStrokeColor(context.getResources().getColor(R.color.primary));
            holder.cardView.setStrokeWidth(2);
        } else {
            holder.cardView.setStrokeWidth(0);
        }

        // Set click listeners
        holder.btnSetDefault.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSetDefaultClick(address);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(address);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvRecipientName, tvPhone, tvAddress, tvDefaultIndicator;
        MaterialButton btnSetDefault, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAddress);
            tvRecipientName = itemView.findViewById(R.id.tvRecipientName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefaultIndicator = itemView.findViewById(R.id.tvDefaultIndicator);
            btnSetDefault = itemView.findViewById(R.id.btnSetDefault);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}