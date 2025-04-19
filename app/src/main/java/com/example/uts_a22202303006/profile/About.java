package com.example.uts_a22202303006.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uts_a22202303006.databinding.ActivityAboutBinding;

public class About extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        // Uncomment and set click listener for Website if needed
        // binding.website.setOnClickListener(v -> {
        //     Intent intent = new Intent(Intent.ACTION_VIEW);
        //     intent.setData(Uri.parse("https://kla.co.id/"));
        //     startActivity(intent);
        // });
    }
}