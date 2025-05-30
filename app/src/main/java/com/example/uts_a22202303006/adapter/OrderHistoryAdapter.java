package com.example.uts_a22202303006.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.models.Order;
import com.example.uts_a22202303006.models.OrderItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private List<Order> orders;
    private Context context;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderHistoryAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.orders = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderNumber, tvOrderDate, tvOrderStatus, tvShippingInfo, tvTotalPrice;
        private LinearLayout layoutOrderItems;
        private Button btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvShippingInfo = itemView.findViewById(R.id.tvShippingInfo);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            layoutOrderItems = itemView.findViewById(R.id.layoutOrderItems);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(Order order) {
            tvOrderNumber.setText("#" + order.getOrderNumber());
            tvOrderDate.setText(order.getOrderDate());
            
            // Set order status with appropriate background
            String orderStatus = order.getStatus().getOrder();
            tvOrderStatus.setText(orderStatus);
            switch (orderStatus.toLowerCase()) {
                case "pending":
                    tvOrderStatus.setBackgroundResource(R.drawable.status_pending_bg);
                    break;
                case "processing":
                    tvOrderStatus.setBackgroundResource(R.drawable.status_pending_bg);
                    break;
                case "shipped":
                    tvOrderStatus.setBackgroundResource(R.drawable.status_pending_bg);
                    break;
                case "delivered":
                    tvOrderStatus.setBackgroundResource(R.drawable.status_pending_bg);
                    break;
                default:
                    tvOrderStatus.setBackgroundResource(R.drawable.status_pending_bg);
                    break;
            }
            
            // Set shipping info
            String shippingInfo = order.getShipping().getCourier().toUpperCase() + " " + order.getShipping().getService();
            tvShippingInfo.setText(shippingInfo);
            
            // Set total price
            tvTotalPrice.setText(formatRupiah(order.getPayment().getTotal()));
            
            // Clear previous items
            layoutOrderItems.removeAllViews();
            
            // Get order items
            List<OrderItem> items = order.getItems();
            
            // Show first 3 items only, with a limit indicator if there are more
            int itemsToShow = Math.min(items.size(), 3);
            for (int i = 0; i < itemsToShow; i++) {
                OrderItem item = items.get(i);
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_order_product, layoutOrderItems, false);
                
                TextView tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
                TextView tvProductName = itemView.findViewById(R.id.tvProductName);
                TextView tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
                
                tvProductQuantity.setText(item.getQuantity() + "x");
                tvProductName.setText(item.getProductName());
                tvProductPrice.setText(formatRupiah(item.getSubtotal()));
                
                layoutOrderItems.addView(itemView);
            }
            
            // Add "and X more items" if there are more than 3 items
            if (items.size() > 3) {
                View moreItemsView = LayoutInflater.from(context).inflate(R.layout.item_order_product, layoutOrderItems, false);
                
                TextView tvProductQuantity = moreItemsView.findViewById(R.id.tvProductQuantity);
                TextView tvProductName = moreItemsView.findViewById(R.id.tvProductName);
                TextView tvProductPrice = moreItemsView.findViewById(R.id.tvProductPrice);
                
                tvProductQuantity.setText("");
                tvProductName.setText("and " + (items.size() - 3) + " more items");
                tvProductPrice.setText("");
                tvProductName.setTextColor(context.getResources().getColor(R.color.text_secondary));
                
                layoutOrderItems.addView(moreItemsView);
            }
            
            // Set click listener for order details button
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }
        
        private String formatRupiah(double amount) {
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return formatRupiah.format(amount).replace(",00", "");
        }
    }
}
