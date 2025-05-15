package com.example.mobileproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mobileproject.databinding.ActivityNewAccountBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class NewAccount extends AppCompatActivity {

    private ActivityNewAccountBinding binding;
    private static final String TAG = "NewAccountDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Create Account");

        // Create Account button click
        binding.createAccountBtn.setOnClickListener(view -> {
            String username = binding.newUsername.getText().toString().trim();
            String password = binding.newPassword.getText().toString().trim();

            Log.d(TAG, "Clicked register: " + username + ", pass: " + password);

            if (!username.isEmpty() && !password.isEmpty()) {
                if (isPasswordStrong(password)) {
                    registerUser(username, password);
                } else {
                    showWeakPasswordAlert();
                }
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Password generator button click
        binding.btnGeneratePassword.setOnClickListener(v -> {
            String generatedPassword = generateStrongPassword();
            binding.newPassword.setText(generatedPassword);
            Toast.makeText(this, "Strong password generated!", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return Pattern.compile(pattern).matcher(password).matches();
    }

    private void showWeakPasswordAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Weak Password")
                .setMessage("Your password must have:\n• At least 8 characters\n• 1 uppercase\n• 1 number\n• 1 special character")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String generateStrongPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "@#$%^&*()-_=+[]{}<>?!";
        String all = upper + lower + numbers + special;

        String password;
        do {
            StringBuilder sb = new StringBuilder();
            sb.append(upper.charAt((int)(Math.random() * upper.length())));
            sb.append(lower.charAt((int)(Math.random() * lower.length())));
            sb.append(numbers.charAt((int)(Math.random() * numbers.length())));
            sb.append(special.charAt((int)(Math.random() * special.length())));

            for (int i = 4; i < 12; i++) {
                sb.append(all.charAt((int)(Math.random() * all.length())));
            }

            password = sb.toString();
        } while (!isPasswordStrong(password)); // Ensure the password matches the regex

        return password;
    }

    private void registerUser(String username, String password) {
        String registerUrl = "http://10.21.136.222/myproject/rego.php";  // PHP registration endpoint

        StringRequest registerRequest = new StringRequest(Request.Method.POST, registerUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            // Registration successful, navigate to login
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(registerRequest);
    }
}
