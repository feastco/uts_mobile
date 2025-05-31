package com.example.uts_a22202303006.adapter;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;

import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.auth.LoginRequiredManager;
import com.example.uts_a22202303006.product.ProductDetailActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import com.example.uts_a22202303006.MainActivity;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.product.Product;
import com.example.uts_a22202303006.product.ProductDetailDialog;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import android.util.Log;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;
import com.example.uts_a22202303006.api.ServerAPI;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public List<Product> productList;
    private Fragment fragment;

    // Constructor
    public ProductAdapter(Fragment fragment, List<Product> productList) {
        this.fragment = fragment;
        this.productList = productList;
    }

    // Fungsi format harga ke dalam bentuk Rupiah (Indonesia)
    private String formatRupiah(String harga) {
        try {
            double hargaDouble = Double.parseDouble(harga);
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return formatRupiah.format(hargaDouble).replace(",00", "");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "Rp. 0";
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout setiap item produk
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set data ke tampilan
        holder.textViewMerk.setText(product.getMerk());

        // Format harga
        String hargaJual = formatRupiah(String.valueOf(product.getHargaJual()));
        String hargaPokok = formatRupiah(String.valueOf(product.getHargapokok()));

        // Cek diskon
        if (product.getDiskonJual() > 0) {
            // Tampilkan harga asli dengan coretan dan harga diskon
            holder.textViewHargaJual.setPaintFlags(holder.textViewHargaJual.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textViewHargaJual.setTextSize(16);
            holder.textViewHargaJual.setTypeface(null, Typeface.NORMAL);
            holder.textViewHargaJual.setText(hargaPokok);
            holder.textViewHargaJualDiskon.setVisibility(View.VISIBLE);
            holder.textViewHargaJualDiskon.setText(hargaJual);
        } else {
            // Tanpa diskon
            holder.textViewHargaJual.setPaintFlags(holder.textViewHargaJual.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textViewHargaJual.setText(hargaJual);
            holder.textViewHargaJualDiskon.setVisibility(View.GONE);
        }

        // Cek stok (Sold Out)
        if (product.getStok() <= 0) {
            holder.imageViewStatus.setVisibility(View.VISIBLE);
            holder.imageViewProduct.setAlpha(0.5f); // Reduce opacity
            holder.textViewAvailability.setText("Tidak Tersedia");
            holder.textViewAvailability.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error));
        } else {
            holder.imageViewStatus.setVisibility(View.GONE);
            holder.imageViewProduct.setAlpha(1.0f);
            holder.textViewAvailability.setText("Tersedia");
            holder.textViewAvailability.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        }

        // Load gambar menggunakan Glide
        Glide.with(fragment.getContext())
                .load(product.getFoto())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imageViewProduct);

        // Display weight information
//        if (product.getWeight() > 0) {
//            holder.textViewWeight.setVisibility(View.VISIBLE);
//            holder.textViewWeight.setText(product.getWeight() + "g");
//        } else {
//            holder.textViewWeight.setVisibility(View.GONE);
//        }

        // Display initial visit count (might be 0 if not yet fetched)
        holder.textViewVisitCount.setText("Visited: " + product.getVisitCount());

        // Handle click on product
        // Handle click on product
        holder.productLayout.setOnClickListener(v -> {
            // Update visit count in database and show dialog AFTER count is updated
            updateVisitCount(product.getKode(), position, holder.textViewVisitCount);
            // Dialog creation is now handled in updateVisitCount after successful API call
        });

// Handle detail button click
        holder.btnDetail.setOnClickListener(v -> {
            // Update visit count in database and show dialog AFTER count is updated
            updateVisitCount(product.getKode(), position, holder.textViewVisitCount);
            // Dialog creation is now handled in updateVisitCount after successful API call
        });




        holder.btnAddToCart.setOnClickListener(v -> {

            if (product.getStok() <= 0) {
                Toasty.error(fragment.getContext(), "Produk tidak tersedia", Toast.LENGTH_SHORT, true).show();
                return;
            }

            // Remaining existing cart logic
            SharedPreferences sharedPreferences = fragment.requireActivity()
                    .getSharedPreferences("product", fragment.requireActivity().MODE_PRIVATE);

            Gson gson = new Gson();
            String jsonText = sharedPreferences.getString("listproduct", null);
            Type type = new TypeToken<ArrayList<Product>>() {}.getType();
            ArrayList<Product> listcart = gson.fromJson(jsonText, type);

            if (listcart == null) {
                listcart = new ArrayList<>();
            }

            // Check if product already exists in cart
            boolean alreadyExists = false;
            for (Product p : listcart) {
                if (p.getKode().equals(product.getKode())) {
                    if (p.getQty() >= product.getStok()) {
                        Toasty.warning(fragment.getContext(), "Stok tidak mencukupi", Toast.LENGTH_SHORT, true).show();
                        return;
                    }
                    p.setQty(p.getQty() + 1);
                    alreadyExists = true;
                    break;
                }
            }

            // Add new product to cart if it doesn't exist
            if (!alreadyExists) {
                Product cartProduct = gson.fromJson(gson.toJson(product), Product.class);
                cartProduct.setQty(1); // Set initial quantity to 1
                listcart.add(cartProduct);
            }

            // Save updated cart to SharedPreferences
            String updatedJson = gson.toJson(listcart);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("listproduct", updatedJson);
            editor.apply();

            // Update cart badge in MainActivity
            if (fragment.getActivity() instanceof MainActivity) {
                ((MainActivity) fragment.getActivity()).updateCartBadge();
            }

            Toasty.success(fragment.getContext(), "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT, true).show();
        });
    }

    // Show loading animation
    private void showLoading(View overlay) {
        ImageView loadingIcon = overlay.findViewById(R.id.loadingIcon);
        loadingIcon.setImageResource(R.drawable.loading_animation);

        // Create rotation animation
        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());

        loadingIcon.startAnimation(rotate);
        overlay.setVisibility(View.VISIBLE);
    }

    public void updateVisitCount(String productCode, int position, TextView textViewVisitCount) {
        RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
        Call<ResponseBody> call = apiService.updateVisitCount(productCode);

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
                            Log.d("ProductAdapter", "Visit count updated to: " + visitCount);

                            // Update the product in the list
                            if (position < productList.size()) {
                                Product product = productList.get(position);
                                product.setVisitCount(visitCount);

                                // Update UI on the main thread
                                fragment.requireActivity().runOnUiThread(() -> {
                                    // Update visit count in list item
                                    textViewVisitCount.setText("Visited: " + visitCount);
                                    textViewVisitCount.setTextColor(ContextCompat.getColor(
                                            fragment.requireContext(), R.color.primary));

                                    // Navigate to ProductDetailActivity with flag to prevent second update
                                    Intent intent = new Intent(fragment.requireContext(), ProductDetailActivity.class);
                                    intent.putExtra("PRODUCT_CODE", product.getKode());
                                    intent.putExtra("FROM_SOURCE", "PRODUCT_FRAGMENT");
                                    intent.putExtra("COUNT_ALREADY_UPDATED", true); // Add flag to prevent double counting
                                    fragment.startActivity(intent);
                                });
                            }
                        } else {
                            navigateToProductDetail(productList.get(position), false);
                        }
                    } catch (Exception e) {
                        Log.e("ProductAdapter", "Error parsing response: " + e.getMessage());
                        navigateToProductDetail(productList.get(position), false);
                    }
                } else {
                    navigateToProductDetail(productList.get(position), false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ProductAdapter", "Network error: " + t.getMessage());
                navigateToProductDetail(productList.get(position), false);
            }
        });
    }

    // Updated helper method to navigate with countUpdated flag
    private void navigateToProductDetail(Product product, boolean countUpdated) {
        fragment.requireActivity().runOnUiThread(() -> {
            Intent intent = new Intent(fragment.requireContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_CODE", product.getKode());
            intent.putExtra("FROM_SOURCE", "PRODUCT_FRAGMENT");
            intent.putExtra("COUNT_ALREADY_UPDATED", countUpdated);
            fragment.startActivity(intent);
        });
    }

    // Helper method to show dialog regardless of API success/failure
    private void showProductDetailDialog(Product product) {
        fragment.requireActivity().runOnUiThread(() -> {
            ProductDetailDialog dialog = new ProductDetailDialog(product);
            dialog.show(fragment.getChildFragmentManager(), "ProductDetailDialog");
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder untuk menyimpan referensi elemen UI
    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewVisitCount;
        ImageView imageViewProduct, imageViewStatus;
        TextView textViewMerk, textViewHargaJual, textViewHargaJualDiskon, textViewAvailability;
//        TextView textViewWeight; // Added weight TextView
        ImageButton btnAddToCart, btnDetail;
        public ConstraintLayout productLayout;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            textViewMerk = itemView.findViewById(R.id.textViewMerk);
            textViewHargaJual = itemView.findViewById(R.id.textViewHargaJual);
            textViewHargaJualDiskon = itemView.findViewById(R.id.textViewHargaJualDiskon);
            textViewAvailability = itemView.findViewById(R.id.textViewAvailability);
            textViewVisitCount = itemView.findViewById(R.id.textViewVisitCount);
//            textViewWeight = itemView.findViewById(R.id.textViewWeight); // Initialize weight TextView
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            productLayout = itemView.findViewById(R.id.product);
        }
    }

    // Update product list and notify adapter
    public void setProductList(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }
}
