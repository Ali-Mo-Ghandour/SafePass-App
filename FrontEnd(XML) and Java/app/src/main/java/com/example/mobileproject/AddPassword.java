package com.example.mobileproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mobileproject.databinding.ActivityAddPasswordBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddPassword extends AppCompatActivity {

    private ActivityAddPasswordBinding binding;
    private static final String TAG = "AddPassword";
    private static final String THEME_PREFS = "ThemePrefs";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String USER_PREFS = "UserPrefs";

    private boolean isEditMode = false;
    private int editingId = -1; // ID of the password being edited

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme(); // Apply theme before super
        super.onCreate(savedInstanceState);

        binding = ActivityAddPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupSaveButton();
        checkIfEditing();

        // Connect generate button with password generator
        binding.btnGeneratePassword.setOnClickListener(v -> {
            String generatedPassword = generateStrongPassword();
            binding.passwordInput.setText(generatedPassword);
            Toast.makeText(this, "Strong password generated!", Toast.LENGTH_SHORT).show();
        });
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(DARK_MODE_KEY, false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Password");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                savePasswordToServer();
            }
        });
    }

    private void checkIfEditing() {
        Intent intent = getIntent();
        if ("edit".equals(intent.getStringExtra("mode"))) {
            isEditMode = true;
            editingId = intent.getIntExtra("id", -1);

            binding.accountInput.setText(intent.getStringExtra("account"));
            binding.usernameInput.setText(intent.getStringExtra("username"));
            binding.passwordInput.setText(intent.getStringExtra("password"));
            binding.notesInput.setText(intent.getStringExtra("notes"));

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Password");
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String passwordText = binding.passwordInput.getText().toString().trim();

        if (binding.accountInput.getText().toString().trim().isEmpty()) {
            binding.accountInput.setError("Account name required");
            isValid = false;
        }

        if (binding.usernameInput.getText().toString().trim().isEmpty()) {
            binding.usernameInput.setError("Username required");
            isValid = false;
        }

        if (passwordText.isEmpty()) {
            binding.passwordInput.setError("Password required");
            isValid = false;
        } else if (!isPasswordStrong(passwordText)) {
            binding.passwordInput.setError("Password must be 8+ chars, include 1 uppercase, 1 number, 1 special.");
            isValid = false;
        }

        return isValid;
    }

    private boolean isPasswordStrong(String password) {
        // Must be at least 8 characters, include 1 uppercase, 1 digit, and 1 special character (any non-letter/digit)
        String pattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";
        return password.matches(pattern);
    }

    private String generateStrongPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "@#$%^&*()-_=+[]{}<>?!";
        String all = upper + lower + numbers + special;

        StringBuilder password = new StringBuilder();
        password.append(upper.charAt((int)(Math.random() * upper.length())));
        password.append(lower.charAt((int)(Math.random() * lower.length())));
        password.append(numbers.charAt((int)(Math.random() * numbers.length())));
        password.append(special.charAt((int)(Math.random() * special.length())));

        
        for (int i = 4; i < 12; i++) {
            password.append(all.charAt((int)(Math.random() * all.length())));
        }

        return password.toString();
    }

    private void savePasswordToServer() {
        binding.saveButton.setEnabled(false);

        SharedPreferences prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String userKey = prefs.getString("userKey", null);

        if (userId == null || userKey == null) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String url = isEditMode ?
                "http://10.21.136.222/myproject/updatepasso.php" :
                "http://10.21.136.222/myproject/addpasso.php";

        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("user_key", userKey);
        params.put("account", binding.accountInput.getText().toString().trim());
        params.put("username", binding.usernameInput.getText().toString().trim());
        params.put("pass", binding.passwordInput.getText().toString().trim());
        params.put("notes", binding.notesInput.getText().toString().trim());

        if (isEditMode) {
            params.put("id", String.valueOf(editingId));
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d(TAG, "Server Response: " + response);
                    handleServerResponse(response);
                    binding.saveButton.setEnabled(true);
                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.getMessage());
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    binding.saveButton.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void handleServerResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getBoolean("success");

            if (success) {
                Toast.makeText(this,
                        isEditMode ? "Password updated!" : "Password saved!",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String errorMsg = jsonResponse.optString("message", "Unknown error occurred");
                Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
            Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
        }
    }
}