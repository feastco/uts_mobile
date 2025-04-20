package com.example.uts_a22202303006.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.bumptech.glide.Glide;
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

        Glide.with(this)
                .load("https://lh3.googleusercontent.com/gps-cs-s/AB5caB_wBCeZ11AdoutIN2rerj_ed5hawI7I4p2zW9tJRZzjfvSWzm5PHeEdTW8CH8vxrTlrZxZH4Z8dPiqTfUJx6sUYlos78wHeXDs55XvvVxJIwGiFDJI80ljXRKyHxR98YTwa-9xUNA=s680-w680-h510")
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

        // Uncomment and set click listener for Website if needed
        // binding.website.setOnClickListener(v -> {
        //     Intent intent = new Intent(Intent.ACTION_VIEW);
        //     intent.setData(Uri.parse("https://kla.co.id/"));
        //     startActivity(intent);
        // });
    }
}