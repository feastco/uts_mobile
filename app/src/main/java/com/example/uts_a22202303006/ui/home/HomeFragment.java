package com.example.uts_a22202303006.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.adapter.ViewPagerAdapter;
import com.example.uts_a22202303006.databinding.FragmentHomeBinding;
import com.example.uts_a22202303006.product.AllProductsFragment;
import com.example.uts_a22202303006.product.BodyCareFragment;
import com.example.uts_a22202303006.product.HairCareFragment;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Initialize ViewModel first to ensure data is available
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Initialize data loading for all categories
        homeViewModel.fetchAllProducts("");
        homeViewModel.fetchBodyCareProducts("");
        homeViewModel.fetchHairCareProducts("");

        // Setup UI components
        setupHeader();
        setupViewPager();
        setupSearch();

        // This prevents the SearchView from automatically taking focus
        binding.getRoot().requestFocus();

        return binding.getRoot();
    }

    private void setupSearch() {
        SearchView searchView = binding.toolbar.findViewById(R.id.searchView);

        // Prevent auto-focus on the SearchView
        searchView.setFocusable(false);
        searchView.setIconified(true);

        // Configure SearchView to properly handle focus
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // When search gains focus, adjust window to prevent bottom nav from rising
                if (requireActivity().getWindow() != null) {
                    requireActivity().getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
            } else {
                // Reset when focus is lost
                if (requireActivity().getWindow() != null) {
                    requireActivity().getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
            }
        });

        // Handle search interactions
        searchView.setOnSearchClickListener(v -> {
            // When search icon is clicked
            searchView.setIconified(false);
        });

        searchView.setOnCloseListener(() -> {
            // When search is closed, clear query and hide keyboard
            searchView.setQuery("", false);
            hideKeyboard();
            binding.getRoot().requestFocus();
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                homeViewModel.setSearchQuery(query);
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Add slight delay to avoid excessive API calls
                searchView.removeCallbacks(null);
                searchView.postDelayed(() -> {
                    if (searchView.getQuery().toString().equals(newText)) {
                        homeViewModel.setSearchQuery(newText);
                    }
                }, 300); // 300ms delay
                return true;
            }
        });
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new AllProductsFragment(), "Semua");
        viewPagerAdapter.addFragment(new BodyCareFragment(), "Perawatan Tubuh");
        viewPagerAdapter.addFragment(new HairCareFragment(), "Perawatan Rambut");

        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        // Force initial layout refresh
        binding.viewPager.setOffscreenPageLimit(2);
    }

    private void setupHeader() {
        // Load logo
        binding.logoImage.setImageResource(R.drawable.logo);

        // Set welcome text with user's name
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE);
        String nama = sharedPreferences.getString("nama", "Guest");
        String email = sharedPreferences.getString("email", "Guest@gmail.com");
        binding.welcomeText.setText("Selamat Datang, " + nama);
        binding.txtEmail.setText(email);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}