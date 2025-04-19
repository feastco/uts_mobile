package com.example.uts_a22202303006.adapter;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
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

        // Setup product card click listener
        holder.productLayout.setOnClickListener(v -> {
            ProductDetailDialog dialog = new ProductDetailDialog(product);
            dialog.show(fragment.getChildFragmentManager(), "ProductDetailDialog");
        });

        // Setup button click listeners
        holder.btnDetail.setOnClickListener(v -> {
            ProductDetailDialog dialog = new ProductDetailDialog(product);
            dialog.show(fragment.getChildFragmentManager(), "ProductDetailDialog");
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            if (product.getStok() <= 0) {
                Toasty.error(fragment.getContext(), "Produk tidak tersedia", Toast.LENGTH_SHORT, true).show();
                return;
            }

            // Get current cart from SharedPreferences
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
                // Create a deep copy of the product using Gson serialization/deserialization
//                Gson gson = new Gson();
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

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder untuk menyimpan referensi elemen UI
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct, imageViewStatus;
        TextView textViewMerk, textViewHargaJual, textViewHargaJualDiskon, textViewAvailability;
        ImageButton btnAddToCart, btnDetail;
        ConstraintLayout productLayout;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            textViewMerk = itemView.findViewById(R.id.textViewMerk);
            textViewHargaJual = itemView.findViewById(R.id.textViewHargaJual);
            textViewHargaJualDiskon = itemView.findViewById(R.id.textViewHargaJualDiskon);
            textViewAvailability = itemView.findViewById(R.id.textViewAvailability);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            productLayout = itemView.findViewById(R.id.product);
        }
    }

    // HomeViewModel
    public void setProductList(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }
}