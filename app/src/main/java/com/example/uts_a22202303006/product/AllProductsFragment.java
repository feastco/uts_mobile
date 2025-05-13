package com.example.uts_a22202303006.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.adapter.ProductAdapter;
import com.example.uts_a22202303006.ui.product.ProductViewModel;

import java.util.ArrayList;

public class AllProductsFragment extends Fragment {

    // RecyclerView untuk menampilkan daftar produk
    private RecyclerView recyclerView;
    // TextView untuk menampilkan pesan jika data kosong
    private TextView textViewEmpty;
    // Adapter untuk mengelola data produk
    private ProductAdapter productAdapter;
    // ViewModel untuk mengelola data produk
    private ProductViewModel productViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout untuk fragment
        View view = inflater.inflate(R.layout.product_fragment, container, false);

        // Inisialisasi RecyclerView dan TextView
        recyclerView = view.findViewById(R.id.recyclerView);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Inisialisasi adapter produk
        productAdapter = new ProductAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(productAdapter);

        // Inisialisasi ViewModel
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        // Observasi data produk
        productViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products == null || products.isEmpty()) {
                // Tampilkan pesan jika data kosong
                recyclerView.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.VISIBLE);
            } else {
                // Tampilkan data produk
                textViewEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                productAdapter.setProductList(products);
            }
        });

        // Observasi query pencarian
        productViewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null) {
                productViewModel.fetchAllProducts(query); // Ambil data berdasarkan query
            }
        });

        // Observasi pesan error
        productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                textViewEmpty.setText("Gagal memuat data produk");
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        return view;
    }
}
