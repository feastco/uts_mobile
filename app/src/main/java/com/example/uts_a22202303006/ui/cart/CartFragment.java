package com.example.uts_a22202303006.ui.cart;

import static android.content.Context.MODE_PRIVATE;

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
import com.example.uts_a22202303006.databinding.FragmentCartBinding;
import com.example.uts_a22202303006.product.Product;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding; // Binding untuk mengakses komponen UI
    private SharedPreferences sharedPreferences; // SharedPreferences untuk menyimpan data keranjang

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
//            proceedToCheckout();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Hindari memory leak dengan menghapus binding
    }

}