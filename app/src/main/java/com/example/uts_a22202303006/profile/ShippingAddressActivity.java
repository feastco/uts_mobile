package com.example.uts_a22202303006.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.adapter.ShippingAddressAdapter;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.databinding.ActivityShippingAddressBinding;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShippingAddressActivity extends AppCompatActivity implements ShippingAddressAdapter.OnAddressClickListener {

    private static final String TAG = "ShippingAddr";
    private ActivityShippingAddressBinding binding;
    private ShippingAddressAdapter addressAdapter;
    private List<ShippingAddress> addressList = new ArrayList<>();
    private int userId;

    private final ActivityResultLauncher<Intent> addAddressLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh address list after adding/editing
                    loadAddresses();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShippingAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get user ID from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("id", -1);


        if (userId == -1) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup UI
        setupToolbar();
        setupRecyclerView();
        setupAddButton();

        // Load addresses
        loadAddresses();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Shipping Addresses");
    }

    private void setupRecyclerView() {
        addressAdapter = new ShippingAddressAdapter(this, addressList);
        addressAdapter.setOnAddressClickListener(this);
        binding.recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAddresses.setAdapter(addressAdapter);
    }

    private void setupAddButton() {
        binding.fabAddAddress.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddShippingAddressActivity.class);
            intent.putExtra("user_id", userId);
            addAddressLauncher.launch(intent);
        });
    }

    private void loadAddresses() {
        showLoading(true);
        showEmptyState(false);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getShippingAddresses(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                showLoading(false);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "API Response: " + jsonResponse);

                        // Parse JSON response
                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        if (jsonObject.has("status") && jsonObject.get("status").getAsBoolean()) {
                            // Parse the data array instead of result
                            if (jsonObject.has("data")) {
                                Gson gson = new Gson();
                                Type listType = new TypeToken<List<ShippingAddress>>(){}.getType();
                                addressList = gson.fromJson(jsonObject.get("data"), listType);

                                // Sort addresses by ID (ascending order)
                                Collections.sort(addressList, (a1, a2) ->
                                        Integer.compare(a1.getId(), a2.getId()));

                                // Update adapter with new data
                                addressAdapter.updateData(addressList);

                                // Show empty state if no addresses
                                showEmptyState(addressList.isEmpty());
                            } else {
                                Log.e(TAG, "Missing 'data' field in response");
                                showEmptyState(true);
                            }
                        } else {
                            String message = jsonObject.has("message") ?
                                    jsonObject.get("message").getAsString() : "Failed to get addresses";
                            Toast.makeText(ShippingAddressActivity.this, message, Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    } else {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "API error: " + errorBody);
                        Toast.makeText(ShippingAddressActivity.this,
                                "Failed to load addresses", Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    Toast.makeText(ShippingAddressActivity.this,
                            "Error processing data", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error", t);
                Toast.makeText(ShippingAddressActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean isEmpty) {
        binding.emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewAddresses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSetDefaultClick(ShippingAddress address) {
        showLoading(true);

        // Get user ID from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", -1);

        if (userId == -1) {
            Toasty.error(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // Call the API
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.setDefaultShippingAddress(address.getId(), userId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        binding.progressBar.setVisibility(View.GONE);

                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseData = response.body().string();
                                JSONObject jsonObject = new JSONObject(responseData);

                                if (jsonObject.getBoolean("status")) {
                                    // Success - refresh the address list
                                    Toasty.success(ShippingAddressActivity.this,
                                            jsonObject.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                    loadAddresses();
                                } else {
                                    // API returned error
                                    Toasty.error(ShippingAddressActivity.this,
                                            jsonObject.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // HTTP error
                                Toasty.error(ShippingAddressActivity.this,
                                        "Failed to set default address",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("SetDefault", "Error parsing response", e);
                            Toasty.error(ShippingAddressActivity.this,
                                    "Error processing response",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Log.e("SetDefault", "Network error", t);
                        Toasty.error(ShippingAddressActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDeleteClick(ShippingAddress address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteAddress(address.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditClick(ShippingAddress address) {
        Intent intent = new Intent(this, AddShippingAddressActivity.class);
        intent.putExtra("address", address);
        intent.putExtra("user_id", userId);
        addAddressLauncher.launch(intent);
    }

    private void deleteAddress(int addressId) {
        showLoading(true);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.deleteShippingAddress(addressId, userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                showLoading(false);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        Toast.makeText(ShippingAddressActivity.this,
                                jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                        if (jsonObject.getBoolean("status")) {
                            // Refresh address list
                            loadAddresses();
                        }
                    } else {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "API error: " + errorBody);
                        Toast.makeText(ShippingAddressActivity.this,
                                "Failed to delete address", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response", e);
                    Toast.makeText(ShippingAddressActivity.this,
                            "Error processing response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error", t);
                Toast.makeText(ShippingAddressActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}