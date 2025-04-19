package com.example.uts_a22202303006.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    // Deklarasi komponen UI
    TextView tv_kembali;
    EditText ti_nama, ti_email, ti_username, ti_password, ti_konfirmasi_password;
    Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Aktifkan EdgeToEdge untuk tampilan full-screen
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Mengatur padding berdasarkan sistem insets (status bar, navigasi)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi view dari layout
        tv_kembali = findViewById(R.id.tv_kembali);
        ti_nama = findViewById(R.id.ti_nama);
        ti_email = findViewById(R.id.ti_email);
        ti_username = findViewById(R.id.ti_username);
        ti_password = findViewById(R.id.ti_password);
        ti_konfirmasi_password = findViewById(R.id.ti_konfirmasi_password);
        btn_register = findViewById(R.id.btn_register);

        // Tombol kembali: Arahkan ke LoginActivity
        tv_kembali.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Tombol register: Validasi input lalu submit ke server
        btn_register.setOnClickListener(view -> {
            String email = ti_email.getText().toString();
            String nama = ti_nama.getText().toString();
            String username = ti_username.getText().toString();
            String password = ti_password.getText().toString();
            String konfirmasiPassword = ti_konfirmasi_password.getText().toString();

            // Validasi: Pastikan semua field terisi
            if (nama.isEmpty() || email.isEmpty() || username.isEmpty()
                    || password.isEmpty() || konfirmasiPassword.isEmpty()) {
                showMessage("Semua field harus diisi");
                return;
            }

            // Validasi: Cek format email
            if (!isEmailValid(email)) {
                showMessage("Email Tidak Valid");
                return;
            }

            // Validasi: Cek kesesuaian password dan konfirmasi
            if (!password.equals(konfirmasiPassword)) {
                showMessage("Password dan Konfirmasi Password tidak sama");
                return;
            }

            // Proses pendaftaran
            prosesSubmit(email, nama, username, password);
        });
    }

    /**
     * Cek apakah email valid menggunakan regex.
     * @param email String email yang akan dicek
     * @return true jika email valid, false jika tidak
     */
    public boolean isEmailValid(String email) {
        // Contoh regex sederhana untuk validasi email
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,}$";
        return Pattern.compile(expression, Pattern.CASE_INSENSITIVE).matcher(email).matches();
    }

    /**
     * Menampilkan dialog pesan error
     * @param message Pesan yang akan ditampilkan
     */
    private void showMessage(String message) {
        // Tampilkan AlertDialog dengan tombol "Retry"
        AlertDialog.Builder msg = new AlertDialog.Builder(RegisterActivity.this);
        msg.setMessage(message)
                .setNegativeButton("Retry", null)
                .create()
                .show();
    }

    /**
     * Proses submit data registrasi ke server menggunakan Retrofit
     */
    void prosesSubmit(String email, String nama, String username, String password) {
        // URL dasar API
        ServerAPI urlapi = new ServerAPI();
        String URL = urlapi.BASE_URL;

        // Buat instance Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Buat instance API yang telah didefinisikan
        RegisterAPI api = retrofit.create(RegisterAPI.class);

        // Proses registrasi via API
        api.register(email, nama, username, password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Response diterima, dismiss loading (jika ada)
                try {
                    // Pastikan body tidak null untuk mencegah crash
                    if (response.body() == null) {
                        showMessage("Response kosong dari server.");
                        return;
                    }
                    // Parsing response string
                    String responseString = response.body().string();
                    Log.d("RegisterResponse", responseString);
                    JSONObject json = new JSONObject(responseString);

                    // Cek status dan result dari response
                    if (json.getString("status").equals("1")) {
                        if (json.getString("result").equals("1")) {
                            // Pendaftaran berhasil, tampilkan dialog sukses
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setMessage("Register Berhasil, silakan login")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .show();
                            // Reset field input
                            ti_nama.setText("");
                            ti_email.setText("");
                            ti_username.setText("");
                            ti_password.setText("");
                            ti_konfirmasi_password.setText("");
                        } else {
                            // Proses simpan data gagal
                            showMessage("Simpan Gagal");
                        }
                    } else {
                        // Jika status tidak 1, artinya user sudah ada atau error lain
                        showMessage("User Sudah Ada");
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    showMessage("Terjadi kesalahan saat memproses registrasi.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("RegisterError", "Register Gagal: " + t.toString());
                showMessage("Gagal terhubung ke server.");
            }
        });
    }
}
