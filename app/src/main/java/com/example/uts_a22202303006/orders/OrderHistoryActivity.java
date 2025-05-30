package com.example.uts_a22202303006.orders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.uts_a22202303006.adapter.OrderHistoryAdapter;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.databinding.ActivityOrderHistoryBinding;
import com.example.uts_a22202303006.models.Order;
import com.example.uts_a22202303006.models.OrderItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity implements OrderHistoryAdapter.OnOrderClickListener {
    
    private static final String TAG = "OrderHistoryActivity";
    private ActivityOrderHistoryBinding binding;
    private OrderHistoryAdapter adapter;
    private int userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order History");
        
        // Get user ID from shared preferences
        SharedPreferences userPrefs = getSharedPreferences("login_session", MODE_PRIVATE);
        userId = userPrefs.getInt("id", -1);
        
        // Setup RecyclerView
        adapter = new OrderHistoryAdapter(this, this);
        binding.recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewOrders.setAdapter(adapter);
        
        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadOrderHistory);
        
        // Load order history
        loadOrderHistory();
    }
    
    private void loadOrderHistory() {
        // Check if user is logged in
        if (userId == -1) {
            showNoOrders("Please login to view your orders");
            binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }
        
        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoOrders.setVisibility(View.GONE);
        binding.recyclerViewOrders.setVisibility(View.GONE);
        
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getOrderHistory(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "API Response: " + jsonResponse);
                        
                        // Parse JSON manually to ensure field mapping is correct
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                        
                        if (jsonObject.has("status") && jsonObject.get("status").getAsBoolean()) {
                            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                                JsonArray ordersArray = jsonObject.getAsJsonArray("data");
                                List<Order> orders = new ArrayList<>();
                                
                                // Parse each order manually
                                for (int i = 0; i < ordersArray.size(); i++) {
                                    JsonObject orderJson = ordersArray.get(i).getAsJsonObject();
                                    
                                    // Create a new Order object
                                    Order order = new Order();
                                    
                                    // Set basic info
                                    order.setId(orderJson.get("id").getAsInt());
                                    order.setOrderNumber(orderJson.get("order_number").getAsString());
                                    order.setOrderDate(orderJson.get("order_date").getAsString());
                                    
                                    // Log the order number and date to debug
                                    Log.d(TAG, "Order #" + order.getOrderNumber() + " date: " + order.getOrderDate());
                                    
                                    // Parse status
                                    Order.Status status = new Order.Status();
                                    JsonObject statusJson = orderJson.getAsJsonObject("status");
                                    status.setOrder(statusJson.get("order").getAsString());
                                    status.setPayment(statusJson.get("payment").getAsString());
                                    order.setStatus(status);
                                    
                                    // Parse shipping
                                    Order.Shipping shipping = new Order.Shipping();
                                    JsonObject shippingJson = orderJson.getAsJsonObject("shipping");
                                    shipping.setName(shippingJson.get("name").getAsString());
                                    shipping.setPhone(shippingJson.get("phone").getAsString());
                                    shipping.setAddress(shippingJson.get("address").getAsString());
                                    shipping.setProvince(shippingJson.get("province").getAsString());
                                    shipping.setCity(shippingJson.get("city").getAsString());
                                    shipping.setPostalCode(shippingJson.get("postal_code").getAsString());
                                    shipping.setCourier(shippingJson.get("courier").getAsString());
                                    shipping.setService(shippingJson.get("service").getAsString());
                                    shipping.setCost(shippingJson.get("cost").getAsDouble());
                                    order.setShipping(shipping);
                                    
                                    // Parse payment
                                    Order.Payment payment = new Order.Payment();
                                    JsonObject paymentJson = orderJson.getAsJsonObject("payment");
                                    payment.setSubtotal(paymentJson.get("subtotal").getAsDouble());
                                    payment.setShipping(paymentJson.get("shipping").getAsDouble());
                                    payment.setTotal(paymentJson.get("total").getAsDouble());
                                    order.setPayment(payment);
                                    
                                    // Parse items
                                    List<OrderItem> items = new ArrayList<>();
                                    JsonArray itemsArray = orderJson.getAsJsonArray("items");
                                    for (int j = 0; j < itemsArray.size(); j++) {
                                        JsonObject itemJson = itemsArray.get(j).getAsJsonObject();
                                        OrderItem item = new OrderItem();
                                        item.setId(itemJson.get("id").getAsInt());
                                        item.setProductCode(itemJson.get("product_code").getAsString());
                                        item.setProductName(itemJson.get("product_name").getAsString());
                                        item.setQuantity(itemJson.get("quantity").getAsInt());
                                        item.setPrice(itemJson.get("price").getAsDouble());
                                        item.setSubtotal(itemJson.get("subtotal").getAsDouble());
                                        items.add(item);
                                    }
                                    order.setItems(items);
                                    
                                    orders.add(order);
                                }
                                
                                if (orders.isEmpty()) {
                                    showNoOrders("You haven't placed any orders yet");
                                } else {
                                    binding.recyclerViewOrders.setVisibility(View.VISIBLE);
                                    adapter.setOrders(orders);
                                }
                            } else {
                                showNoOrders("You haven't placed any orders yet");
                            }
                        } else {
                            String message = jsonObject.has("message") ? 
                                    jsonObject.get("message").getAsString() : 
                                    "Failed to load orders";
                            showNoOrders(message);
                        }
                    } else {
                        showNoOrders("Error loading orders");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing order data", e);
                    showNoOrders("Error parsing order data: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                
                Log.e(TAG, "Network error loading orders", t);
                showNoOrders("Network error: " + t.getMessage());
                Toasty.error(OrderHistoryActivity.this, 
                        "Network error: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
//    // Helper class for API response parsing
//    private static class ApiResponse {
//        private boolean status;
//        private String message;
//        private List<Order> data;
//
//        public boolean isStatus() {
//            return status;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//
//        public List<Order> getData() {
//            return data != null ? data : new ArrayList<>();
//        }
//    }
    
    private void showNoOrders(String message) {
        binding.recyclerViewOrders.setVisibility(View.GONE);
        binding.tvNoOrders.setVisibility(View.VISIBLE);
        binding.tvNoOrders.setText(message);
    }
    
    @Override
    public void onOrderClick(Order order) {
        // Navigate to order detail screen
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_data", new Gson().toJson(order));
        startActivity(intent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
