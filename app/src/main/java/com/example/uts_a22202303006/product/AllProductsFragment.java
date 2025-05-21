package com.example.uts_a22202303006.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

public class AllProductsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView textViewEmpty;
    private ProductAdapter productAdapter;
    private ProductViewModel productViewModel;
    private ShimmerFrameLayout shimmerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_fragment, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize adapter
        productAdapter = new ProductAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(productAdapter);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        // Start shimmer initially
        startShimmer();

        // Observe data products
        productViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            stopShimmer();

            if (products == null || products.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.VISIBLE);
            } else {
                textViewEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                productAdapter.setProductList(products);
            }
        });

        // Observe search query
        productViewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null) {
                startShimmer();
                productViewModel.fetchAllProducts(query);
            }
        });

        // Observe error message
        productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            stopShimmer();

            if (error != null) {
                textViewEmpty.setText("Gagal memuat data produk");
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        return view;
    }

    private void startShimmer() {
        if (shimmerLayout != null) {
            recyclerView.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.GONE);
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
        }
    }

    private void stopShimmer() {
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);

            // Create fade-in animation for smooth transition
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(1000);
            recyclerView.startAnimation(fadeIn);
        }
    }
}