package com.example.uts_a22202303006.auth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uts_a22202303006.MainActivity;
import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    public static final String URL = new ServerAPI().BASE_URL;

    ProgressDialog pd;
    Button btnLogin;
    EditText etUsername, etPassword;
    TextView tv_register;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cek apakah user sudah login
        sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            // Jika sudah login, langsung buka MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Tampilkan layout login
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi komponen UI
        tv_register = findViewById(R.id.tv_register);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Klik "Belum punya akun?"
        tv_register.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        // Klik tombol login
        btnLogin.setOnClickListener(view -> {
            pd = new ProgressDialog(LoginActivity.this);
            pd.setTitle("Login...");
            pd.setMessage("Tunggu Sebentar...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            prosesLogin(etUsername.getText().toString(), etPassword.getText().toString());
        });
    }

    /**
     * Proses login ke server
     */
    void prosesLogin(String identifier, String password) {
        pd.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);

        api.login(identifier, password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                pd.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d("LoginResponse", responseString);

                        JSONObject json = new JSONObject(responseString);

                        // Check login result
                        if (json.getString("result").equals("1")) {
                            // Extract data from response
                            String nama = json.getJSONObject("data").getString("nama");
                            String username = json.getJSONObject("data").getString("username");
                            String email = json.getJSONObject("data").getString("email");
                            String foto = json.getJSONObject("data").getString("foto");

                            // Save login session
                            SharedPreferences sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE);
                            editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("username", username);
                            editor.putString("nama", nama);
                            editor.putString("email", email);
                            editor.putString("foto", foto);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            showErrorDialog("Invalid identifier or password.");
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        showErrorDialog("Error processing login.");
                    }
                } else {
                    showErrorDialog("Server did not respond correctly.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                showErrorDialog("Failed to connect to the server.");
                Log.e("Login Error", t.toString());
            }
        });
    }

    /**
     * Tampilkan dialog error
     */
    void showErrorDialog(String message) {
        AlertDialog.Builder msg = new AlertDialog.Builder(LoginActivity.this);
        msg.setMessage(message)
                .setNegativeButton("Retry", null)
                .create()
                .show();
    }
}
