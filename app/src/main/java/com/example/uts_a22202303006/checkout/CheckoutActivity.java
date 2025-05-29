package com.example.uts_a22202303006.checkout;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.adapter.ShippingServiceAdapter;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.databinding.ActivityCheckoutBinding;
import com.example.uts_a22202303006.product.Product;
import com.example.uts_a22202303006.profile.ShippingAddress;
import com.example.uts_a22202303006.profile.ShippingAddressActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private double cartTotal;
    private double shippingCost = 0;
    private int userId;
    private int addressId = -1;

    // Address information
    private String recipientName;
    private String phoneNumber;
    private String fullAddress;
    private int provinceId = -1;
    private String provinceName;
    private int cityId = -1;
    private String cityName;
    private String postalCode;

    // Shipping information
    private String selectedCourier;
    private String selectedService;


    // For shipping services
    private List<Map<String, String>> shippingServices = new ArrayList<>();
    private ShippingServiceAdapter shippingServiceAdapter;
    // For address selection
    private ShippingAddress defaultAddress;

    // Activity result launcher for address selection
    private final ActivityResultLauncher<Intent> addressSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh address after returning from address selection
                    loadDefaultAddress();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Checkout");

        // Get cart total from intent
        cartTotal = getIntent().getDoubleExtra("CART_TOTAL", 0);
        binding.tvProductTotal.setText(formatRupiah(cartTotal));

        // Get user ID from shared preferences
        SharedPreferences userPrefs = getSharedPreferences("login_session", MODE_PRIVATE);
        userId = userPrefs.getInt("id", -1);

        // Setup shipping services RecyclerView
        shippingServiceAdapter = new ShippingServiceAdapter(this::selectShippingService);
        binding.recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewServices.setAdapter(shippingServiceAdapter);

        // Setup swipe refresh
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshCheckoutData);

        // Setup address selection
        setupAddressSelection();

        // Setup courier selection
        setupCourierSelection();

        // Setup checkout button
        binding.btnProcessOrder.setOnClickListener(v -> processOrder());

        // Update order summary
        updateOrderSummary();

        // Load default address
        loadDefaultAddress();
    }

    private void refreshCheckoutData() {
        // Reload default address
        loadDefaultAddress();

        // Reset and setup courier selection
        setupCourierSelection();

        // If a courier is already selected, reload shipping services
        if (selectedCourier != null && !selectedCourier.isEmpty()) {
            loadShippingCost();
        }

        // Recalculate totals
        updateOrderSummary();

        // Stop the refresh animation
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void loadShippingCost() {
        if (selectedCourier != null && !selectedCourier.isEmpty()) {
            calculateShippingCost(selectedCourier, calculateTotalWeight());
        }
    }

    private void setupAddressSelection() {
        // Show "No address" text initially
        binding.tvNoAddress.setVisibility(View.VISIBLE);

        // Setup address selection button to navigate to ShippingAddressActivity
        binding.btnChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, ShippingAddressActivity.class);
            addressSelectionLauncher.launch(intent);
        });
    }

    private void loadDefaultAddress() {
        // Show loading indicator
        binding.addressLoadingProgress.setVisibility(View.VISIBLE);
        binding.tvNoAddress.setVisibility(View.GONE);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getShippingAddresses(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.addressLoadingProgress.setVisibility(View.GONE);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        if (jsonObject.has("status") && jsonObject.get("status").getAsBoolean()) {
                            if (jsonObject.has("data")) {
                                Gson gson = new Gson();
                                Type listType = new TypeToken<List<ShippingAddress>>(){}.getType();
                                List<ShippingAddress> addresses = gson.fromJson(jsonObject.get("data"), listType);

                                // Find the default address
                                for (ShippingAddress address : addresses) {
                                    if (address.isDefault()) {
                                        defaultAddress = address;
                                        break;
                                    }
                                }

                                // Update UI with the new default address
                                updateAddressUI(defaultAddress);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading default address", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.addressLoadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Network error loading address", t);
            }
        });
    }

    private void updateAddressUI(ShippingAddress address) {
        // Store address information for order processing
        addressId = address.getId();
        recipientName = address.getNamaPenerima();
        phoneNumber = address.getNomorTelepon();
        fullAddress = address.getAlamatLengkap();
        provinceId = address.getProvinceId();
        provinceName = address.getProvince();
        cityId = address.getCityId();
        cityName = address.getCity();
        postalCode = address.getKodePos();

        // Update UI
        binding.tvNoAddress.setVisibility(View.GONE);
        binding.recyclerViewAddresses.setVisibility(View.VISIBLE);

        // Create address card view programmatically (simplified version)
        View addressView = getLayoutInflater().inflate(R.layout.item_checkout_address, binding.recyclerViewAddresses, false);
        TextView tvName = addressView.findViewById(R.id.tvRecipientName);
        TextView tvPhone = addressView.findViewById(R.id.tvPhone);
        TextView tvAddress = addressView.findViewById(R.id.tvAddress);
        TextView tvDefaultIndicator = addressView.findViewById(R.id.tvDefaultIndicator);

        tvName.setText(address.getNamaPenerima());
        tvPhone.setText(address.getNomorTelepon());
        tvAddress.setText(address.getFullAddress());

        // Show default indicator if this is the default address
        if (address.isDefault()) {
            tvDefaultIndicator.setVisibility(View.VISIBLE);
        } else {
            tvDefaultIndicator.setVisibility(View.GONE);
        }

        binding.recyclerViewAddresses.removeAllViews();
        binding.recyclerViewAddresses.addView(addressView);
    }

    private void showNoAddressView() {
        binding.tvNoAddress.setVisibility(View.VISIBLE);
        binding.recyclerViewAddresses.setVisibility(View.GONE);

        // Reset address information
        addressId = -1;
        recipientName = null;
        phoneNumber = null;
        fullAddress = null;
        provinceId = -1;
        provinceName = null;
        cityId = -1;
        cityName = null;
        postalCode = null;
    }

    private String setupCourierSelection() {

        selectedCourier = null;
        selectedService = null;

        // Original courier selection code remains unchanged
        binding.radioGroupCourier.setOnCheckedChangeListener((group, checkedId) -> {
            // Clear previous shipping services
            shippingServices.clear();
            shippingServiceAdapter.submitList(null);
            binding.servicesCardView.setVisibility(View.GONE);
            shippingCost = 0;
            updateOrderSummary();

            // Check if we have a valid address
            if (cityId == -1) {
                Toasty.warning(this, "Please select a shipping address first", Toast.LENGTH_SHORT, true).show();
                binding.radioGroupCourier.clearCheck();
                return;
            }

            // Determine selected courier code
            if (checkedId == R.id.rbJne) {
                selectedCourier = "jne";
            } else if (checkedId == R.id.rbTiki) {
                selectedCourier = "tiki";
            } else if (checkedId == R.id.rbPos) {
                selectedCourier = "pos";
            }

            // Calculate shipping cost with actual origin and destination
            calculateShippingCost(selectedCourier, 501); // 501g weight
        });
        return selectedCourier;
    }

    private void calculateShippingCost(String courier, int weight) {
        binding.progressBar.setVisibility(View.VISIBLE);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getShippingCost(501, cityId, weight, courier).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        // Check if rajaongkir response exists and has valid status
                        if (jsonObject != null && jsonObject.has("rajaongkir")) {
                            JsonObject rajaongkir = jsonObject.getAsJsonObject("rajaongkir");
                            JsonObject status = rajaongkir.getAsJsonObject("status");

                            if (status.get("code").getAsInt() == 200) {
                                shippingServices.clear();
                                JsonArray results = rajaongkir.getAsJsonArray("results");

                                if (results != null && results.size() > 0) {
                                    JsonObject courierObject = results.get(0).getAsJsonObject();
                                    JsonArray costs = courierObject.getAsJsonArray("costs");

                                    for (int i = 0; i < costs.size(); i++) {
                                        JsonObject serviceObject = costs.get(i).getAsJsonObject();
                                        JsonArray costArray = serviceObject.getAsJsonArray("cost");
                                        JsonObject costDetail = costArray.get(0).getAsJsonObject();

                                        Map<String, String> service = new HashMap<>();
                                        service.put("service", serviceObject.get("service").getAsString());
                                        service.put("description", serviceObject.get("description").getAsString());
                                        service.put("cost", costDetail.get("value").getAsString());
                                        service.put("etd", costDetail.get("etd").getAsString());

                                        shippingServices.add(service);
                                    }

                                    // Update UI with shipping services
                                    shippingServiceAdapter.submitList(new ArrayList<>(shippingServices));
                                    binding.servicesCardView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Toasty.error(CheckoutActivity.this,
                                        "Error: " + status.get("description").getAsString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ShippingCost", "JSON parsing error", e);
                        Toasty.error(CheckoutActivity.this,
                                "Error parsing shipping data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toasty.error(CheckoutActivity.this,
                            "Failed to calculate shipping costs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e("ShippingCost", "Network error", t);
                Toasty.error(CheckoutActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectShippingService(Map<String, String> service) {
        selectedService = service.get("service");
        try {
            shippingCost = Double.parseDouble(service.get("cost"));
            updateOrderSummary();
        } catch (NumberFormatException e) {
            Log.e("Checkout", "Error parsing shipping cost", e);
        }
    }

    private void updateOrderSummary() {
        binding.tvProductTotal.setText(formatRupiah(cartTotal));
        binding.tvShippingCost.setText(formatRupiah(shippingCost));
        double grandTotal = cartTotal + shippingCost;
        binding.tvGrandTotal.setText(formatRupiah(grandTotal));
    }

    private String formatRupiah(double amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatRupiah.format(amount).replace(",00", "");
    }

    private void processOrder() {
        // Disable the button to prevent multiple submissions
        binding.btnProcessOrder.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Check if user is guest or has invalid session
        if (userId == -1) {
            Toasty.warning(this, "Please login to checkout", Toast.LENGTH_SHORT, true).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnProcessOrder.setEnabled(true);
            return;
        }

        // Validate shipping address
        if (addressId == -1) {
            Toasty.warning(this, "Please select a shipping address", Toast.LENGTH_SHORT, true).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnProcessOrder.setEnabled(true);
            return;
        }

        // Validate shipping method
        if (selectedCourier == null || selectedCourier.isEmpty()) {
            Toasty.warning(this, "Please select a courier", Toast.LENGTH_SHORT, true).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnProcessOrder.setEnabled(true);
            return;
        }

        // Validate shipping service
        if (selectedService == null || selectedService.isEmpty()) {
            Toasty.warning(this, "Please select a shipping service", Toast.LENGTH_SHORT, true).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnProcessOrder.setEnabled(true);
            return;
        }

        // Load cart items for checkout
        ArrayList<Product> cartItems = getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            Toasty.warning(this, "Cart is empty", Toast.LENGTH_SHORT, true).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnProcessOrder.setEnabled(true);
            return;
        }

        // Format cart data as JSON array for API
        JsonArray cartArray = new JsonArray();
        for (Product item : cartItems) {
            JsonObject productObj = new JsonObject();
            productObj.addProperty("product_id", item.getKode());
            productObj.addProperty("product_code", item.getKode());
            productObj.addProperty("product_name", item.getMerk());
            productObj.addProperty("price", item.getHargaJual());
            productObj.addProperty("quantity", item.getQty());
            productObj.addProperty("subtotal", item.getHargaJual() * item.getQty());
            cartArray.add(productObj);
        }
        String cartJson = cartArray.toString();

        // Calculate grand total
        double grandTotal = cartTotal + shippingCost;

        // Make API call with the actual address ID
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.processCheckout(
                userId,
                addressId, // Use the real address ID from selected address
                cartTotal,
                shippingCost,
                grandTotal,
                selectedCourier,
                selectedService,
                cartJson
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnProcessOrder.setEnabled(true);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("CheckoutDebug", "Response: " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("status")) {
                            // Order successful
                            clearCart();
                            showSuccessDialog(jsonResponse.getJSONObject("order").getString("order_number"));
                        } else {
                            // Order failed
                            Toasty.error(CheckoutActivity.this, jsonResponse.getString("message"), Toast.LENGTH_LONG, true).show();
                        }
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.d("CheckoutDebug", "Error: " + errorBody);
                        Toasty.error(CheckoutActivity.this, "Failed to process order", Toast.LENGTH_LONG, true).show();
                    }
                } catch (Exception e) {
                    Log.e("CheckoutDebug", "Error parsing response", e);
                    Toasty.error(CheckoutActivity.this, "Error processing response", Toast.LENGTH_LONG, true).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnProcessOrder.setEnabled(true);
                Log.e("CheckoutDebug", "API call failed", t);
                Toasty.error(CheckoutActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG, true).show();
            }
        });
    }

    // Helper method to get cart items
    private ArrayList<Product> getCartItems() {
        SharedPreferences sharedPreferences = getSharedPreferences("product", MODE_PRIVATE);
        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            return gson.fromJson(jsonText, new TypeToken<ArrayList<Product>>(){}.getType());
        }
        return new ArrayList<>();
    }

    private void showSuccessDialog(String orderNumber) {
        // Create dialog with custom view
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_order_success);
        dialog.setCancelable(false);

        // Set window properties
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Set order number
        TextView tvOrderNumber = dialog.findViewById(R.id.tvOrderNumber);
        tvOrderNumber.setText(orderNumber);

        // Clear cart after successful order
        clearCart();

        // Set button click listeners
        Button btnContinueShopping = dialog.findViewById(R.id.btnContinueShopping);
        btnContinueShopping.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        Button btnViewOrders = dialog.findViewById(R.id.btnViewOrders);
        btnViewOrders.setOnClickListener(v -> {
            // Implement navigation to orders screen if available
            // For now, just dismiss and finish this activity
            dialog.dismiss();
            finish();
        });

        // Show dialog with animation
        dialog.show();

        // Add simple animation to dialog content
        View dialogContent = dialog.findViewById(android.R.id.content);
        if (dialogContent != null) {
            dialogContent.setAlpha(0f);
            dialogContent.animate().alpha(1f).setDuration(400).start();
        }
    }

    private void clearCart() {
        // Clear the cart in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("product", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("listproduct", "[]"); // Empty array
        editor.apply();
    }

    private ArrayList<Product> getCartItemsFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("product", MODE_PRIVATE);
        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            return gson.fromJson(jsonText, new TypeToken<ArrayList<Product>>(){}.getType());
        }
        return new ArrayList<>();
    }

    private int calculateTotalWeight() {
        // Get cart items and calculate total weight
        SharedPreferences sharedPreferences = getSharedPreferences("product", MODE_PRIVATE);
        String jsonText = sharedPreferences.getString("listproduct", null);

        if (jsonText == null || jsonText.isEmpty()) {
            return 1000; // Default 1kg
        }

        Gson gson = new Gson();
        ArrayList<Product> cartItems = gson.fromJson(jsonText,
                new TypeToken<ArrayList<Product>>(){}.getType());

        int totalWeight = 0;
        for (Product item : cartItems) {
            // Assume each product weighs 250g, multiply by quantity
            totalWeight += 250 * item.getQty();
        }

        // Ensure minimum weight is 1000g (1kg)
        return Math.max(totalWeight, 1000);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}