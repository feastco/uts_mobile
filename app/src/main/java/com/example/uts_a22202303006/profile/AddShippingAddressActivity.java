package com.example.uts_a22202303006.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.databinding.ActivityAddShippingAddressBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddShippingAddressActivity extends AppCompatActivity {

    private static final String TAG = "AddShippingAddress";
    private ActivityAddShippingAddressBinding binding;
    private int userId;
    private ShippingAddress addressToEdit = null;
    private boolean isEditMode = false;

    // Data structures for provinces and cities
    private List<Map<String, String>> provinces = new ArrayList<>();
    private List<Map<String, String>> cities = new ArrayList<>();

    // Selection data
    private int provinceId = -1;
    private String provinceName;
    private int cityId = -1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddShippingAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get user ID from shared preferences or intent
        getUserId();

        // Check if we're editing an existing address
        checkEditMode();

        // Setup UI elements
        setupToolbar();
        loadProvinces();
        setupSaveButton();
    }

    private void getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("login_session", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("id", -1);

        if (userId == -1) {
            // Try to get from intent if not in shared preferences
            userId = getIntent().getIntExtra("user_id", -1);

            if (userId == -1) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("address")) {
            addressToEdit = (ShippingAddress) getIntent().getSerializableExtra("address");
            isEditMode = true;
            binding.tvTitle.setText("Edit Address");
            populateFieldsWithAddress();
        }
    }

    private void setupToolbar() {
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void setupSaveButton() {
        binding.btnSaveAddress.setOnClickListener(v -> {
            if (validateForm()) {
                saveAddress();
            }
        });
    }

    private void populateFieldsWithAddress() {
        if (addressToEdit == null) return;

        // Fill form fields with existing address data
        binding.etRecipientName.setText(addressToEdit.getNamaPenerima());
        binding.etPhoneNumber.setText(addressToEdit.getNomorTelepon());
        binding.etAddress.setText(addressToEdit.getAlamatLengkap());
        binding.etPostalCode.setText(addressToEdit.getKodePos());

        // Store IDs for selection after spinners are populated
        provinceId = addressToEdit.getProvinceId();
        provinceName = addressToEdit.getProvince();
        cityId = addressToEdit.getCityId();
        cityName = addressToEdit.getCity();
    }

    private void loadProvinces() {
        showLoading(true);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getProvinces().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "Province API response: " + jsonResponse);

                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        if (jsonObject != null && jsonObject.has("rajaongkir")) {
                            JsonObject rajaongkir = jsonObject.getAsJsonObject("rajaongkir");
                            JsonObject status = rajaongkir.getAsJsonObject("status");

                            if (status.get("code").getAsInt() == 200) {
                                processProvinceData(rajaongkir.getAsJsonArray("results"));
                            } else {
                                showError("Error: " + status.get("description").getAsString());
                            }
                        } else {
                            showError("Invalid API response format");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing province response", e);
                        showError("Error processing data");
                    }
                } else {
                    showError("Failed to load provinces");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Province API network error", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void processProvinceData(JsonArray jsonProvinces) {
        provinces.clear();

        // Add placeholder item
        Map<String, String> placeholder = new HashMap<>();
        placeholder.put("id", "-1");
        placeholder.put("name", "Select Province");
        provinces.add(placeholder);

        // Process provinces data
        for (int i = 0; i < jsonProvinces.size(); i++) {
            JsonObject provinceObj = jsonProvinces.get(i).getAsJsonObject();
            Map<String, String> province = new HashMap<>();
            province.put("id", provinceObj.get("province_id").getAsString());
            province.put("name", provinceObj.get("province").getAsString());
            provinces.add(province);
        }

        setupProvinceSpinner();
    }

    // In AddShippingAddressActivity.java

    // Modify the setupProvinceSpinner method to handle edit mode better
    private void setupProvinceSpinner() {
        // Set up province spinner adapter
        List<String> provinceNames = new ArrayList<>();
        for (Map<String, String> province : provinces) {
            provinceNames.add(province.get("name"));
        }

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                provinceNames);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProvince.setAdapter(provinceAdapter);

        // If editing, select the correct province
        if (isEditMode && provinceId > 0) {
            int provincePosition = -1;
            for (int i = 0; i < provinces.size(); i++) {
                if (provinces.get(i).get("id") != null &&
                        Integer.parseInt(provinces.get(i).get("id")) == provinceId) {
                    provincePosition = i;
                    break;
                }
            }

            // Store the position for use after setting the listener
            final int finalProvincePosition = provincePosition;

            // Set up province selection listener
            binding.spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Reset city spinner when province changes
                    cities.clear();

                    // Skip loading cities for placeholder item
                    if (position == 0) {
                        provinceId = -1;
                        provinceName = null;
                        resetCitySpinner();
                        return;
                    }

                    // Get selected province ID and load cities
                    Map<String, String> selectedProvince = provinces.get(position);
                    provinceId = Integer.parseInt(selectedProvince.get("id"));
                    provinceName = selectedProvince.get("name");

                    // Load cities with a callback to select the city in edit mode
                    if (isEditMode && position == finalProvincePosition) {
                        loadCitiesForEditMode(provinceId);
                    } else {
                        loadCities(provinceId);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            // Trigger selection manually to load cities
            if (provincePosition >= 0) {
                binding.spinnerProvince.setSelection(provincePosition);
            }
        } else {
            // Regular setup for non-edit mode
            binding.spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Reset city spinner when province changes
                    cities.clear();
                    cityId = -1;

                    // Skip loading cities for placeholder item
                    if (position == 0) {
                        provinceId = -1;
                        provinceName = null;
                        resetCitySpinner();
                        return;
                    }

                    // Get selected province ID and load cities
                    Map<String, String> selectedProvince = provinces.get(position);
                    provinceId = Integer.parseInt(selectedProvince.get("id"));
                    provinceName = selectedProvince.get("name");
                    loadCities(provinceId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }
    }

    // Add a special method to load cities for edit mode
    private void loadCitiesForEditMode(int provinceId) {
        showLoading(true);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getCities(provinceId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "City API response: " + jsonResponse);

                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        if (jsonObject != null && jsonObject.has("rajaongkir")) {
                            JsonObject rajaongkir = jsonObject.getAsJsonObject("rajaongkir");
                            JsonObject status = rajaongkir.getAsJsonObject("status");

                            if (status.get("code").getAsInt() == 200) {
                                processCityDataForEditMode(rajaongkir.getAsJsonArray("results"));
                            } else {
                                showError("Error: " + status.get("description").getAsString());
                            }
                        } else {
                            showError("Invalid response format");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing city response", e);
                        showError("Error processing city data");
                    }
                } else {
                    showError("Failed to load cities");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "City API network error", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    // Process city data specifically for edit mode
    private void processCityDataForEditMode(JsonArray jsonCities) {
        cities.clear();

        for (int i = 0; i < jsonCities.size(); i++) {
            JsonObject city = jsonCities.get(i).getAsJsonObject();

            Map<String, String> cityMap = new HashMap<>();
            cityMap.put("id", city.get("city_id").getAsString());
            cityMap.put("name", city.get("city_name").getAsString());
            cityMap.put("type", city.get("type").getAsString());
            cityMap.put("postal_code", city.get("postal_code").getAsString()); // Add this line
            cities.add(cityMap);
        }

        setupCitySpinnerForEditMode();
    }

    // Setup city spinner specifically for edit mode
    private void setupCitySpinnerForEditMode() {
        List<String> cityNames = new ArrayList<>();
        cityNames.add("Select City");

        for (Map<String, String> city : cities) {
            String displayName = city.get("type") + " " + city.get("name");
            cityNames.add(displayName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCity.setAdapter(adapter);

        // Select the city matching our stored cityId
        int cityPosition = 0; // Default to first position (Select City)

        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).get("id") != null &&
                    Integer.parseInt(cities.get(i).get("id")) == cityId) {
                cityPosition = i + 1; // +1 because of "Select City"
                Log.d(TAG, "Found matching city at position: " + cityPosition);
                break;
            }
        }

        if (cityPosition > 0) {
            Log.d(TAG, "Setting city selection to position: " + cityPosition);
            final int finalCityPosition = cityPosition;

            // Need to post this to the UI thread to ensure it happens after adapter is set
            binding.spinnerCity.post(() -> {
                binding.spinnerCity.setSelection(finalCityPosition);
            });

            // Also make sure the city data is properly set
            if (cityPosition > 0 && cityPosition <= cities.size()) {
                Map<String, String> selectedCity = cities.get(cityPosition - 1);
                cityId = Integer.parseInt(selectedCity.get("id"));
                cityName = selectedCity.get("name");
                Log.d(TAG, "Selected city: " + cityName + " (ID: " + cityId + ")");
            }
        }

        // Set listener for city selection
        binding.spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Map<String, String> selectedCity = cities.get(position - 1);
                    cityId = Integer.parseInt(selectedCity.get("id"));
                    cityName = selectedCity.get("name");

                    // Auto-fill postal code
                    String postalCode = selectedCity.get("postal_code");
                    binding.etPostalCode.setText(postalCode);

                    Log.d(TAG, "Selected city: " + cityName + " (ID: " + cityId + ")");
                } else {
                    cityId = -1;
                    cityName = null;
                    // Clear postal code when "Select City" is selected
                    binding.etPostalCode.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void resetCitySpinner() {
        List<String> emptyCities = new ArrayList<>();
        emptyCities.add("Select City");
        ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                emptyCities);
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCity.setAdapter(emptyAdapter);
    }

    private void loadCities(int provinceId) {
        showLoading(true);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        apiService.getCities(provinceId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "City API response: " + jsonResponse);

                        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                        if (jsonObject != null && jsonObject.has("rajaongkir")) {
                            JsonObject rajaongkir = jsonObject.getAsJsonObject("rajaongkir");
                            JsonObject status = rajaongkir.getAsJsonObject("status");

                            if (status.get("code").getAsInt() == 200) {
                                processCityData(rajaongkir.getAsJsonArray("results"));
                            } else {
                                showError("Error: " + status.get("description").getAsString());
                            }
                        } else {
                            showError("Invalid response format");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing city response", e);
                        showError("Error processing city data");
                    }
                } else {
                    showError("Failed to load cities");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "City API network error", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void processCityData(JsonArray jsonCities) {
        cities.clear();

        for (int i = 0; i < jsonCities.size(); i++) {
            JsonObject city = jsonCities.get(i).getAsJsonObject();

            Map<String, String> cityMap = new HashMap<>();
            cityMap.put("id", city.get("city_id").getAsString());
            cityMap.put("name", city.get("city_name").getAsString());
            cityMap.put("type", city.get("type").getAsString());
            cityMap.put("postal_code", city.get("postal_code").getAsString()); // Add this line
            cities.add(cityMap);
        }

        setupCitySpinner();
    }

    private void setupCitySpinner() {
        // Create adapter and set to spinner
        List<String> cityNames = new ArrayList<>();
        cityNames.add("Select City");

        for (Map<String, String> city : cities) {
            String displayName = city.get("type") + " " + city.get("name");
            cityNames.add(displayName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCity.setAdapter(adapter);

        // If editing, select the correct city
        if (isEditMode && cityId > 0) {
            for (int i = 0; i < cities.size(); i++) {
                if (cities.get(i).get("id") != null &&
                        Integer.parseInt(cities.get(i).get("id")) == cityId) {
                    binding.spinnerCity.setSelection(i + 1); // +1 because of "Select City"
                    break;
                }
            }
        }

        // Set listener for city selection
        binding.spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Map<String, String> selectedCity = cities.get(position - 1);
                    cityId = Integer.parseInt(selectedCity.get("id"));
                    cityName = selectedCity.get("name");

                    // Auto-fill postal code
                    String postalCode = selectedCity.get("postal_code");
                    binding.etPostalCode.setText(postalCode);

                    Log.d(TAG, "Selected city: " + cityName + " (ID: " + cityId + ")");
                } else {
                    cityId = -1;
                    cityName = null;
                    // Clear postal code when "Select City" is selected
                    binding.etPostalCode.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Reset errors
        binding.tilRecipientName.setError(null);
        binding.tilPhoneNumber.setError(null);
        binding.tilFullAddress.setError(null);
        binding.etPostalCode.setError(null);

        // Validate recipient name
        String recipientName = binding.etRecipientName.getText().toString().trim();
        if (recipientName.isEmpty()) {
            binding.tilRecipientName.setError("Recipient name is required");
            isValid = false;
        }

        // Validate phone number
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            binding.tilPhoneNumber.setError("Phone number is required");
            isValid = false;
        } else if (phoneNumber.length() < 10) {
            binding.tilPhoneNumber.setError("Please enter a valid phone number");
            isValid = false;
        }

        // Validate address
        String address = binding.etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            binding.tilFullAddress.setError("Address is required");
            isValid = false;
        }

        // Validate postal code
        String postalCode = binding.etPostalCode.getText().toString().trim();
        if (postalCode.isEmpty()) {
            binding.etPostalCode.setError("Postal code is required");
            isValid = false;
        } else if (postalCode.length() < 5) {
            binding.etPostalCode.setError("Postal code must be 5 digits");
            isValid = false;
        }

        // Validate province and city
        if (provinceId == -1) {
            showError("Please select a province");
            isValid = false;
        }

        if (cityId == -1) {
            showError("Please select a city");
            isValid = false;
        }

        return isValid;
    }

    private void saveAddress() {
        showLoading(true);
        binding.btnSaveAddress.setEnabled(false);

        // Get form data
        String recipientName = binding.etRecipientName.getText().toString().trim();
        String phoneNumber = binding.etPhoneNumber.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String postalCode = binding.etPostalCode.getText().toString().trim();

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ResponseBody> call;

        if (isEditMode) {
            // Update existing address
            call = apiService.updateShippingAddress(
                    addressToEdit.getId(),
                    userId,
                    recipientName,
                    phoneNumber,
                    address,
                    provinceId,
                    provinceName,
                    cityId,
                    cityName,
                    postalCode,
                    addressToEdit.getIsDefaultAsInt()  // Add this missing parameter
            );
        } else {
            // Create new address
            call = apiService.addShippingAddress(
                    userId,
                    recipientName,
                    phoneNumber,
                    address,
                    provinceId,
                    provinceName,
                    cityId,
                    cityName,
                    postalCode
            );
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                showLoading(false);
                binding.btnSaveAddress.setEnabled(true);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        showToast(jsonObject.getString("message"));

                        if (jsonObject.getBoolean("status")) {
                            // Return result and close activity
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "API error: " + errorBody);
                        showError("Failed to save address");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing save response", e);
                    showError("Error processing response");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                showLoading(false);
                binding.btnSaveAddress.setEnabled(true);
                Log.e(TAG, "Save address network error", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}