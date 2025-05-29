package com.example.uts_a22202303006.ui.cart;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202303006.MainActivity;
import com.example.uts_a22202303006.adapter.CartAdapter;
import com.example.uts_a22202303006.auth.LoginRequiredManager;
import com.example.uts_a22202303006.checkout.CheckoutActivity;
import com.example.uts_a22202303006.databinding.FragmentCartBinding;
import com.example.uts_a22202303006.product.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding; // Binding untuk mengakses komponen UI
    private SharedPreferences sharedPreferences; // SharedPreferences untuk menyimpan data keranjang
    private CartAdapter adapter;
    private ArrayList<Product> listproduct;

    /**
     * Format harga ke dalam format Rupiah.
     */
    private String formatRupiah(int harga) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatRupiah.format(harga).replace(",00", "");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inisialisasi ViewModel
        CartViewModel cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // Inflate layout fragment
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // List untuk menyimpan produk dari keranjang
        ArrayList<Product> listproduct = new ArrayList<>();

        // Setup swipe refresh
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        binding.swipeRefreshLayout.setOnRefreshListener(this::loadCartItems);

        // Load data keranjang dari SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("product", MODE_PRIVATE);
        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            Product[] productArray = gson.fromJson(jsonText, Product[].class);
            for (Product product : productArray) {
                listproduct.add(product);
            }
            Log.i("Info pref", listproduct.toString());
        }

        // Inisialisasi adapter untuk RecyclerView
        CartAdapter adapter = new CartAdapter(listproduct);
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setAdapter(adapter);

        // Listener untuk memperbarui total harga
        adapter.setOnCartChangedListener(total -> {
            TextView tvTotalHarga = binding.tvTotalHarga;
            tvTotalHarga.setText(formatRupiah(total));
        });

        // Listener untuk memperbarui badge jumlah produk
        adapter.setOnCartQtyChangedListener(totalQty -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCartBadge();
            }
        });

        // Perbarui total harga saat data di-load
        adapter.notifyCartTotalChanged();

        // Set checkout button click listener
        binding.btnCheckout.setOnClickListener(v -> {
            // Check if user is fully logged in before allowing checkout
            if (!LoginRequiredManager.isFullyLoggedIn(requireContext())) {
                // Show a message explaining why login is required for checkout
                Toasty.info(requireContext(), "Silahkan login untuk melanjutkan proses checkout", Toast.LENGTH_SHORT, true).show();

                // Show login dialog
                LoginRequiredManager.showLoginRequiredDialog(requireContext());
                return;
            }

            // Continue with checkout process if logged in
            proceedToCheckout();
        });

        return root;
    }

    private void loadCartItems() {
        listproduct.clear();

        // Load data from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("product", MODE_PRIVATE);
        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            Product[] productArray = gson.fromJson(jsonText, Product[].class);
            for (Product product : productArray) {
                listproduct.add(product);
            }
            Log.i("Info pref", listproduct.toString());
        }

        // Initialize adapter if needed
        if (adapter == null) {
            adapter = new CartAdapter(listproduct);
            binding.recyclerView.setHasFixedSize(true);
            binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            binding.recyclerView.setAdapter(adapter);

            // Setup listeners
            adapter.setOnCartChangedListener(total -> {
                binding.tvTotalHarga.setText(formatRupiah(total));
            });

            adapter.setOnCartQtyChangedListener(totalQty -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateCartBadge();
                }
            });
        } else {
            adapter.notifyDataSetChanged();
        }

        // Update total price
        adapter.notifyCartTotalChanged();

        // Stop the refresh animation
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void proceedToCheckout() {
        // Get cart items
        ArrayList<Product> cartItems = getCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            Toasty.warning(requireContext(), "Keranjang kosong", Toast.LENGTH_SHORT, true).show();
            return;
        }

        // Start CheckoutActivity with cart items
        Intent intent = new Intent(getActivity(), CheckoutActivity.class);
        intent.putExtra("CART_TOTAL", calculateCartTotal(cartItems));
        startActivity(intent);
    }

    private ArrayList<Product> getCartItems() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("product", MODE_PRIVATE);
        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            return gson.fromJson(jsonText, new TypeToken<ArrayList<Product>>(){}.getType());
        }
        return new ArrayList<>();
    }

    private double calculateCartTotal(ArrayList<Product> cartItems) {
        double total = 0;
        for (Product p : cartItems) {
            total += p.getHargaJual() * p.getQty();
        }
        return total;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Hindari memory leak dengan menghapus binding
    }

}