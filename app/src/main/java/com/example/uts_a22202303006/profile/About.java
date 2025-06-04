package com.example.uts_a22202303006.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.databinding.ActivityAboutBinding;
import com.example.uts_a22202303006.utils.AppConfig;

public class About extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load image from drawable resources (reverted to original)
        Glide.with(this)
                .load(R.drawable.store)
                .into(binding.imgTentangKami);

        // Set click listener for ivBack
        binding.ivBack.setOnClickListener(v -> finish());

        // Set click listener for WhatsApp
        binding.whatsapp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send/?phone=628112919195&text&type=phone_number&app_absent=0"));
            startActivity(intent);
        });

        // Set click listener for Instagram
        binding.instagram.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.instagram.com/elishamart_kosmetik?utm_source=ig_web_button_share_sheet&igsh=ZDNlZDc0MzIxNw=="));
            startActivity(intent);
        });
        
        // Set click listener for Google Maps button
        binding.maps.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://maps.app.goo.gl/Yxm2pV5A9abamErf9"));
            startActivity(intent);
        });

        // Set click listener for Google Maps link in address
        binding.txtAddressDetails.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://maps.app.goo.gl/Yxm2pV5A9abamErf9"));
            startActivity(intent);
        });
    }

    public static int getOriginId() {
        return AppConfig.STORE_CITY_ID; // Use centralized store city ID
    }
}
