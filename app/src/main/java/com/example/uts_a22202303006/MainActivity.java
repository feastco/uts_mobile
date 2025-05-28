package com.example.uts_a22202303006;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.uts_a22202303006.auth.LoginRequiredManager;
import com.example.uts_a22202303006.databinding.ActivityMainBinding;
import com.example.uts_a22202303006.product.Product;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver cartUpdateReceiver;
    private ActivityMainBinding binding;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize LocalBroadcastManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Create broadcast receiver for cart updates
        cartUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.uts_a22202303006.UPDATE_CART_BADGE".equals(intent.getAction())) {
                    updateCartBadge();
                }
            }
        };

        // Register with LocalBroadcastManager
        IntentFilter filter = new IntentFilter("com.example.uts_a22202303006.UPDATE_CART_BADGE");
        localBroadcastManager.registerReceiver(cartUpdateReceiver, filter);

        // Setup BottomNavigationView and Navigation
        setupNavigation();

        // Initialize cart badge
        updateCartBadge();
    }


    /**
     * Mengatur BottomNavigationView dengan NavController.
     */
    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Konfigurasi destinasi utama
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_product, R.id.navigation_cart, R.id.navigation_profile, R.id.navigation_home)
                .build();

        // Setup NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Replace the standard setup with custom item selection listener
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Check restricted sections (cart and profile)
            if (itemId == R.id.navigation_profile) {
                if (!LoginRequiredManager.isFullyLoggedIn(this)) {
                    LoginRequiredManager.showLoginRequiredDialog(this);
                    return false; // Prevent navigation
                }
            }

            // Navigate using NavController if checks pass
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    /**
     * Memperbarui badge keranjang dengan jumlah total produk di keranjang.
     */
    public void updateCartBadge() {
        // Ambil data keranjang dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("product", MODE_PRIVATE);
        String jsonText = sharedPreferences.getString("listproduct", null);

        // Dapatkan BottomNavigationView dan BadgeDrawable
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.navigation_cart);

        if (jsonText != null) {
            // Parsing data keranjang
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Product>>() {}.getType();
            ArrayList<Product> listProduct = gson.fromJson(jsonText, type);

            // Hitung jumlah total produk
            int totalQty = 0;
            for (Product product : listProduct) {
                totalQty += product.getQty();
            }

            // Perbarui visibilitas dan angka pada badge
            if (totalQty > 0) {
                badge.setVisible(true);
                badge.setNumber(totalQty);
            } else {
                badge.clearNumber();
                badge.setVisible(false);
            }
        } else {
            // Sembunyikan badge jika keranjang kosong atau null
            badge.clearNumber();
            badge.setVisible(false);
        }
    }
    @Override
    protected void onDestroy() {
        // Unregister receiver to prevent memory leaks
        if (cartUpdateReceiver != null) {
            unregisterReceiver(cartUpdateReceiver);
        }
        super.onDestroy();
    }
}