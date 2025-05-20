package com.example.uts_a22202303006.profile;

import static com.example.uts_a22202303006.auth.LoginActivity.URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303006.MainActivity;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.databinding.ActivityEditProfileBinding;
import com.example.uts_a22202303006.ui.profile.ProfileFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfile extends AppCompatActivity {

    private String username, nama, foto;

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private String currentPhotoPath;

    private SwipeRefreshLayout swipeRefreshLayout;

    ImageView ivBack, imgProfile;
    TextView btnChangePhoto;
    EditText etProfile_Nama, etProfile_Email, etProfile_Alamat, etProfile_Kota, etProfile_Provinsi, etProfile_Telp, etProfile_Kodepos;
    Button btnSubmit;
    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_edit_profile);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nama = getIntent().getStringExtra("nama");
        username = getIntent().getStringExtra("username");
        // Get latest photo from SharedPreferences instead of Intent
        SharedPreferences sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE);
        foto = sharedPreferences.getString("foto", "");

        // Inisialisasi UI
        ivBack = findViewById(R.id.ivBack);
        imgProfile = findViewById(R.id.imgProfile);
        btnChangePhoto = findViewById(R.id.btnChangeImage);
        etProfile_Nama = findViewById(R.id.etProfile_Nama);
        etProfile_Email = findViewById(R.id.etProfile_Email);
        etProfile_Alamat = findViewById(R.id.etProfile_Alamat);
        etProfile_Kota = findViewById(R.id.etProfile_Kota);
        etProfile_Provinsi = findViewById(R.id.etProfile_Province);
        etProfile_Telp = findViewById(R.id.etProfile_Telp);
        etProfile_Kodepos = findViewById(R.id.etProfile_Kodepos);
        btnSubmit = findViewById(R.id.btnSubmit);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        // Make email field non-editable
        etProfile_Email.setEnabled(false);
        etProfile_Email.setFocusable(false);
        etProfile_Email.setTextColor(getResources().getColor(R.color.text_secondary));

        // Set refresh colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.secondary,
                R.color.accent
        );

        // Set refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload profile data when swipe refresh is triggered
            getProfil(username);
        });

        // Load the image from SharedPreferences (most up-to-date)
        loadProfileImage();

        // Get latest profile data from server
        getProfil(username);

        btnChangePhoto.setOnClickListener(v -> showImagePickerDialog());

        btnSubmit.setOnClickListener(v -> updateProfil());

        binding.ivBack.setOnClickListener(v -> finish());

    }

    // Add this helper method to load the profile image
    private void loadProfileImage() {
        if (foto != null && !foto.isEmpty()) {
            Glide.with(this)
                    .load(ServerAPI.BASE_URL_IMAGE + foto)
                    .circleCrop()
                    .error(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .into(imgProfile);
        } else {
            Glide.with(this)
                    .load(R.drawable.profile)
                    .circleCrop()
                    .into(imgProfile);
        }
    }

//    private void navigateToHome() {
//        Intent intent = new Intent(EditProfile.this, ProfileFragment.class);
//        startActivity(intent);
//        finish();
//    }

    private void getProfil(String vusername) {
        // Show refresh animation if not already showing
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        ServerAPI urlAPI = new ServerAPI();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getProfile(vusername).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        if ("1".equals(json.getString("result"))) {
                            JSONObject data = json.getJSONObject("data");
                            etProfile_Nama.setText(getValidString(data, "nama"));
                            etProfile_Email.setText(getValidString(data, "email"));
                            etProfile_Alamat.setText(getValidString(data, "alamat"));
                            etProfile_Kota.setText(getValidString(data, "kota"));
                            etProfile_Provinsi.setText(getValidString(data, "provinsi"));
                            etProfile_Telp.setText(getValidString(data, "telp"));
                            etProfile_Kodepos.setText(getValidString(data, "kodepos"));
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    // Stop refresh animation
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                // Stop refresh animation on failure as well
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateProfil() {
        DataUser data = new DataUser();
        data.setNama(etProfile_Nama.getText().toString().trim());
        data.setEmail(etProfile_Email.getText().toString().trim());
        data.setAlamat(etProfile_Alamat.getText().toString().trim());
        data.setKota(etProfile_Kota.getText().toString().trim());
        data.setProvinsi(etProfile_Provinsi.getText().toString().trim());
        data.setTelp(etProfile_Telp.getText().toString().trim());
        data.setKodepos(etProfile_Kodepos.getText().toString().trim());
        data.setUsername(username);

        ServerAPI urlAPI = new ServerAPI();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.updateProfile(data.getNama(), data.getAlamat(), data.getKota(), data.getProvinsi(),
                        data.getTelp(), data.getKodepos(),foto, data.getEmail(),  data.getUsername())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.body() != null) {
                                JSONObject json = new JSONObject(response.body().string());
                                String message = json.getString("message");
                                String result = json.getString("result");
                                if ("1".equals(result)) {
                                    Toasty.success(EditProfile.this, message, Toast.LENGTH_SHORT, true).show();
                                } else {
                                    Toasty.error(EditProfile.this, message, Toast.LENGTH_SHORT, true).show();
                                }

                                if (json.getString("result").equals("1")) {
                                    // Update SharedPreferences with new values
                                    SharedPreferences sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("nama", data.getNama());
                                    editor.putString("email", data.getEmail());
                                    editor.putString("alamat", data.getAlamat());
                                    editor.putString("kota", data.getKota());
                                    editor.putString("provinsi", data.getProvinsi());
                                    editor.putString("telp", data.getTelp());
                                    editor.putString("kodepos", data.getKodepos());
                                    editor.apply();

                                    // Return to previous screen
                                    finish();
                                }
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        new AlertDialog.Builder(EditProfile.this)
                                .setMessage("Simpan Gagal, Error: " + t.toString())
                                .setNegativeButton("Retry", null)
                                .create()
                                .show();
                    }
                });
    }

    //Fungsi untuk mendapatkan string valid dari JSON
    private String getValidString(JSONObject json, String key) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getString(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Gambar");
        builder.setItems(new CharSequence[]{"Galeri", "Kamera"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    openGallery();
                    break;
                case 1:
                    openCamera();
                    break;
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        // Check for camera permission first
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
        }

        // Proceed with camera intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toasty.error(this, "Error creating image file", Toast.LENGTH_SHORT, true).show();
                ex.printStackTrace();
                return;
            }

            // Continue only if the file was successfully created
            if (photoFile != null) {
                try {
                    Uri photoURI = FileProvider.getUriForFile(
                            this,
                            "com.example.uts_a22202303006.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                } catch (Exception e) {
                    Toasty.error(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    Log.e("Camera", "FileProvider error", e);
                }
            }
        } else {
            Toasty.error(this, "No camera app found", Toast.LENGTH_SHORT, true).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open camera
                openCamera();
            } else {
                Toasty.error(this, "Camera permission required", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    imgProfile.setImageBitmap(bitmap);
                    uploadImage(selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST) {
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imgProfile.setImageBitmap(bitmap);
                    uploadImage(Uri.fromFile(imgFile));
                }
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        try {
            if (username == null || username.isEmpty()) {
                Toasty.error(this, "Username tidak ditemukan", Toast.LENGTH_SHORT, true).show();
                return;
            }

            // Show progress
            swipeRefreshLayout.setRefreshing(true);

            // Get bitmap and resize it
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Calculate new dimensions while maintaining aspect ratio
            int maxDimension = 1200; // Max width or height
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float ratio = Math.min((float)maxDimension / width, (float)maxDimension / height);

            // Only resize if the image is larger than maxDimension
            Bitmap resizedBitmap;
            if (ratio < 1) {
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                // Recycle the original to free memory
                originalBitmap.recycle();
            } else {
                resizedBitmap = originalBitmap;
            }

            // Create a temporary file for upload with higher compression
            File outputFile = new File(getCacheDir(), "profile_image.jpg");
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                // Use higher compression (lower quality) for larger images
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                resizedBitmap.recycle(); // Free memory
            }

            Log.d("Upload", "Compressed image size: " + (outputFile.length()/1024) + "KB");

            // Check if file is still too large
            if (outputFile.length() > 1024 * 1024) { // If larger than 1MB
                Toasty.warning(this, "Gambar masih terlalu besar, menurunkan kualitas", Toast.LENGTH_SHORT).show();

                // Try again with even lower quality
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    originalBitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    originalBitmap.recycle();
                }

                Log.d("Upload", "Re-compressed image size: " + (outputFile.length()/1024) + "KB");
            }

            // Create request parts
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), outputFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("foto", outputFile.getName(), requestFile);
            RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);

            // Execute upload
            ServerAPI urlAPI = new ServerAPI();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urlAPI.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RegisterAPI api = retrofit.create(RegisterAPI.class);
            Call<ResponseBody> call = api.uploadImage(body, usernameBody);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    swipeRefreshLayout.setRefreshing(false);
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseString = response.body().string();
                            Log.d("Upload", "Server response: " + responseString);

                            JSONObject json = new JSONObject(responseString);
                            int kode = json.getInt("kode");
                            String pesan = json.getString("pesan");

                            if (kode == 1) {
                                foto = json.getString("filename");

                                // Update SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("foto", foto);
                                editor.apply();

                                runOnUiThread(() -> {
                                    Toasty.success(EditProfile.this, pesan, Toast.LENGTH_SHORT, true).show();
                                    loadProfileImage();
                                });
                            } else {
                                runOnUiThread(() ->
                                        Toasty.error(EditProfile.this, pesan, Toast.LENGTH_SHORT, true).show());
                            }
                        } else {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e("Upload", "Server error: " + response.code() + " - " + errorBody);
                            runOnUiThread(() ->
                                    Toasty.error(EditProfile.this, "Upload error: " + response.code(), Toast.LENGTH_SHORT, true).show());
                        }
                    } catch (Exception e) {
                        Log.e("Upload", "Response parsing error", e);
                        runOnUiThread(() ->
                                Toasty.error(EditProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT, true).show());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e("Upload", "Upload failed", t);
                    runOnUiThread(() ->
                            Toasty.error(EditProfile.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT, true).show());
                }
            });
        } catch (Exception e) {
            swipeRefreshLayout.setRefreshing(false);
            Log.e("Upload", "Image processing failed", e);
            Toasty.error(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT, true).show();
        }
    }

    private File compressImage(Bitmap bitmap, String filename) throws IOException {
        File outputDir = getCacheDir();
        File outputFile = File.createTempFile("compressed_", ".jpg", outputDir);

        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out); // 80% quality
        }

        Log.d("Upload", "Original size: " + (bitmap.getByteCount()/10024) + "KB");
        Log.d("Upload", "Compressed size: " + (outputFile.length()/1024) + "KB");

        return outputFile;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}