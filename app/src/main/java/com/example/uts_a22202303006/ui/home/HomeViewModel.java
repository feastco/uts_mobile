package com.example.uts_a22202303006.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.denzcoskun.imageslider.models.SlideModel;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.product.Product;
import com.example.uts_a22202303006.product.ProductResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> popularProducts = new MutableLiveData<>();
    private final MutableLiveData<List<SlideModel>> sliderImages = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryItem>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public HomeViewModel() {
        // Initialize with default data
        initializeSliderImages();
        initializeCategories();
        loadPopularProducts();
    }

    public void loadAllData() {
        errorMessage.setValue(null);
         loadSliderImages();
         loadCategories();
         loadPopularProducts();
     }

    public LiveData<List<Product>> getPopularProducts() {
        return popularProducts;
    }

    public LiveData<List<SlideModel>> getSliderImages() {
        return sliderImages;
    }

    public LiveData<List<CategoryItem>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        // Reload products with search term
        loadProductsWithSearch(query);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void loadSliderImages() {
        isLoading.setValue(true);
        try {
            // Currently using local images - could be replaced with API call
            List<SlideModel> imageList = new ArrayList<>();
            imageList.add(new SlideModel(R.drawable.imageslider1, com.denzcoskun.imageslider.constants.ScaleTypes.CENTER_CROP));
            imageList.add(new SlideModel(R.drawable.imageslider2, com.denzcoskun.imageslider.constants.ScaleTypes.CENTER_CROP));
            sliderImages.setValue(imageList);
            isLoading.setValue(false);
        } catch (Exception e) {
            isLoading.setValue(false);
            String error = "Error loading slider images: " + e.getMessage();
            Log.e("HomeViewModel", error);
            errorMessage.setValue(error);
        }
    }

    public void loadCategories() {
        isLoading.setValue(true);
        try {
            // Currently using static categories - could be replaced with API call
            List<CategoryItem> categoryList = new ArrayList<>();
            categoryList.add(new CategoryItem("All Products", R.drawable.ic_product));
            categoryList.add(new CategoryItem("Body Care", R.drawable.ic_shopping_bag));
            categoryList.add(new CategoryItem("Hair Care", R.drawable.ic_price_tag));
            categories.setValue(categoryList);
            isLoading.setValue(false);
        } catch (Exception e) {
            isLoading.setValue(false);
            String error = "Error loading categories: " + e.getMessage();
            Log.e("HomeViewModel", error);
            errorMessage.setValue(error);
        }
    }

    private void initializeSliderImages() {
        List<SlideModel> imageList = new ArrayList<>();
        imageList.add(new SlideModel(R.drawable.imageslider1, com.denzcoskun.imageslider.constants.ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.imageslider2,  com.denzcoskun.imageslider.constants.ScaleTypes.CENTER_CROP));
        sliderImages.setValue(imageList);
    }

    private void initializeCategories() {
        List<CategoryItem> categoryList = new ArrayList<>();
        categoryList.add(new CategoryItem("All Products", R.drawable.ic_product));
        categoryList.add(new CategoryItem("Body Care", R.drawable.ic_shopping_bag));
        categoryList.add(new CategoryItem("Hair Care", R.drawable.ic_price_tag));
        categories.setValue(categoryList);
    }

    public void loadPopularProducts() {
        isLoading.setValue(true);
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ProductResponse> call = apiService.getProducts("all", "");

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getResult();

                    // Sort by visit count to get popular products
                    products.sort((p1, p2) -> p2.getVisitCount() - p1.getVisitCount());

                    // Take only first 4 products or fewer if less available
                    List<Product> popularProductsList = products.size() > 4 ?
                            products.subList(0, 4) : products;

                    popularProducts.setValue(popularProductsList);
                } else {
                    String error = "Failed to load products";
                    Log.e("HomeViewModel", error);
                    errorMessage.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                isLoading.setValue(false);
                String error = "Network error: " + t.getMessage();
                Log.e("HomeViewModel", error);
                errorMessage.setValue(error);
            }
        });
    }

    private void loadProductsWithSearch(String query) {
        isLoading.setValue(true);
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ProductResponse> call = apiService.getProducts("all", query);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getResult();

                    // Sort by visit count to get popular products
                    products.sort((p1, p2) -> p2.getVisitCount() - p1.getVisitCount());

                    // Take only first 4 products or fewer if less available
                    List<Product> popularProductsList = products.size() > 4 ?
                            products.subList(0, 4) : products;

                    popularProducts.setValue(popularProductsList);
                } else {
                    String error = "Failed to load products";
                    Log.e("HomeViewModel", error);
                    errorMessage.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                isLoading.setValue(false);
                String error = "Network error: " + t.getMessage();
                Log.e("HomeViewModel", error);
                errorMessage.setValue(error);
            }
        });
    }

    // Inner class to represent category items
    public static class CategoryItem {
        private final String name;
        private final int iconResId;

        public CategoryItem(String name, int iconResId) {
            this.name = name;
            this.iconResId = iconResId;
        }

        public String getName() {
            return name;
        }

        public int getIconResId() {
            return iconResId;
        }
    }
}