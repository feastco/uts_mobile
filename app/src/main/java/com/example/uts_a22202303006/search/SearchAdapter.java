package com.example.uts_a22202303006.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.product.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.product.ProductDetailActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<Product> productList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public SearchAdapter(OnProductClickListener listener) {
        this.listener = listener;
        this.productList = new ArrayList<>();
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView productCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    // Call updateVisitCount instead of listener
                    updateVisitCount(productList.get(position), position);
                }
            });
        }

        public void bind(Product product) {
            // Keep existing binding code unchanged
            productName.setText(product.getMerk());

            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String formattedPrice = currencyFormatter.format(product.getHargaJual());
            productPrice.setText(formattedPrice);

            productCategory.setText(product.getKategori());

            if (product.getFoto() != null && !product.getFoto().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getFoto())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.placeholder_image);
            }
        }

        public void updateVisitCount(Product product, int position) {
            RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
            Call<ResponseBody> call = apiService.updateVisitCount(product.getKode());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            org.json.JSONObject jsonObject = new org.json.JSONObject(responseData);

                            if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {
                                // Get updated visit count
                                int visitCount = jsonObject.optInt("visit_count", 0);
                                Log.d("SearchAdapter", "Visit count updated to: " + visitCount);

                                // Update product locally
                                product.setVisitCount(visitCount);

                                // Navigate to product detail with flag to prevent second update
                                navigateToProductDetail(product, itemView, true);
                            } else {
                                // If API fails, still navigate but without setting the flag
                                navigateToProductDetail(product, itemView, false);
                            }
                        } catch (Exception e) {
                            Log.e("SearchAdapter", "Error parsing response: " + e.getMessage());
                            navigateToProductDetail(product, itemView, false);
                        }
                    } else {
                        navigateToProductDetail(product, itemView, false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("SearchAdapter", "Network error: " + t.getMessage());
                    navigateToProductDetail(product, itemView, false);
                }
            });
        }

        private void navigateToProductDetail(Product product, View itemView, boolean countUpdated) {
            Intent intent = new Intent(itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_CODE", product.getKode());
            intent.putExtra("FROM_SOURCE", "SEARCH_ACTIVITY");
            intent.putExtra("COUNT_ALREADY_UPDATED", countUpdated); // Add flag to prevent double counting
            itemView.getContext().startActivity(intent);
        }
    }
}