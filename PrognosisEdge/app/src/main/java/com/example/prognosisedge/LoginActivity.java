package com.example.prognosisedge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prognosisedge.models.LoginRequest;
import com.example.prognosisedge.models.OtpRequest;
import com.example.prognosisedge.models.LoginResponse;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button loginButton;
    private TextView forgotPassword;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PrognosisEdgePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ROLE = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Check if the user is already logged in
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            navigateToDashboard(sharedPreferences.getString(KEY_USER_ROLE, ""));
            finish(); // Finish this activity to prevent going back to login
            return;
        }

        // Initialize views
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        forgotPassword = findViewById(R.id.forgot_password);

        // Set hint sizes
        setHintSize(username, "Username", 16);
        setHintSize(password, "Password", 16);

        // Handle login logic
        loginButton.setOnClickListener(v -> handleLogin());

        // Handle forgot password click
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetpasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.forgetpass_slidedown, R.anim.login_slide_down);
        });
    }


    private void setHintSize(EditText editText, String hintText, int sizeInSp) {
        SpannableString hint = new SpannableString(hintText);
        hint.setSpan(new AbsoluteSizeSpan(sizeInSp, true), 0, hint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(hint);
    }

    private void handleLogin() {
        String enteredUsername = username.getText().toString().trim();
        String enteredPassword = password.getText().toString().trim();

        if (enteredUsername.isEmpty()) {
            username.setError("Username is required");
            return;
        }

        if (enteredPassword.isEmpty()) {
            password.setError("Password is required");
            return;
        }

        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(enteredUsername, enteredPassword);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        // Save login state, user role, and username
                        sharedPreferences.edit()
                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                .putString(KEY_USER_ROLE, loginResponse.getRole())
                                .putString("userName", enteredUsername) // Save username
                                .apply();

                        // Show a toast message immediately after validation
                        Toast.makeText(LoginActivity.this, "An OTP is being sent to your email.", Toast.LENGTH_LONG).show();

                        // Trigger OTP sending
                        sendOTPToUser(enteredUsername);
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials.", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 401) {
                    // Handle invalid credentials
                    Toast.makeText(LoginActivity.this, "Invalid Credentials.", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle other server errors
                    Toast.makeText(LoginActivity.this, "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendOTPToUser(String username) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        OtpRequest otpRequest = new OtpRequest(username);

        apiService.sendOTP(otpRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showOTPDialog(username); // Show the OTP dialog
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to send OTP. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

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

        submitButton.setOnClickListener(v -> {
            String enteredOTP = otpDigit1.getText().toString().trim() +
                    otpDigit2.getText().toString().trim() +
                    otpDigit3.getText().toString().trim() +
                    otpDigit4.getText().toString().trim() +
                    otpDigit5.getText().toString().trim() +
                    otpDigit6.getText().toString().trim();

            verifyOTP(username, enteredOTP, dialog);
        });

        resendOtpButton.setOnClickListener(v -> sendOTPToUser(username));

        dialog.show();
    }

    private void verifyOTP(String username, String enteredOTP, AlertDialog dialog) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        OtpRequest otpRequest = new OtpRequest(username, enteredOTP);

        apiService.verifyOTP(otpRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    navigateToDashboard(sharedPreferences.getString(KEY_USER_ROLE, ""));
                } else {
                    Toast.makeText(LoginActivity.this, "The OTP you have entered is incorrect. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "System temporarily unavailable. Please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;

        if ("service engineer".equalsIgnoreCase(role)) {
            intent = new Intent(this, ServiceEngineerActivity.class);
        } else if ("system supervisor".equalsIgnoreCase(role)) {
            intent = new Intent(this, SystemSupervisorActivity.class);
        } else if ("user administrator".equalsIgnoreCase(role)) {
            intent = new Intent(this, UserAdministratorActivity.class);
        } else {
            Toast.makeText(this, "Invalid role. Contact admin.", Toast.LENGTH_SHORT).show();
            return; // Prevent navigation if the role is invalid
        }

        startActivity(intent);
        finish();
    }

    // Generic TextWatcher for OTP boxes
    private class GenericTextWatcher implements TextWatcher {
        private EditText currentView;
        private EditText nextView;

        public GenericTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                currentView.setBackgroundResource(R.drawable.otp_box);
            } else {
                currentView.setBackgroundResource(R.drawable.otp_filledbox);
            }
        }
    }
}
