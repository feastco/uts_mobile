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
    private static final int REQUEST_CHECKOUT = 1001;
    /**
     * Format harga ke dalam format Rupiah.
     */
    private String formatRupiah(int harga) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatRupiah.format(harga).replace(",00", "");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Auto-refresh cart data when returning to this fragment
        loadCartItems();
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inisialisasi ViewModel
        CartViewModel cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // Inflate layout fragment
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup swipe refresh
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        binding.swipeRefreshLayout.setOnRefreshListener(this::loadCartItems);

        // Load data from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("product", MODE_PRIVATE);

        // Initial load of cart items
        loadCartItems();

        // Set checkout button click listener
        binding.btnCheckout.setOnClickListener(v -> {
            // Check if user is fully logged in before allowing checkout
            if (!LoginRequiredManager.isFullyLoggedIn(requireContext())) {
                // Show a message explaining why login is required for checkout
                Toasty.info(requireContext(), "Silahkan login untuk melanjutkan proses checkout",
                        Toast.LENGTH_SHORT, true).show();

                // Show login dialog
                LoginRequiredManager.showLoginRequiredDialog(requireContext());
                return;
            }

            // Continue with checkout process if logged in
            proceedToCheckout();
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECKOUT) {
            // Always refresh cart when returning from checkout
            loadCartItems();

            // Force badge update
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCartBadge();
            }
        }
    }

    private void loadCartItems() {
        // Initialize adapter if needed
        if (adapter == null) {
            adapter = new CartAdapter(new ArrayList<>());
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
        }

        // Load data from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("product", MODE_PRIVATE);

        // Create new list instead of clearing existing one (prevents NPE)
        ArrayList<Product> cartItems = new ArrayList<>();

        if (sharedPreferences.contains("listproduct")) {
            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            if (jsonText != null && !jsonText.equals("[]")) {
                Product[] productArray = gson.fromJson(jsonText, Product[].class);
                for (Product product : productArray) {
                    cartItems.add(product);
                }
            }
        }

        // Update adapter with new list
        listproduct = cartItems;
        adapter = new CartAdapter(listproduct);
        binding.recyclerView.setAdapter(adapter);

        // Re-setup listeners
        adapter.setOnCartChangedListener(total -> {
            binding.tvTotalHarga.setText(formatRupiah(total));
        });

        adapter.setOnCartQtyChangedListener(totalQty -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCartBadge();
            }
        });

        // Update total price and badge
        adapter.notifyDataSetChanged();
        adapter.notifyCartTotalChanged();
        adapter.notifyCartQtyChanged();

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
        startActivityForResult(intent, REQUEST_CHECKOUT);
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