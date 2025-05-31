package com.example.uts_a22202303006.product;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.uts_a22202303006.MainActivity;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.product.ProductImage;
import com.example.uts_a22202303006.product.ProductImageResponse;
import com.example.uts_a22202303006.auth.LoginRequiredManager;
import com.google.android.material.tabs.TabLayout;
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
    private TextView productDetailWeight; // Added weight TextView
    private Button btnAddToCart;
    private ImageButton btnBack;
    private Product currentProduct;
    private SharedPreferences sharedPreferences;
    private TextView productDetailVisitCount;
    private ImageSlider productImageSlider;
    private List<String> imageUrls = new ArrayList<>();

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
//        productDetailImage = findViewById(R.id.productDetailImage);
        productImageSlider = findViewById(R.id.productImageSlider);
        productDetailCategory = findViewById(R.id.productDetailCategory);
        productDetailCategory = findViewById(R.id.productDetailCategory);
        productDetailName = findViewById(R.id.productDetailName);
        productDetailPrice = findViewById(R.id.productDetailPrice);
        productDetailStock = findViewById(R.id.productDetailStock);
        productDetailWeight = findViewById(R.id.productDetailWeight); // Initialize weight TextView
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

    private void loadProductImages(String productCode) {
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ProductImageResponse> call = apiService.getProductImages(productCode);

        call.enqueue(new Callback<ProductImageResponse>() {
            @Override
            public void onResponse(Call<ProductImageResponse> call, Response<ProductImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductImage> images = response.body().getResult();
                    imageUrls.clear();

                    if (images != null && !images.isEmpty()) {
                        // Get photos from API response
                        for (ProductImage image : images) {
                            imageUrls.add(ServerAPI.BASE_URL_IMAGE + image.getFoto());
                        }
                    } else if (currentProduct != null) {
                        // Fallback to single product image
                        imageUrls.add(currentProduct.getFoto());
                    }

                    setupImageSlider();
                }
            }

            @Override
            public void onFailure(Call<ProductImageResponse> call, Throwable t) {
                // Fallback to single product image
                if (currentProduct != null) {
                    imageUrls.clear();
                    imageUrls.add(currentProduct.getFoto());
                    setupImageSlider();
                }
            }
        });
    }

    private void setupImageSlider() {
        List<SlideModel> slideModels = new ArrayList<>();

        // Convert string URLs to SlideModel objects
        for (String imageUrl : imageUrls) {
            slideModels.add(new SlideModel(imageUrl, ScaleTypes.FIT));
        }

        // Set the slider adapter
        productImageSlider.setImageList(slideModels);
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
        // Remove this block - it's causing the crash:
        // Glide.with(this)
        //     .load(product.getFoto())
        //     .placeholder(R.drawable.placeholder_image)
        //     .error(R.drawable.error_image)
        //     .into(productDetailImage);

        // Make sure to call the image loading method instead
        loadProductImages(product.getKode());

        // Keep the rest of your existing code
        productDetailCategory.setText(product.getKategori());
        productDetailName.setText(product.getMerk());

        // Format currency
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String price = formatter.format(product.getHargaJual());
        productDetailPrice.setText(price);

        productDetailStock.setText(String.valueOf(product.getStok()));
        // Set weight
        productDetailWeight.setText(product.getWeight() + " gram");
        productDetailDescription.setText(product.getDeskripsi());

        // Show trending badge for popular products
        View trendingBadge = findViewById(R.id.trendingBadge);
        trendingBadge.setVisibility(product.getVisitCount() > 10 ? View.VISIBLE : View.GONE);

        // Display visit count
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
            // Create deep copy of product instead of using direct reference
            Product cartProduct = gson.fromJson(gson.toJson(currentProduct), Product.class);
            cartProduct.setQty(1);
            cartItems.add(cartProduct);
        }

        // Save updated cart
        String updatedJson = gson.toJson(cartItems);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("listproduct", updatedJson);
        editor.apply();

        // Update cart badge in MainActivity using broadcast
        Intent intent = new Intent("com.example.uts_a22202303006.UPDATE_CART_BADGE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Toasty.success(this, "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT, true).show();
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
