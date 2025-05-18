package com.example.uts_a22202303006;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.uts_a22202303006.databinding.ActivityMainBinding;
import com.example.uts_a22202303006.product.Product;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // Binding untuk mengakses komponen UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout menggunakan ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup BottomNavigationView dan Navigation
        setupNavigation();

        // Inisialisasi dan perbarui badge keranjang
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
        NavigationUI.setupWithNavController(binding.navView, navController);
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
}