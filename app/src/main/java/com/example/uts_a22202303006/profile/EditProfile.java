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

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.uts_a22202303006.MainActivity;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
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
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private String currentPhotoPath;

    ImageView ivBack, imgProfile;
    TextView btnChangePhoto;
    EditText etProfile_Nama, etProfile_Email, etProfile_Alamat, etProfile_Kota, etProfile_Provinsi, etProfile_Telp, etProfile_Kodepos;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nama = getIntent().getStringExtra("nama");
        username = getIntent().getStringExtra("username");
        foto = getIntent().getStringExtra("foto");

        // Inisialisasi UI
        ivBack = findViewById(R.id.ivBack);
//        imgProfile = findViewById(R.id.imgProfile);
//        btnChangePhoto = findViewById(R.id.btnChangeImage);
        etProfile_Nama = findViewById(R.id.etProfile_Nama);
        etProfile_Email = findViewById(R.id.etProfile_Email);
        etProfile_Alamat = findViewById(R.id.etProfile_Alamat);
        etProfile_Kota = findViewById(R.id.etProfile_Kota);
        etProfile_Provinsi = findViewById(R.id.etProfile_Province);
        etProfile_Telp = findViewById(R.id.etProfile_Telp);
        etProfile_Kodepos = findViewById(R.id.etProfile_Kodepos);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Set data ke EditText
//        Glide.with(this)
//                .load(URL + "images/" + foto)
//                .centerCrop()
//                .error(R.drawable.profile) // Fallback to the default drawable if loading fails
//                .placeholder(R.drawable.profile) // Show the default drawable while loading
//                .into(imgProfile);

        getProfil(username);

//        btnChangePhoto.setOnClickListener(v -> showImagePickerDialog());

        btnSubmit.setOnClickListener(v -> updateProfil());

        ivBack.setOnClickListener(v -> navigateToHome());

    }

    private void navigateToHome() {
        Intent intent = new Intent(EditProfile.this, ProfileFragment.class);
        startActivity(intent);
        finish();
    }

    private void getProfil(String vusername) {
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
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
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
                        data.getTelp(), data.getKodepos(), data.getEmail(), data.getUsername())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.body() != null) {
                                JSONObject json = new JSONObject(response.body().string());
                                Toast.makeText(EditProfile.this, json.getString("message"), Toast.LENGTH_SHORT).show();

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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.uts_a22202302996.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
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
            File file = new File(getRealPathFromURI(imageUri));
            if (file == null || !file.exists()) {
                Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Upload", "Attempting to upload: " + file.getAbsolutePath());

            // Compress image before upload
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            File compressedFile = compressImage(bitmap, file.getName());

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), compressedFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", compressedFile.getName(), requestFile);
            RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);

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
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseString = response.body().string();
                            Log.d("Upload", "Server response: " + responseString);

                            JSONObject json = new JSONObject(responseString);
                            String result = json.getString("result");

                            runOnUiThread(() -> {
                                try {
                                    Toast.makeText(EditProfile.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                                if ("1".equals(result)) {
                                    String newPhotoUrl = null;
                                    try {
                                        newPhotoUrl = json.getString("url");
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    Glide.with(EditProfile.this)
                                            .load(newPhotoUrl)
                                            .into(imgProfile);
                                }
                            });
                        } else {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e("Upload", "Server error: " + response.code() + " - " + errorBody);
                            runOnUiThread(() ->
                                    Toast.makeText(EditProfile.this, "Upload error: " + response.code(), Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        Log.e("Upload", "Response parsing error", e);
                        runOnUiThread(() ->
                                Toast.makeText(EditProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Upload", "Upload failed", t);
                    runOnUiThread(() ->
                            Toast.makeText(EditProfile.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Log.e("Upload", "Upload preparation failed", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File compressImage(Bitmap bitmap, String filename) throws IOException {
        File outputDir = getCacheDir();
        File outputFile = File.createTempFile("compressed_", ".jpg", outputDir);

        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out); // 80% quality
        }

        Log.d("Upload", "Original size: " + (bitmap.getByteCount()/1024) + "KB");
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