package com.example.prognosisedge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prognosisedge.models.CreateUserRequest;
import com.example.prognosisedge.models.CreateUserResponse;
import com.example.prognosisedge.models.DeactivateUserRequest;
import com.example.prognosisedge.models.DeactivateUserResponse;
import com.example.prognosisedge.models.GetAllUsersResponse;
import com.example.prognosisedge.models.UpdateUserRequest;
import com.example.prognosisedge.models.UpdateUserResponse;
import com.example.prognosisedge.models.User;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONObject;


public class UserAdministratorActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    // Initialize userList to avoid null pointer exceptions
    private List<User> userList = new ArrayList<>();
    private ImageView logoutButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ua__usermanagment);

        // Initialize ApiService using your working Retrofit client
        apiService = ApiClient.getRetrofitClient().create(ApiService.class);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load users from the backend
        loadUsers();

        // Set up the adapter with the (now non-null) userList
        userAdapter = new UserAdapter(userList, this::toggleUserStatus, this::showUpdateUserDialog);
        recyclerView.setAdapter(userAdapter);

//        // Filter Button Setup
//        Button filterButton = findViewById(R.id.open_filter_button);
//        if (filterButton != null) {
//            filterButton.setOnClickListener(v -> openFilterBottomSheet());
//        }

        // "Create User" button
        findViewById(R.id.create_user_button).setOnClickListener(v -> showCreateUserDialog());

        // Logout functionality
        logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(v -> handleLogout());
    }

    // Load all users from the backend
    private void loadUsers() {
        Call<GetAllUsersResponse> call = apiService.getAllUsers();
        call.enqueue(new Callback<GetAllUsersResponse>() {
            @Override
            public void onResponse(Call<GetAllUsersResponse> call, Response<GetAllUsersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Clear current list and update with fetched users
                    userList.clear();
                    userList.addAll(response.body().getData());
                    userAdapter.notifyDataSetChanged();
                } else if (response.body() != null && response.body().getMessage() != null) {
                    Toast.makeText(UserAdministratorActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(UserAdministratorActivity.this, "Failed to load users. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetAllUsersResponse> call, Throwable t) {
                Toast.makeText(UserAdministratorActivity.this, "Error loading users: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void openFilterBottomSheet() {
//        // Pass full role names and statuses
//        List<String> roles = Arrays.asList("User Administrator", "System Supervisor", "Service Engineer");
//        FilterBottomSheet bottomSheet = new FilterBottomSheet(new java.util.HashMap<String, List<String>>() {{
//            put("Status", Arrays.asList("Active", "Inactive"));
//            put("Roles", roles);
//        }});
//        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
//    }

    // Show the "Create User" dialog
    private void showCreateUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.ua__createuser_dialog, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.user_name_edit_text);
        EditText emailEditText = dialogView.findViewById(R.id.user_email_edit_text);
        EditText passwordEditText = dialogView.findViewById(R.id.user_password_edit_text);
        Spinner roleSpinner = dialogView.findViewById(R.id.user_role_list);
        Button createUserButton = dialogView.findViewById(R.id.submit_button);

        // Populate the spinner with full role names
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getRoles());
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_backgroud));

        createUserButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            } else {
                createUser(name, email, password, role, dialog);
            }
        });

        dialog.show();
    }

    // Call the backend to create a new user
    private void createUser(String name, String email, String password, String role, AlertDialog dialog) {
        CreateUserRequest request = new CreateUserRequest(name, email, password, role);
        Call<CreateUserResponse> call = apiService.createUser(request);
        call.enqueue(new Callback<CreateUserResponse>() {
            @Override
            public void onResponse(Call<CreateUserResponse> call, Response<CreateUserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UserAdministratorActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadUsers();
                } else if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        org.json.JSONObject errorJson = new org.json.JSONObject(errorBody);
                        String errorMessage = errorJson.optString("message", "Failed to create user.");
                        Toast.makeText(UserAdministratorActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(UserAdministratorActivity.this, "Failed to create user. Please try again.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(UserAdministratorActivity.this, "Failed to create user. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateUserResponse> call, Throwable t) {
                Toast.makeText(UserAdministratorActivity.this, "Error creating user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Show the "Update User" dialog
    private void showUpdateUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.ua__userdetails_dialog, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.user_name_edit_text);
        EditText emailEditText = dialogView.findViewById(R.id.user_email_edit_text);
        Spinner roleSpinner = dialogView.findViewById(R.id.user_role_list);
        Button updateUserButton = dialogView.findViewById(R.id.update_button);

        // Populate fields with existing user data
        nameEditText.setText(user.getName());
        emailEditText.setText(user.getEmail());

        // Populate spinner with full role names
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getRoles());
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Pre-select current role using case-insensitive comparison
        int rolePosition = -1;
        String fetchedRole = user.getRole() != null ? user.getRole().trim() : "";
        List<String> roles = getRoles();
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).equalsIgnoreCase(fetchedRole)) {
                rolePosition = i;
                break;
            }
        }
        if (rolePosition >= 0) {
            roleSpinner.setSelection(rolePosition);
        }

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_backgroud));

        updateUserButton.setOnClickListener(v -> {
            String updatedName = nameEditText.getText().toString().trim();
            String updatedEmail = emailEditText.getText().toString().trim();
            String updatedRole = roleSpinner.getSelectedItem().toString();

            if (validateInputs(updatedName, updatedEmail)) {
                updateUser(user, updatedName, updatedEmail, updatedRole, dialog);
            } else {
                Toast.makeText(this, "Please enter valid details.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // Call the backend to update an existing user
    private void updateUser(User user, String name, String email, String role, AlertDialog dialog) {
        UpdateUserRequest request = new UpdateUserRequest(user.getUserId(), name, email, role);
        Call<UpdateUserResponse> call = apiService.updateUser(request);
        call.enqueue(new Callback<UpdateUserResponse>() {
            @Override
            public void onResponse(Call<UpdateUserResponse> call, Response<UpdateUserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UserAdministratorActivity.this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadUsers();
                } else if (response.body() != null && response.body().getMessage() != null) {
                    Toast.makeText(UserAdministratorActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(UserAdministratorActivity.this, "Failed to update user. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateUserResponse> call, Throwable t) {
                Toast.makeText(UserAdministratorActivity.this, "Error updating user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Toggle user status via backend (only deactivation supported)
    private void toggleUserStatus(int position, boolean isActive) {
        User user = userList.get(position);
        if (!isActive) {
            Call<DeactivateUserResponse> call = apiService.deactivateUser(new DeactivateUserRequest(user.getUserId()));
            call.enqueue(new Callback<DeactivateUserResponse>() {
                @Override
                public void onResponse(Call<DeactivateUserResponse> call, Response<DeactivateUserResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(UserAdministratorActivity.this, "User " + user.getName() + " deactivated.", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        Toast.makeText(UserAdministratorActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                        userAdapter.notifyItemChanged(position);
                    } else {
                        Toast.makeText(UserAdministratorActivity.this, "Failed to deactivate user. Please try again.", Toast.LENGTH_SHORT).show();
                        userAdapter.notifyItemChanged(position);
                    }
                }

                @Override
                public void onFailure(Call<DeactivateUserResponse> call, Throwable t) {
                    Toast.makeText(UserAdministratorActivity.this, "Error deactivating user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    userAdapter.notifyItemChanged(position);
                }
            });
        } else {
            Toast.makeText(this, "Activating a user is not supported.", Toast.LENGTH_SHORT).show();
            userAdapter.notifyItemChanged(position);
        }
    }

    // Simple input validation
    private boolean validateInputs(String name, String email) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Return the full list of roles
    private List<String> getRoles() {
        return Arrays.asList("User Administrator", "System Supervisor", "Service Engineer");
    }

    // Clear preferences and navigate to LoginActivity on logout
    private void handleLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("PrognosisEdgePrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.prognosisedge.models.User;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class UserAdministratorActivity extends AppCompatActivity {
//
//    private RecyclerView recyclerView;
//    private UserAdapter userAdapter;
//    private List<User> userList;
//    private ImageView logoutButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.ua__usermanagment);
//
//        // Initialize RecyclerView
//        recyclerView = findViewById(R.id.recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Initialize user list and adapter
//        userList = fetchUsersFromDatabase();
//        userAdapter = new UserAdapter(userList, this::toggleUserStatus, this::showUpdateUserDialog);
//        recyclerView.setAdapter(userAdapter);
//
//        // Filter Button Setup
//        Button filterButton = findViewById(R.id.open_filter_button);
//        if (filterButton != null) {
//            filterButton.setOnClickListener(v -> openFilterBottomSheet());
//        }
//
//        // Handle the "Create User" button click
//        findViewById(R.id.create_user_button).setOnClickListener(v -> showCreateUserDialog());
//
//        // Logout functionality
//        logoutButton = findViewById(R.id.logout);
//        logoutButton.setOnClickListener(v -> handleLogout());
//    }
//
//    private void openFilterBottomSheet() {
//        Map<String, List<String>> filters = new HashMap<>();
//        filters.put("Status", Arrays.asList("Active", "Inactive"));
//        filters.put("Roles", Arrays.asList("Admin", "Supervisor", "Engineer"));
//
//        // Open FilterBottomSheet
//        FilterBottomSheet bottomSheet = new FilterBottomSheet(filters);
//        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag()); // Fixed to use `getSupportFragmentManager`
//    }
//
//    // Show the "Create User" dialog
//    private void showCreateUserDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.ua__createuser_dialog, null);
//        builder.setView(dialogView);
//
//        EditText nameEditText = dialogView.findViewById(R.id.user_name_edit_text);
//        EditText emailEditText = dialogView.findViewById(R.id.user_email_edit_text);
//        EditText passwordEditText = dialogView.findViewById(R.id.user_password_edit_text);
//        Spinner roleSpinner = dialogView.findViewById(R.id.user_role_list);
//        Button createUserButton = dialogView.findViewById(R.id.submit_button);
//
//        // Populate the spinner with roles
//        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getRoles());
//        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        roleSpinner.setAdapter(roleAdapter);
//
//        AlertDialog dialog = builder.create();
//        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_backgroud));
//
//
//        // Handle "Create User" button click
//        createUserButton.setOnClickListener(v -> {
//            String name = nameEditText.getText().toString().trim();
//            String email = emailEditText.getText().toString().trim();
//            String password = passwordEditText.getText().toString().trim();
//            String role = roleSpinner.getSelectedItem().toString();
//
//            if (name.isEmpty() || email.isEmpty() || role.isEmpty()) {
//                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
//            } else {
//                createUser(name, email, password, role);
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }
//
//    // Show the "Update User" dialog
//    private void showUpdateUserDialog(User user) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.ua__userdetails_dialog, null);
//        builder.setView(dialogView);
//
//        EditText nameEditText = dialogView.findViewById(R.id.user_name_edit_text);
//        EditText emailEditText = dialogView.findViewById(R.id.user_email_edit_text);
//        Spinner roleSpinner = dialogView.findViewById(R.id.user_role_list);
//        Button updateUserButton = dialogView.findViewById(R.id.update_button);
//
//        // Populate fields with existing data
//        nameEditText.setText(user.getName());
//        emailEditText.setText(user.getEmail());
//
//        // Populate the spinner with roles
//        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getRoles());
//        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        roleSpinner.setAdapter(roleAdapter);
//
//        // Pre-select the current role
//        int rolePosition = getRoles().indexOf(user.getRole());
//        if (rolePosition >= 0) {
//            roleSpinner.setSelection(rolePosition);
//        }
//
//        AlertDialog dialog = builder.create();
//        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_backgroud));
//
//
//        // Handle "Update User" button click
//        updateUserButton.setOnClickListener(v -> {
//            String updatedName = nameEditText.getText().toString().trim();
//            String updatedEmail = emailEditText.getText().toString().trim();
//            String updatedRole = roleSpinner.getSelectedItem().toString();
//
//            if (validateInputs(updatedName, updatedEmail)) {
//                updateUser(user, updatedName, updatedEmail, updatedRole);
//                dialog.dismiss();
//            } else {
//                Toast.makeText(this, "Please enter valid details.", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        dialog.show();
//    }
//
//    // Update an existing user
//    private void updateUser(User user, String name, String email, String role) {
//        user.setName(name);
//        user.setEmail(email);
//        user.setRole(role);
//
//        // Update the database (mock logic below)
//        Toast.makeText(this, "User details updated successfully!", Toast.LENGTH_SHORT).show();
//
//        // Refresh the RecyclerView
//        userAdapter.notifyDataSetChanged();
//    }
//
//    // Validate user inputs
//    private boolean validateInputs(String name, String email) {
//        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
//            return false;
//        }
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        return true;
//    }
//
//    // Mock method to fetch user roles
//    private List<String> getRoles() {
//        List<String> roles = new ArrayList<>();
//        roles.add("Admin");
//        roles.add("Supervisor");
//        roles.add("Engineer");
//        return roles;
//    }
//
//    // Create a new user
//    private void createUser(String name, String email, String password, String role) {
//        User newUser = new User(name, email, password, role, true); // Default to active
//        userList.add(newUser);
//
//        // Update the database (mock logic below)
//        Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show();
//
//        // Refresh the RecyclerView
//        userAdapter.notifyDataSetChanged();
//    }
//
//    // Mock method to fetch user data
//    private List<User> fetchUsersFromDatabase() {
//        List<User> list = new ArrayList<>();
//        list.add(new User("Shahzeb Ahmed", "sa@example.com", "password123", "Admin", true));
//        list.add(new User("Ghazal e Ashar", "ga@example.com", "password123", "Admin", true));
//        list.add(new User("Adeel Ansari", "aa@example.com", "password123", "Supervisor", false));
//
//        return list;
//    }
//
//    // Toggle user status
//    private void toggleUserStatus(int position, boolean isActive) {
//        User user = userList.get(position);
//        user.setActive(isActive);
//
//        // Update the database (mock logic below)
//        Toast.makeText(this, "User " + user.getName() + " is now " + (isActive ? "Active" : "Inactive"), Toast.LENGTH_SHORT).show();
//
//        // Update RecyclerView
//        userAdapter.notifyItemChanged(position);
//    }
//
//    // Handle logout
//    private void handleLogout() {
//        // Clear all saved preferences
//        SharedPreferences sharedPreferences = getSharedPreferences("PrognosisEdgePrefs", Context.MODE_PRIVATE);
//        sharedPreferences.edit().clear().apply();
//
//        // Navigate to LoginActivity
//        Intent intent = new Intent(this, LoginActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//
//        // Finish the current activity to prevent going back
//        finish();
//    }
//}

