package com.example.prognosisedge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prognosisedge.models.ResetPasswordRequest;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpasswordactivity); // Ensure the layout file matches this name

        // Initialize views
        newPasswordEditText = findViewById(R.id.new_password);
        confirmNewPasswordEditText = findViewById(R.id.confirmnewpassword);
        resetPasswordButton = findViewById(R.id.btn_reset_password);

        // Get username from Intent
        String username = getIntent().getStringExtra("username");

        // Set click listener for the reset button
        resetPasswordButton.setOnClickListener(v -> resetPassword(username));
    }

    private void resetPassword(String username) {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        // Validate fields
        if (!validateField(newPasswordEditText, "New password is required")) return;
        if (!validateField(confirmNewPasswordEditText, "Please confirm your password")) return;

        // Check if passwords match
        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("Passwords do not match");
            confirmNewPasswordEditText.requestFocus();
            return;
        }

        // Make API call to reset the password
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        ResetPasswordRequest request = new ResetPasswordRequest(username, newPassword);

        apiService.resetPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        org.json.JSONObject errorJson = new org.json.JSONObject(errorBody);
                        String errorMessage = errorJson.optString("message", "");

                        // Show specific backend error only if it's about password length
                        if (!errorMessage.isEmpty()) {
                            Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Failed to reset password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                    } catch (Exception e) {
                        Toast.makeText(ResetPasswordActivity.this, "Failed to reset password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, "System temporarily unavailable. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validate a field and show an error if invalid
    private static boolean validateField(EditText field, String errorMessage) {
        String input = field.getText().toString().trim();
        if (input.isEmpty()) {
            field.setError(errorMessage);
            field.requestFocus();
            return false;
        }
        return true;
    }

    // Navigate to login screen
    private void navigateToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close the activity
    }
}
