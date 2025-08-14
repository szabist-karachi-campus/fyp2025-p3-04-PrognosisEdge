package com.example.prognosisedge;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prognosisedge.models.OtpRequest;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetpasswordActivity extends AppCompatActivity {

    private EditText resetUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);

        // Initialize views
        resetUsername = findViewById(R.id.reset_username);
        Button resetButton = findViewById(R.id.btn_reset);

        // Set customized hint size
        setHintSize(resetUsername, "Enter your username", 16);

        // Reset button click logic
        resetButton.setOnClickListener(v -> {
            String username = resetUsername.getText().toString().trim();
            if (username.isEmpty()) {
                showToast("Please enter your username");
            } else {
                // Call the API to send OTP for the entered username
                sendOTP(username);
            }
        });
    }

    // Method to send OTP
    private void sendOTP(String username) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        OtpRequest otpRequest = new OtpRequest(username);

        apiService.sendOTP(otpRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("OTP sent successfully to your registered email.");
                    showOTPDialog(username); // Show OTP dialog on success
                } else {
                    showToast("Failed to send OTP. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("System temporarily unavailable. Please try again.");
            }
        });
    }

    // Method to show the OTP dialog
    private void showOTPDialog(String username) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.otp_dialogbox, null);

        final EditText otpDigit1 = dialogView.findViewById(R.id.otp_digit_1);
        final EditText otpDigit2 = dialogView.findViewById(R.id.otp_digit_2);
        final EditText otpDigit3 = dialogView.findViewById(R.id.otp_digit_3);
        final EditText otpDigit4 = dialogView.findViewById(R.id.otp_digit_4);
        final EditText otpDigit5 = dialogView.findViewById(R.id.otp_digit_5);
        final EditText otpDigit6 = dialogView.findViewById(R.id.otp_digit_6);
        Button resendOtpButton = dialogView.findViewById(R.id.resendOtpButton);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        // Set TextWatchers for OTP fields to handle focus and color changes
        otpDigit1.addTextChangedListener(new GenericTextWatcher(otpDigit1, otpDigit2));
        otpDigit2.addTextChangedListener(new GenericTextWatcher(otpDigit2, otpDigit3));
        otpDigit3.addTextChangedListener(new GenericTextWatcher(otpDigit3, otpDigit4));
        otpDigit4.addTextChangedListener(new GenericTextWatcher(otpDigit4, otpDigit5));
        otpDigit5.addTextChangedListener(new GenericTextWatcher(otpDigit5, otpDigit6));
        otpDigit6.addTextChangedListener(new GenericTextWatcher(otpDigit6, null));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_backgroud));

        // Submit OTP
        submitButton.setOnClickListener(v -> {
            String enteredOTP = otpDigit1.getText().toString().trim() +
                    otpDigit2.getText().toString().trim() +
                    otpDigit3.getText().toString().trim() +
                    otpDigit4.getText().toString().trim() +
                    otpDigit5.getText().toString().trim() +
                    otpDigit6.getText().toString().trim();

            if (enteredOTP.length() == 6) {
                verifyOTP(username, enteredOTP, dialog);
            } else {
                showToast("Please enter a valid 6-digit OTP.");
            }
        });

        // Resend OTP
        resendOtpButton.setOnClickListener(v -> sendOTP(username));

        dialog.show();
    }

    // Method to verify OTP
    private void verifyOTP(String username, String enteredOTP, AlertDialog dialog) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        OtpRequest otpRequest = new OtpRequest(username, enteredOTP);

        apiService.verifyOTP(otpRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    navigateToResetPassword(username); // Navigate to ResetPasswordActivity
                } else {
                    showToast("Invalid OTP. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("System temporarily unavailable. Please try again later.");
            }
        });
    }

    // Navigate to Reset Password screen
    private void navigateToResetPassword(String username) {
        Log.d("Navigation", "Navigating to ResetPasswordActivity with username: " + username);
        Intent intent = new Intent(ForgetpasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra("username", username); // Pass username to the ResetPasswordActivity
        startActivity(intent);
        finish(); // Close ForgetpasswordActivity
    }

    // Utility method to dynamically change hint size
    private void setHintSize(EditText editText, String hintText, int sizeInSp) {
        SpannableString hint = new SpannableString(hintText);
        hint.setSpan(new AbsoluteSizeSpan(sizeInSp, true), 0, hint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(hint);
    }

    // Utility method to show toast messages
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // TextWatcher for OTP fields to handle focus and color changes
    private class GenericTextWatcher implements TextWatcher {
        private final EditText currentView;
        private final EditText nextView;

        public GenericTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus(); // Move to next field automatically
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                currentView.setBackgroundResource(R.drawable.otp_box); // Empty state
            } else {
                currentView.setBackgroundResource(R.drawable.otp_filledbox); // Filled state
            }
        }
    }
}
