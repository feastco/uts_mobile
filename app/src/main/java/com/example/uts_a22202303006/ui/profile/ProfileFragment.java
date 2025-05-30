package com.example.uts_a22202303006.ui.profile;

import static com.example.uts_a22202303006.auth.LoginActivity.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.auth.LoginActivity;
import com.example.uts_a22202303006.databinding.FragmentProfileBinding;
import com.example.uts_a22202303006.orders.OrderHistoryActivity;
import com.example.uts_a22202303006.profile.About;
import com.example.uts_a22202303006.profile.EditProfile;
import com.example.uts_a22202303006.profile.ShippingAddressActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences prefs = getActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);
        String email = prefs.getString("email", "none");
        Log.d("ProfileFragment", "Login status check - userId: " + userId + ", email: " + email);

        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::refreshUserProfile);

        // Add click listener for shipping address
        binding.alamatPengiriman.setOnClickListener(v -> {
            // Check if user is logged in
            if (isLoggedIn()) {
                Intent intent = new Intent(getActivity(), ShippingAddressActivity.class);
                startActivity(intent);
            } else {
                // Show login required dialog
                showLoginRequiredDialog();
            }
        });

        // Load user profile initially
        loadUserProfile(false);

        // Set onClick listeners
        setupClickListeners();

        return root;
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("id", -1);

        // Add debug logging
        Log.d("ProfileFragment", "isLoggedIn check - userId: " + userId);

        return userId != -1;
    }

    private void showLoginRequiredDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Login Required")
                .setMessage("Please login to manage your shipping addresses")
                .setPositiveButton("Login", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshUserProfile() {
        // Force a refresh from server
        loadUserProfile(true);
    }

    private void loadUserProfile(boolean forceRefresh) {
        if (forceRefresh) {
            binding.swipeRefresh.setRefreshing(true);
        }

        // First load from SharedPreferences for immediate display
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences("login_session", Context.MODE_PRIVATE);
        String nama = sharedPreferences.getString("nama", "Guest");
        String email = sharedPreferences.getString("email", "Guest@gmail.com");
        String foto = sharedPreferences.getString("foto", "");
        String username = sharedPreferences.getString("username", "Guest");

        // Display data from SharedPreferences immediately
        binding.txtWelcome.setText("Selamat Datang\n" + nama);
        binding.txtEmail.setText(email);

        // Load profile image from SharedPreferences data
        loadProfileImage(foto);

        // Then fetch fresh data from server
        ServerAPI urlAPI = new ServerAPI();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getProfile(username).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (binding == null) return; // Fragment might be detached

                try {
                    if (response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        if ("1".equals(json.getString("result"))) {
                            JSONObject data = json.getJSONObject("data");

                            // Get updated data
                            String updatedNama = data.getString("nama");
                            String updatedEmail = data.getString("email");
                            String updatedFoto = data.optString("foto", "");

                            // Update UI with fresh data
                            binding.txtWelcome.setText("Selamat Datang\n" + updatedNama);
                            binding.txtEmail.setText(updatedEmail);

                            // Update SharedPreferences with new values
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("nama", updatedNama);
                            editor.putString("email", updatedEmail);
                            editor.putString("foto", updatedFoto);
                            editor.putString("alamat", data.optString("alamat", ""));
                            editor.putString("kota", data.optString("kota", ""));
                            editor.putString("provinsi", data.optString("provinsi", ""));
                            editor.putString("telp", data.optString("telp", ""));
                            editor.putString("kodepos", data.optString("kodepos", ""));
                            editor.apply();

                            // Load profile image from server response
                            loadProfileImage(updatedFoto);
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                // Stop the refresh animation
                binding.swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (binding != null) {
                    binding.swipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    // Helper method to load profile image with proper null/empty checks
    private void loadProfileImage(String foto) {
        if (foto != null && !foto.isEmpty()) {
            Glide.with(requireContext())
                    .load(ServerAPI.BASE_URL_IMAGE + foto)
                    .circleCrop()
                    .error(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .into(binding.imgProfile);
        } else {
            Glide.with(requireContext())
                    .load(R.drawable.profile)
                    .circleCrop()
                    .into(binding.imgProfile);
        }
    }

    private void setupClickListeners() {
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences("login_session", Context.MODE_PRIVATE);
        String nama = sharedPreferences.getString("nama", "Guest");
        String email = sharedPreferences.getString("email", "Guest@gmail.com");
        String foto = sharedPreferences.getString("foto", "");
        String username = sharedPreferences.getString("username", "Guest");

        binding.editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfile.class);
            intent.putExtra("email", email);
            intent.putExtra("nama", nama);
            intent.putExtra("foto", foto);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        binding.about.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), About.class);
            startActivity(intent);
        });
        
        binding.riwayatPemesanan.setOnClickListener(v -> {
            // Check if user is logged in
            if (isLoggedIn()) {
                Intent intent = new Intent(requireContext(), OrderHistoryActivity.class);
                startActivity(intent);
            } else {
                // Show login required dialog
                showLoginRequiredDialog();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            requireActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to the fragment
        loadUserProfile(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
