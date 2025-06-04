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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.adapter.CheckoutProductAdapter;
import com.example.uts_a22202303006.adapter.ShippingServiceAdapter;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.databinding.ActivityCheckoutBinding;
import com.example.uts_a22202303006.orders.OrderHistoryActivity;
import com.example.uts_a22202303006.product.Product;
import com.example.uts_a22202303006.profile.ShippingAddress;
import com.example.uts_a22202303006.profile.ShippingAddressActivity;
import com.example.uts_a22202303006.utils.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    private String selectedPaymentMethod = null; // No default payment method
    private String selectedDeliveryTime = ""; // Add this missing variable declaration

    private int originId = AppConfig.STORE_CITY_ID; // Use the city ID from centralized config

    // For shipping services
    private List<Map<String, String>> shippingServices = new ArrayList<>();
    private ShippingServiceAdapter shippingServiceAdapter;
    // For address selection
    private ShippingAddress defaultAddress;

    // Add this new variable to track available couriers
    private List<String> availableCouriers = new ArrayList<>();

    // Activity result launcher for address selection
    private final ActivityResultLauncher<Intent> addressSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Always refresh when returning from address selection, regardless of result code
                binding.swipeRefreshLayout.setRefreshing(true);
                binding.servicesCardView.setVisibility(View.GONE);

                // Give UI time to update
                binding.getRoot().post(() -> {
                    // Complete refresh of checkout data
                    resetShippingSelection();
                    loadDefaultAddress();
                    updateOrderSummary();

                    binding.swipeRefreshLayout.setRefreshing(false);
                });
            });

    // For products
    private CheckoutProductAdapter productAdapter;

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

        // Setup products RecyclerView
        productAdapter = new CheckoutProductAdapter(this);
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProducts.setAdapter(productAdapter);

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

        // Setup payment method selection
        setupPaymentMethodSelection();

        // Setup checkout button
        binding.btnProcessOrder.setOnClickListener(v -> processOrder());

        // Update order summary
        updateOrderSummary();

        // Load default address
        loadDefaultAddress();

        // Load cart items
        loadProductsFromCart();
    }

    private void refreshCheckoutData() {
        // Pertama-tama sembunyikan CardView shipping service
        binding.servicesCardView.setVisibility(View.GONE);

        // Reset semua pilihan shipping terlebih dahulu
        resetShippingSelection();

        // Reload default address
        loadDefaultAddress();

        // Reset dan setup courier selection
        setupCourierSelection();

        // Reload products
        loadProductsFromCart();

        // Recalculate totals
        updateOrderSummary();

        // Stop the refresh animation
        binding.swipeRefreshLayout.setRefreshing(false);

        // Clear payment method selection
        binding.radioGroupPayment.clearCheck();
        selectedPaymentMethod = null;
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

                                // Reset shipping method, service, and cost setelah address berubah
                                resetShippingSelection();
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
        
        // After updating address, check which couriers are available
        checkAvailableCouriers();
    }

    // Add new method to check which couriers are available for the selected city
    private void checkAvailableCouriers() {
        // Reset available couriers
        availableCouriers.clear();
        
        // Hide all courier options initially
        binding.rbJne.setVisibility(View.GONE);
        binding.rbTiki.setVisibility(View.GONE);
        binding.rbPos.setVisibility(View.GONE);
        
        // Show loading indicator
        binding.courierLoadingProgress.setVisibility(View.VISIBLE);
        
        // If cityId is invalid, don't proceed
        if (cityId <= 0) {
            binding.courierLoadingProgress.setVisibility(View.GONE);
            return;
        }
        
        // Minimum weight for checking
        int minWeight = 1000; // 1kg
        
        // List of couriers to check
        List<String> couriersToCheck = Arrays.asList("jne", "tiki", "pos");
        AtomicInteger pendingRequests = new AtomicInteger(couriersToCheck.size());
        
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        
        // Check each courier
        for (String courier : couriersToCheck) {
            apiService.getShippingCost(AppConfig.STORE_CITY_ID, cityId, minWeight, courier)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String jsonResponse = response.body().string();
                                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                                
                                if (jsonObject != null && jsonObject.has("rajaongkir")) {
                                    JsonObject rajaongkir = jsonObject.getAsJsonObject("rajaongkir");
                                    JsonObject status = rajaongkir.getAsJsonObject("status");
                                    
                                    if (status.get("code").getAsInt() == 200) {
                                        JsonArray results = rajaongkir.getAsJsonArray("results");
                                        
                                        if (results != null && results.size() > 0) {
                                            JsonObject courierObject = results.get(0).getAsJsonObject();
                                            JsonArray costs = courierObject.getAsJsonArray("costs");
                                            
                                            // If courier has any costs/services, consider it available
                                            if (costs != null && costs.size() > 0) {
                                                availableCouriers.add(courier);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("CheckCouriers", "Error parsing response for " + courier, e);
                        }
                        
                        // When all requests are done, update UI
                        if (pendingRequests.decrementAndGet() == 0) {
                            updateCourierUI();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("CheckCouriers", "Network error for " + courier, t);
                        
                        // When all requests are done, update UI
                        if (pendingRequests.decrementAndGet() == 0) {
                            updateCourierUI();
                        }
                    }
                });
        }
    }
    
    // Add method to update courier UI based on available couriers
    private void updateCourierUI() {
        runOnUiThread(() -> {
            // Hide loading indicator
            binding.courierLoadingProgress.setVisibility(View.GONE);
            
            // Show courier options based on availability
            if (availableCouriers.contains("jne")) {
                binding.rbJne.setVisibility(View.VISIBLE);
            }
            
            if (availableCouriers.contains("tiki")) {
                binding.rbTiki.setVisibility(View.VISIBLE);
            }
            
            if (availableCouriers.contains("pos")) {
                binding.rbPos.setVisibility(View.VISIBLE);
            }
            
            // Show message if no couriers are available
            if (availableCouriers.isEmpty()) {
                Toasty.warning(CheckoutActivity.this, 
                    "No delivery services available for this location", 
                    Toast.LENGTH_LONG).show();
            }
        });
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
            calculateShippingCost(selectedCourier, calculateTotalWeight());
        });
        return selectedCourier;
    }

    private void calculateShippingCost(String courier, int weight) {
        binding.progressBar.setVisibility(View.VISIBLE);
        // Tampilkan loading spinner di dalam CardView shipping services
        binding.servicesCardView.setVisibility(View.VISIBLE);
        binding.servicesLoadingProgress.setVisibility(View.VISIBLE);
        binding.recyclerViewServices.setVisibility(View.GONE);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getShippingCost(AppConfig.STORE_CITY_ID, cityId, weight, courier).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progressBar.setVisibility(View.GONE);
                // Sembunyikan loading spinner di dalam CardView shipping services
                binding.servicesLoadingProgress.setVisibility(View.GONE);
                binding.recyclerViewServices.setVisibility(View.VISIBLE);

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
                                } else {
                                    // Jika tidak ada layanan ditemukan
                                    Toasty.info(CheckoutActivity.this,
                                            "Tidak ada layanan pengiriman yang tersedia",
                                            Toast.LENGTH_SHORT).show();
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
                binding.servicesLoadingProgress.setVisibility(View.GONE);
                binding.recyclerViewServices.setVisibility(View.VISIBLE);

                Log.e("ShippingCost", "Network error", t);
                Toasty.error(CheckoutActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectShippingService(Map<String, String> service) {
        selectedService = service.get("service");
        selectedDeliveryTime = service.get("etd") + " hari"; // Store delivery time
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

        // Validate payment method
        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            Toasty.warning(this, "Please select a payment method", Toast.LENGTH_SHORT, true).show();
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

        // Calculate total weight for the entire order
        int totalWeight = calculateTotalWeight();

        // Format cart data as JSON array for API
        JsonArray cartArray = new JsonArray();
        for (Product item : cartItems) {
            JsonObject productObj = new JsonObject();
            productObj.addProperty("product_code", item.getKode());  // Make sure this matches exactly
            productObj.addProperty("product_name", item.getMerk());  // Make sure this matches exactly
            productObj.addProperty("price", item.getHargaJual());    // Make sure this matches exactly
            productObj.addProperty("quantity", item.getQty());       // Make sure this matches exactly
            // Remove any fields not needed by the API to avoid confusion
            cartArray.add(productObj);
        }
        String cartJson = cartArray.toString();
        
        Log.d("CheckoutDebug", "Cart JSON: " + cartJson);  // Log the exact JSON being sent
    
        // Calculate grand total
        double grandTotal = cartTotal + shippingCost;

        // Make API call with the actual address ID and including origin ID
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ResponseBody> checkoutCall = apiService.processCheckout(
                userId,
                addressId,
                originId,
                cartTotal,
                shippingCost,
                grandTotal,
                selectedCourier,
                selectedService,
                cartJson,
                totalWeight,
                selectedPaymentMethod,
                selectedDeliveryTime  // Add delivery time parameter
        );

        checkoutCall.enqueue(new Callback<ResponseBody>() {
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
                            clearCart();

                            // Check payment method and handle differently
                            if ("transfer".equals(selectedPaymentMethod)) {
                                // For transfer payment, go to payment details screen
                                navigateToPaymentDetails(jsonResponse);
                            } else {
                                // For COD, show success dialog
                                showSuccessDialog(jsonResponse.getJSONObject("order").getString("order_number"));
                            }
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

    private void navigateToPaymentDetails(JSONObject responseData) {
        try {
            JSONObject orderData = responseData.getJSONObject("order");
            JSONObject paymentInfo = responseData.getJSONObject("payment_info");
            
            Log.d("CheckoutDebug", "Navigating to payment details for order: " + orderData.getString("order_number"));
            
            Intent intent = new Intent(this, PaymentDetailsActivity.class);
            intent.putExtra("ORDER_ID", orderData.getInt("order_id"));
            intent.putExtra("ORDER_NUMBER", orderData.getString("order_number"));
            intent.putExtra("GRAND_TOTAL", orderData.getDouble("grand_total"));
            intent.putExtra("BANK_NAME", paymentInfo.getString("bank_name"));
            intent.putExtra("ACCOUNT_NUMBER", paymentInfo.getString("account_number"));
            intent.putExtra("ACCOUNT_NAME", paymentInfo.getString("account_name"));
            intent.putExtra("INSTRUCTIONS", paymentInfo.getString("instructions"));
            
            // Use explicit flag combination to ensure proper navigation
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            startActivity(intent);
            finish(); // Make sure we're finishing this activity
            
            // Add force finish to ensure no return to cart
            Runtime.getRuntime().gc(); // Request garbage collection
        } catch (Exception e) {
            Log.e("CheckoutDebug", "Error navigating to payment details", e);
            Toasty.error(this, "Error displaying payment details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
            // Navigate to OrderHistoryActivity
            Intent intent = new Intent(CheckoutActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Find and set payment method text
        TextView tvPaymentMethod = dialog.findViewById(R.id.tvPaymentMethod);
        String paymentMethodText = selectedPaymentMethod.equals("transfer") ? "Bank Transfer" : "Cash on Delivery";
        tvPaymentMethod.setText(paymentMethodText);

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
        // Clear the cart in SharedPreferences - use commit() for immediate effect
        SharedPreferences sharedPreferences = getSharedPreferences("product", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("listproduct", "[]"); // Empty array
        editor.commit(); // Use commit() instead of apply() for immediate effect
        
        // Broadcast that the cart has been cleared
        Intent intent = new Intent("com.example.uts_a22202303006.UPDATE_CART_BADGE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
        Log.d("CheckoutDebug", "Cart has been cleared");
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
            // Use actual weight from the product, with fallback to 100g if not available
            int itemWeight = item.getWeight() > 0 ? item.getWeight() : 100;
            totalWeight += itemWeight * item.getQty();
        }

        // Ensure minimum weight is 1000g (1kg)
        return Math.max(totalWeight, 1000);
    }

    // Tambahkan method untuk reset shipping method, service, dan cost
    private void resetShippingSelection() {
        // Reset shipping method (RadioGroup)
        binding.radioGroupCourier.clearCheck();
        selectedCourier = null;

        // Reset shipping service
        shippingServices.clear();

        // Buat adapter baru untuk benar-benar mereset pilihan
        shippingServiceAdapter = new ShippingServiceAdapter(this::selectShippingService);
        binding.recyclerViewServices.setAdapter(shippingServiceAdapter);

        // Kosongkan data dan pastikan RecyclerView kosong
        shippingServiceAdapter.submitList(null);

        // Pastikan CardView shipping service disembunyikan dengan memberikan View.GONE
        binding.servicesCardView.setVisibility(View.GONE);
        binding.servicesLoadingProgress.setVisibility(View.GONE);
        selectedService = null;
        
        // Reset available couriers
        availableCouriers.clear();
        
        // Hide courier options
        binding.rbJne.setVisibility(View.GONE);
        binding.rbTiki.setVisibility(View.GONE);
        binding.rbPos.setVisibility(View.GONE);

        // Reset shipping cost
        shippingCost = 0;
        updateOrderSummary();
    }

    private void setupPaymentMethodSelection() {
        binding.radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTransfer) {
                selectedPaymentMethod = "transfer";
            } else if (checkedId == R.id.rbCod) {
                selectedPaymentMethod = "cod";
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Load products from the cart into the product list in checkout
     */
    private void loadProductsFromCart() {
        ArrayList<Product> cartItems = getCartItems();
        
        if (cartItems != null && !cartItems.isEmpty()) {
            productAdapter.setProducts(cartItems);
            binding.noProductsLayout.setVisibility(View.GONE);
            binding.recyclerViewProducts.setVisibility(View.VISIBLE);
        } else {
            binding.noProductsLayout.setVisibility(View.VISIBLE);
            binding.recyclerViewProducts.setVisibility(View.GONE);
        }
    }
}
