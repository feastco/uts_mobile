package com.example.uts_a22202303006.orders;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.databinding.ActivityOrderDetailBinding;
import com.example.uts_a22202303006.models.Order;
import com.example.uts_a22202303006.models.OrderItem;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order Details");

        // Get order data from intent
        String orderJson = getIntent().getStringExtra("order_data");
        if (!TextUtils.isEmpty(orderJson)) {
            order = new Gson().fromJson(orderJson, Order.class);
            displayOrderDetails();
        } else {
            finish(); // Close activity if no data
        }
    }

    private void displayOrderDetails() {
        // Order info
        binding.tvOrderNumber.setText("#" + order.getOrderNumber());
        binding.tvOrderDate.setText(order.getOrderDate());
        binding.tvOrderStatus.setText(order.getStatus().getOrder());
        binding.tvPaymentStatus.setText(order.getStatus().getPayment());

        // Set shipping address
        String address = order.getShipping().getFormattedAddress();
        String recipient = order.getShipping().getName() + " | " + order.getShipping().getPhone();
        binding.tvRecipientInfo.setText(recipient);
        binding.tvAddress.setText(address);

        // Set shipping method
        String shippingMethod = order.getShipping().getCourier().toUpperCase() + " " + 
                order.getShipping().getService();
        binding.tvShippingMethod.setText(shippingMethod);

        // Setup order items
        binding.layoutOrderItems.removeAllViews();
        for (OrderItem item : order.getItems()) {
            View itemView = LayoutInflater.from(this).inflate(
                    R.layout.item_order_product, binding.layoutOrderItems, false);
            
            TextView tvQuantity = itemView.findViewById(R.id.tvProductQuantity);
            TextView tvName = itemView.findViewById(R.id.tvProductName);
            TextView tvPrice = itemView.findViewById(R.id.tvProductPrice);
            
            tvQuantity.setText(item.getQuantity() + "x");
            tvName.setText(item.getProductName());
            tvPrice.setText(formatRupiah(item.getSubtotal()));
            
            binding.layoutOrderItems.addView(itemView);
        }

        // Set payment summary
        binding.tvSubtotal.setText(formatRupiah(order.getPayment().getSubtotal()));
        binding.tvShippingCost.setText(formatRupiah(order.getPayment().getShipping()));
        binding.tvTotalPrice.setText(formatRupiah(order.getPayment().getTotal()));
    }

    private String formatRupiah(double amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatRupiah.format(amount).replace(",00", "");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
