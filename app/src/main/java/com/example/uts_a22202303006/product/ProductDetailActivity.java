package com.example.uts_a22202303006.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.auth.LoginRequiredManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productDetailImage;
    private TextView productDetailCategory, productDetailName, productDetailPrice, productDetailStock, productDetailDescription;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private Product currentProduct;
    private SharedPreferences sharedPreferences;
    private TextView productDetailVisitCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize views
        initViews();

        // Get product data from intent
        String productCode = getIntent().getStringExtra("PRODUCT_CODE");
        if (productCode != null && !productCode.isEmpty()) {
            loadProductDetails(productCode);
        } else {
            Toasty.error(this, "Product not found", Toast.LENGTH_SHORT, true).show();
            finish();
        }

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        productDetailImage = findViewById(R.id.productDetailImage);
        productDetailCategory = findViewById(R.id.productDetailCategory);
        productDetailName = findViewById(R.id.productDetailName);
        productDetailPrice = findViewById(R.id.productDetailPrice);
        productDetailStock = findViewById(R.id.productDetailStock);
        productDetailDescription = findViewById(R.id.productDetailDescription);
        productDetailVisitCount = findViewById(R.id.productDetailVisitCount);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBack = findViewById(R.id.btnBack);

        sharedPreferences = getSharedPreferences("product", MODE_PRIVATE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProductDetails(String productCode) {
        // Check if count was already updated by HomeFragment
        boolean countAlreadyUpdated = getIntent().getBooleanExtra("COUNT_ALREADY_UPDATED", false);

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ProductResponse> call = apiService.getProducts("all", "");

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Find product code match
                    Product matchedProduct = null;
                    for (Product product : response.body().getResult()) {
                        if (productCode.equals(product.getKode())) {
                            matchedProduct = product;
                            break;
                        }
                    }

                    if (matchedProduct != null) {
                        currentProduct = matchedProduct;
                        displayProductDetails(matchedProduct);

                        // Only update visit count if not already updated
                        if (!countAlreadyUpdated) {
                            updateVisitCount(productCode);
                        }
                    } else {
                        showProductNotFound();
                    }
                } else {
                    showProductNotFound();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toasty.error(ProductDetailActivity.this, "Failed to load product: " + t.getMessage(),
                        Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    private void displayProductDetails(Product product) {
        // Load product image
        Glide.with(this)
                .load(product.getFoto())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(productDetailImage);

        // Set text fields
        productDetailCategory.setText(product.getKategori());
        productDetailName.setText(product.getMerk());

        // Format currency
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String price = formatter.format(product.getHargaJual());
        productDetailPrice.setText(price);

        productDetailStock.setText(String.valueOf(product.getStok()));
        productDetailDescription.setText(product.getDeskripsi());

        int visitCount = product.getVisitCount();

        // Show trending badge only for popular products (adjust threshold as needed)
        View trendingBadge = findViewById(R.id.trendingBadge);
        trendingBadge.setVisibility(visitCount > 10 ? View.VISIBLE : View.GONE);

        // Add this line to display visit count
        productDetailVisitCount.setText(product.getVisitCount() + " views");

        // Enable/disable add to cart button based on stock
        btnAddToCart.setEnabled(product.getStok() > 0);
        btnAddToCart.setAlpha(product.getStok() > 0 ? 1.0f : 0.5f);
    }

    private void showProductNotFound() {
        Toasty.error(this, "Product not found", Toast.LENGTH_SHORT, true).show();
    }

    private void updateVisitCount(String productCode) {
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ResponseBody> call = apiService.updateVisitCount(productCode);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Increment the visit count in the UI after successful server update
                if (currentProduct != null) {
//                    int newCount = currentProduct.getVisitCount() + 1;
                    int newCount = currentProduct.getVisitCount();
                    currentProduct.setVisitCount(newCount);
                    productDetailVisitCount.setText(newCount + " views");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fail silently - this is not critical functionality
            }
        });
    }

    private void addToCart() {
        if (currentProduct == null || currentProduct.getStok() <= 0) {
            Toasty.warning(this, "Product is not available", Toast.LENGTH_SHORT, true).show();
            return;
        }

        // Check if user is logged in
        if (!LoginRequiredManager.isFullyLoggedIn(this)) {
            LoginRequiredManager.showLoginRequiredDialog(this);
            return;
        }

        // Get current cart items
        Gson gson = new Gson();
        String jsonText = sharedPreferences.getString("listproduct", null);
        ArrayList<Product> cartItems = gson.fromJson(jsonText, new TypeToken<ArrayList<Product>>(){}.getType());

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Check if product already exists in cart
        boolean productExists = false;
        for (Product cartItem : cartItems) {
            if (cartItem.getKode().equals(currentProduct.getKode())) {
                if (cartItem.getQty() >= currentProduct.getStok()) {
                    Toasty.warning(this, "Not enough stock available", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                cartItem.setQty(cartItem.getQty() + 1);
                productExists = true;
                break;
            }
        }

        // Add new product to cart if it doesn't exist
        if (!productExists) {
            currentProduct.setQty(1);
            cartItems.add(currentProduct);
        }

        // Save updated cart
        String updatedJson = gson.toJson(cartItems);
        sharedPreferences.edit().putString("listproduct", updatedJson).apply();

        Toasty.success(this, "Added to cart", Toast.LENGTH_SHORT, true).show();
    }

    @Override
    public void onBackPressed() {
        String source = getIntent().getStringExtra("FROM_SOURCE");

        if (source != null) {
            // We're not doing custom navigation since finish() will take us back to the previous activity
            // But we're prepared for custom logic if needed in the future
            switch (source) {
                case "HOME_FRAGMENT":
                    // Just finish to return to Home Fragment
                    break;
                case "PRODUCT_FRAGMENT":
                    // Just finish to return to Product Fragment
                    break;
                case "SEARCH_ACTIVITY":
                    // Just finish to return to Search Activity
                    break;
            }
        }

        super.onBackPressed();
    }
}