package com.example.uts_a22202303006.checkout;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uts_a22202303006.R;
import com.example.uts_a22202303006.api.RegisterAPI;
import com.example.uts_a22202303006.api.ServerAPI;
import com.example.uts_a22202303006.databinding.ActivityPaymentDetailsBinding;
import com.example.uts_a22202303006.orders.OrderHistoryActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentDetailsActivity extends AppCompatActivity {

    private ActivityPaymentDetailsBinding binding;
    private int orderId;
    private String orderNumber;
    private Uri paymentProofUri;
    private boolean isImageSelected = false;

    // Activity result launcher for selecting an image
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            paymentProofUri = selectedImageUri;
                            binding.imgPaymentProof.setImageURI(selectedImageUri);
                            binding.btnUploadProof.setEnabled(true);
                            isImageSelected = true;
                        } catch (Exception e) {
                            Log.e("PaymentDetails", "Error loading image", e);
                            Toasty.error(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        // Removed back button from action bar
        getSupportActionBar().setTitle("Payment Details");

        // Get data from intent
        orderId = getIntent().getIntExtra("ORDER_ID", -1);
        orderNumber = getIntent().getStringExtra("ORDER_NUMBER");
        double grandTotal = getIntent().getDoubleExtra("GRAND_TOTAL", 0);
        String bankName = getIntent().getStringExtra("BANK_NAME");
        String accountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");
        String accountName = getIntent().getStringExtra("ACCOUNT_NAME");
        String instructions = getIntent().getStringExtra("INSTRUCTIONS");

        // Populate views
        binding.tvBankName.setText(bankName);
        binding.tvAccountNumber.setText(accountNumber);
        binding.tvAccountName.setText(accountName);
        binding.tvTransferAmount.setText(formatRupiah(grandTotal));
        binding.tvOrderNumber.setText("Order #" + orderNumber);
        binding.tvInstructions.setText(instructions);

        // Setup button listeners
        binding.btnSelectImage.setOnClickListener(v -> openImagePicker());
        binding.btnUploadProof.setOnClickListener(v -> uploadPaymentProof());
        binding.btnViewOrders.setOnClickListener(v -> navigateToOrderHistory());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadPaymentProof() {
        if (orderId == -1) {
            Toasty.error(this, "Invalid order data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isImageSelected || paymentProofUri == null) {
            Toasty.warning(this, "Please select a payment proof image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and disable buttons
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnUploadProof.setEnabled(false);
        binding.btnSelectImage.setEnabled(false);

        try {
            // Create a temporary file from the selected image
            File imageFile = createTempFileFromUri(paymentProofUri);
            if (imageFile == null) {
                throw new Exception("Failed to create image file");
            }

            // Create multipart request
            RequestBody orderIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(orderId));
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("bukti_bayar", imageFile.getName(), requestFile);

            // Make API call
            RegisterAPI apiService = ServerAPI.getClient().create(RegisterAPI.class);
            apiService.uploadPaymentProof(orderIdBody, filePart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    binding.progressBar.setVisibility(View.GONE);

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            if (jsonResponse.getBoolean("status")) {
                                showSuccessDialog();
                            } else {
                                Toasty.error(PaymentDetailsActivity.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                                binding.btnUploadProof.setEnabled(true);
                                binding.btnSelectImage.setEnabled(true);
                            }
                        } else {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Toasty.error(PaymentDetailsActivity.this, "Upload failed: " + errorBody, Toast.LENGTH_LONG).show();
                            binding.btnUploadProof.setEnabled(true);
                            binding.btnSelectImage.setEnabled(true);
                        }
                    } catch (Exception e) {
                        Log.e("PaymentDetails", "Error processing response", e);
                        Toasty.error(PaymentDetailsActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                        binding.btnUploadProof.setEnabled(true);
                        binding.btnSelectImage.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnUploadProof.setEnabled(true);
                    binding.btnSelectImage.setEnabled(true);
                    Toasty.error(PaymentDetailsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PaymentDetails", "API call failed", t);
                }
            });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnUploadProof.setEnabled(true);
            binding.btnSelectImage.setEnabled(true);
            Toasty.error(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PaymentDetails", "Error preparing upload", e);
        }
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // Read the image as bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Create temp file
            File outputDir = getCacheDir();
            File outputFile = File.createTempFile("proof_" + UUID.randomUUID().toString(), ".jpg", outputDir);

            // Write the bitmap to the file
            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();

            return outputFile;
        } catch (Exception e) {
            Log.e("PaymentDetails", "Error creating temp file", e);
            return null;
        }
    }

    private void showSuccessDialog() {
        // Create dialog with custom view
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_success);
        dialog.setCancelable(false);

        // Set window properties
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Set button click listeners
        Button btnViewOrders = dialog.findViewById(R.id.btnViewOrders);
        btnViewOrders.setOnClickListener(v -> {
            navigateToOrderHistory();
            dialog.dismiss();
        });

        TextView tvOrderNumber = dialog.findViewById(R.id.tvOrderNumber);
        tvOrderNumber.setText(orderNumber);

        // Show dialog
        dialog.show();
    }

    private void navigateToOrderHistory() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        startActivity(intent);
        finish();
    }

    private String formatRupiah(double amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatRupiah.format(amount).replace(",00", "");
    }

    @Override
    public void onBackPressed() {
        // Don't call super.onBackPressed() here as it would use the default back behavior
        // Instead, always navigate to order history when back is pressed
        navigateToOrderHistory();
    }
}