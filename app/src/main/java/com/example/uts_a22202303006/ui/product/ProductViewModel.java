package com.example.uts_a22202303006.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uts_a22202303006.product.Product;
import com.example.uts_a22202303006.product.ProductResponse;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Kelas ViewModel untuk mengelola data UI secara terpisah dari lifecycle Activity/Fragment
public class ProductViewModel extends ViewModel {


    // MutableLiveData untuk menyimpan daftar produk
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> bodyCareProducts = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> hairCareProducts = new MutableLiveData<>();

    // Getter untuk mendapatkan daftar produk sebagai LiveData
    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }


    public LiveData<List<Product>> getBodyCareProducts() {
        return bodyCareProducts;
    }

    public LiveData<List<Product>> getHairCareProducts() {
        return hairCareProducts;
    }


    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    // Fungsi untuk mengambil semua produk berdasarkan query pencarian
    public void fetchAllProducts(String search) {
        fetchProducts("all", search, products);
        if (products.getValue() != null && search.equals(searchQuery.getValue())) {
            return;
        }
    }

    public void fetchBodyCareProducts(String search) {
        fetchProducts("Perawatan Tubuh", search, bodyCareProducts);
    }

    public void fetchHairCareProducts(String search) {
        fetchProducts("Perawatan Rambut", search, hairCareProducts);
    }

    // Menampilkan Produk berdasarkan kategori
    public void fetchProducts(String category, String search, MutableLiveData<List<Product>> targetLiveData) {

        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ProductResponse> call = apiService.getProducts(category, search);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    targetLiveData.setValue(response.body().getResult());
                } else {
                    errorMessage.setValue("Failed to load products");
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                errorMessage.setValue("Error: " + t.getMessage());
            }
        });
    }

}