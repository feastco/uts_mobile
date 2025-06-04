package com.example.uts_a22202303006.adapter;

import android.content.Context;
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

public class CheckoutProductAdapter extends RecyclerView.Adapter<CheckoutProductAdapter.ViewHolder> {

    private final Context context;
    private List<Product> products;

    public CheckoutProductAdapter(Context context) {
        this.context = context;
        this.products = new ArrayList<>();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        
        // Set product name
        holder.tvProductName.setText(product.getMerk());
        
        // Format and set price
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedPrice = formatRupiah.format(product.getHargaJual()).replace(",00", "");
        holder.tvProductPrice.setText(formattedPrice);
        
        // Set quantity
        holder.tvProductQuantity.setText("x" + product.getQty());
        
        // Set weight information
        if (holder.tvWeight != null) {
            int itemWeight = product.getWeight();
            int totalWeight = itemWeight * product.getQty();
            holder.tvWeight.setText(totalWeight + " gram");
            holder.tvWeight.setVisibility(View.VISIBLE);
        }
        
        // Load product image correctly using same method as CartAdapter
        Glide.with(holder.itemView.getContext())
                .load(product.getFoto())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductQuantity;
        TextView tvWeight; // Added weight TextView reference

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvWeight = itemView.findViewById(R.id.tvWeight); // Initialize weight TextView
        }
    }
}