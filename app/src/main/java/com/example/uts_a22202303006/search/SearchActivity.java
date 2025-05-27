package com.example.uts_a22202303006.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.product.Product;
import com.example.uts_a22202303006.product.ProductDetailActivity;
import com.example.uts_a22202303006.product.ProductResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchActivity extends AppCompatActivity {

    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewResults;
    private View emptyStateView, progressBar, btnBack;
    private SearchAdapter adapter;
    private CompositeDisposable disposables = new CompositeDisposable();
    private PublishSubject<String> searchSubject = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupViews();
        setupSearch();
    }

    private void setupViews() {
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        emptyStateView = findViewById(R.id.emptyStateView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        adapter = new SearchAdapter(product -> {
            // Handle product click - Navigate to detail
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_CODE", product.getKode());
            intent.putExtra("FROM_SEARCH", true);  // Change this to FROM_SOURCE for consistency
            startActivity(intent);
        });

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(adapter);

        // Setup search EditText
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = editTextSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        // Add text change listener
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSubject.onNext(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Auto-focus and show keyboard
        editTextSearch.requestFocus();
        showKeyboard();
    }

    private void setupSearch() {
        // Use RxJava to debounce search input
        disposables.add(searchSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .filter(s -> s.length() >= 2 || s.isEmpty())
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> {
                    if (query.isEmpty()) {
                        showEmptyState();
                    } else {
                        performSearch(query);
                    }
                }, Throwable::printStackTrace));
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            showEmptyState();
            return;
        }

        showLoading();

        // Call API to search products
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ProductResponse> call = apiService.getProducts("all", query);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> results = response.body().getResult();

                    if (results.isEmpty()) {
                        showNoResults();
                    } else {
                        showResults(results);
                    }
                } else {
                    showNoResults();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showNoResults();
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewResults.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerViewResults.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
    }

    private void showNoResults() {
        progressBar.setVisibility(View.GONE);
        recyclerViewResults.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        // Add your no results implementation here if needed
    }

    private void showResults(List<Product> products) {
        progressBar.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.GONE);
        recyclerViewResults.setVisibility(View.VISIBLE);
        adapter.setProductList(products);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }
}