package com.example.uts_a22202303006.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.adapter.ViewPagerAdapter;
import com.example.uts_a22202303006.databinding.FragmentProductBinding;
import com.example.uts_a22202303006.product.AllProductsFragment;
import com.example.uts_a22202303006.product.BodyCareFragment;
import com.example.uts_a22202303006.product.HairCareFragment;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private ProductViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);

        // Setup image slider and other UI components first
//        ImageSlider imageSlider = binding.imageSlider;
//        ArrayList<SlideModel> slideModel = new ArrayList<>();
//        slideModel.add(new SlideModel(R.drawable.imageslider1, ScaleTypes.FIT));
//        slideModel.add(new SlideModel(R.drawable.imageslider2, ScaleTypes.FIT));
//        imageSlider.setImageList(slideModel);

        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        homeViewModel.fetchAllProducts("");
        homeViewModel.fetchBodyCareProducts("");
        homeViewModel.fetchHairCareProducts("");

        // Setup UI components
//        setupHeader();
        setupViewPager();
        setupSearch();

        // Check if we have arguments to set the tab
        if (getArguments() != null) {
            int selectedTab = getArguments().getInt("selected_tab", 0);
            binding.viewPager.setCurrentItem(selectedTab);
        }

        binding.getRoot().requestFocus();

        // Check for arguments first
        int selectedTab = 0;
        if (getArguments() != null) {
            selectedTab = getArguments().getInt("selected_tab", 0);
        } else {
            // If no arguments, check SharedPreferences
            SharedPreferences prefs = requireActivity().getSharedPreferences(
                    "app_prefs", Context.MODE_PRIVATE);
            selectedTab = prefs.getInt("selected_product_tab", 0);
            // Clear the preference after reading it
            prefs.edit().remove("selected_product_tab").apply();
        }

        // Set the selected tab
        int finalSelectedTab = selectedTab;
        binding.viewPager.post(() -> binding.viewPager.setCurrentItem(finalSelectedTab));

        return binding.getRoot();
    }

    private void setupSearch() {
        SearchView searchView = binding.toolbar.findViewById(R.id.searchView);
        View searchBarContainer = binding.toolbar.findViewById(R.id.search_bar);

        // Configure SearchView
        searchView.setIconified(true);
        searchView.setIconifiedByDefault(true);
        searchView.clearFocus();

        // Root view focus control
        View rootView = binding.LayoutProduct;
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();

        // Make the entire search bar clickable
        searchBarContainer.setOnClickListener(v -> {
            // Expand SearchView when clicking anywhere on the container
            searchView.setIconified(false);
            searchView.requestFocus();
            showKeyboard(searchView);
        });

        // Find the built-in close button
        int closeButtonId = searchView.getContext().getResources().getIdentifier(
                "android:id/search_close_btn", null, null);
        ImageView closeButton = searchView.findViewById(closeButtonId);

        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchView.clearFocus();
                hideKeyboard();
                homeViewModel.setSearchQuery("");
                rootView.requestFocus();
            });
        }

        // Query listeners remain unchanged
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                homeViewModel.setSearchQuery(query);
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchView.removeCallbacks(null);
                searchView.postDelayed(() -> {
                    if (searchView.getQuery().toString().equals(newText)) {
                        homeViewModel.setSearchQuery(newText);
                    }
                }, 300);
                return true;
            }
        });

        // Other listeners remain the same
        searchView.setOnSearchClickListener(v -> {
            searchView.setFocusable(true);
            searchView.setFocusableInTouchMode(true);
            searchView.requestFocus();
            showKeyboard(searchView);
        });

        searchView.setOnCloseListener(() -> {
            searchView.setQuery("", false);
            hideKeyboard();
            rootView.requestFocus();
            return false;
        });
    }

    // Improved keyboard showing method
    private void showKeyboard(View view) {
        view.postDelayed(() -> {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }, 100);
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

//    private void setupHeader() {
//        // Load logo
//        binding.logoImage.setImageResource(R.drawable.logo);
//
//        // Set welcome text with user's name
//        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE);
//        String nama = sharedPreferences.getString("nama", "Guest");
//        String email = sharedPreferences.getString("email", "Guest@gmail.com");
//        binding.welcomeText.setText("Selamat Datang, " + nama);
//        binding.txtEmail.setText(email);
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}