package com.example.uts_a22202303006.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.adapter.ProductAdapter;
import com.example.uts_a22202303006.product.AllProductsFragment;
import com.example.uts_a22202303006.product.BodyCareFragment;
import com.example.uts_a22202303006.product.HairCareFragment;
import com.example.uts_a22202303006.ui.product.ProductFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private ImageSlider imageSlider;
    private LinearLayout layoutCategories;
    private RecyclerView recyclerViewPopularProducts;
    private ProductAdapter productAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationTextView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Configure refresh layout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Call the correctly named method on the correctly named variable
            viewModel.refreshData();
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Observe error state
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            // Always stop refreshing on error
            swipeRefreshLayout.setRefreshing(false);

            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize views
        imageSlider = view.findViewById(R.id.imageSlider);
        layoutCategories = view.findViewById(R.id.layoutCategories);
        recyclerViewPopularProducts = view.findViewById(R.id.recyclerViewPopularProducts);

        setupPopularProducts();

        return view; // Return the view first
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Call these methods after the view is created
        setupHeader();
        observeViewModel();

        // Initialize location components
        locationTextView = view.findViewById(R.id.textViewLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Request location permissions and get location
        checkLocationPermission();

        // Initialize swipe refresh layout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Set refresh colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );

        // Configure SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Rest of your existing setup code
        setupHeader();
        setupPopularProducts();
        checkLocationPermission();
        observeViewModel();

        // Load initial data
        viewModel.loadAllData();
    }

    private void refreshData() {
        // Refresh ViewModel data
        viewModel.loadAllData();

        // Refresh user location
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationTextView.setText("Memuat ulang lokasi...");
            getCurrentLocation();
        } else {
            locationTextView.setText("Izin lokasi tidak diberikan");
        }

        // Stop the refresh animation after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500); // 1.5 seconds delay for better UX
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, get location
            getCurrentLocation();
        } else {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

//    private void checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            // Permission already granted, get location
//            getCurrentLocation();
//        } else {
//            // Request permission
//            ActivityCompat.requestPermissions(requireActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                locationTextView.setText("Lokasi tidak tersedia");
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getAddressFromLocation(location);
                    } else {
                        // If last location is null, request location updates
                        requestNewLocationData();
                    }
                })
                .addOnFailureListener(e -> locationTextView.setText("Gagal mendapatkan lokasi"));
    }

    private void requestNewLocationData() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(10000)
                .setMaxUpdateDelayMillis(60000)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    getAddressFromLocation(location);
                    // Remove updates after getting location
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String subLocality = address.getSubLocality();
                String locality = address.getLocality();
                String adminArea = address.getAdminArea();

                // First try to show subLocality + adminArea
                if (subLocality != null && !subLocality.isEmpty() && adminArea != null) {
                    locationTextView.setText(subLocality + ", " + adminArea);
                }
                // Then try locality + adminArea
                else if (locality != null && !locality.isEmpty() && adminArea != null) {
                    locationTextView.setText(locality + ", " + adminArea);
                }
                // Fallback to just adminArea
                else if (adminArea != null) {
                    locationTextView.setText(adminArea);
                } else {
                    locationTextView.setText("Lokasi ditemukan");
                }
            } else {
                locationTextView.setText("Alamat tidak ditemukan");
            }
        } catch (IOException e) {
            locationTextView.setText("Gagal mendapatkan alamat");
        }
    }

    private String getTimeBasedGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 5 && hourOfDay < 12) {
            return "Selamat Pagi Elma";
        } else if (hourOfDay >= 12 && hourOfDay < 15) {
            return "Selamat Siang Elma";
        } else if (hourOfDay >= 15 && hourOfDay < 19) {
            return "Selamat Sore Elma";
        } else {
            return "Selamat Malam Elma";
        }
    }

    private void setupHeader() {
        // Get view safely instead of requiring it
        View view = getView();
        if (view == null) return;

        // Load logo
        ImageView logoImage = view.findViewById(R.id.logoImage);
        if (logoImage != null) {
            // Check if this resource exists in the correct folder
            logoImage.setImageResource(R.drawable.logo);
        }


        // Update brand text with time-based greeting
        TextView timeText = view.findViewById(R.id.timeText);
        if (timeText != null) {
            timeText.setText(getTimeBasedGreeting());
        }

        // Set welcome text
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE);
        String nama = sharedPreferences.getString("nama", "Guest");

        TextView welcomeText = view.findViewById(R.id.textViewWelcome);
        if (welcomeText != null) {
            welcomeText.setText("Hai, " + nama);
        }
    }

    private void setupPopularProducts() {
        // Set up RecyclerView
        recyclerViewPopularProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Create adapter with empty list first
        productAdapter = new ProductAdapter(this, new ArrayList<>());
        recyclerViewPopularProducts.setAdapter(productAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle back press in child fragment manager
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                    getChildFragmentManager().popBackStack();
                    getView().findViewById(R.id.homeContent).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.productContainer).setVisibility(View.GONE);
                } else {
                    this.remove();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void observeViewModel() {
        // Slider image observer remains unchanged
        viewModel.getSliderImages().observe(getViewLifecycleOwner(), slideModels -> {
            imageSlider.setImageList(slideModels);

            imageSlider.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemSelected(int position) {
                    Toasty.info(requireContext(), "Promo " + (position + 1) + " clicked",
                            Toast.LENGTH_SHORT, true).show();
                }

                @Override
                public void doubleClick(int position) {
                    Toasty.info(requireContext(), "Promo " + (position + 1) + " double clicked",
                            Toast.LENGTH_SHORT, true).show();
                }
            });
        });

        // Category observer with improved navigation
        viewModel.getCategories().observe(getViewLifecycleOwner(), categoryItems -> {
            layoutCategories.removeAllViews();

            for (int i = 0; i < categoryItems.size(); i++) {
                View categoryView = getLayoutInflater().inflate(R.layout.item_category, layoutCategories, false);
                ImageView imageView = categoryView.findViewById(R.id.imageViewCategory);
                TextView textView = categoryView.findViewById(R.id.textViewCategory);

                HomeViewModel.CategoryItem item = categoryItems.get(i);
                imageView.setImageResource(item.getIconResId());
                textView.setText(item.getName());

                categoryView.setOnClickListener(v -> {
                    String category = item.getName();

                    // Set the tab index based on category
                    int tabIndex = 0; // Default to All Products tab
                    switch (category) {
                        case "All Products":
                            tabIndex = 0;
                            break;
                        case "Body Care":
                            tabIndex = 1;
                            break;
                        case "Hair Care":
                            tabIndex = 2;
                            break;
                    }

                    // Store selected tab in SharedPreferences for ProductFragment to read
                    SharedPreferences prefs = requireActivity().getSharedPreferences(
                            "app_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putInt("selected_product_tab", tabIndex).apply();

                    // Navigate to Product tab in bottom navigation
                    requireActivity().findViewById(R.id.navigation_product).performClick();

                    // Show feedback to user
                    Toasty.info(requireContext(), category,
                            Toast.LENGTH_SHORT, true).show();
                });

                layoutCategories.addView(categoryView);
            }
        });

        // Keep the existing product and error observers
        viewModel.getPopularProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.setProductList(products);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toasty.error(requireContext(), errorMessage, Toast.LENGTH_SHORT, true).show();
            }
        });
    }
}