package com.example.mobileproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mobileproject.databinding.ActivityAllPasswordsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllPasswords extends AppCompatActivity {

    private ActivityAllPasswordsBinding binding;
    private PasswordAdapter passwordAdapter;
    private final List<Password> passwordList = new ArrayList<>();

    private static final String TAG = "AllPasswords";
    private static final String THEME_PREFS = "ThemePrefs";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String USER_PREFS = "UserPrefs";
    private static final int CALL_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);

        binding = ActivityAllPasswordsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupFAB();
        fetchPasswords();
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
            getSupportActionBar().setTitle("My Passwords");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        passwordAdapter = new PasswordAdapter(passwordList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(passwordAdapter);
        binding.recyclerView.setHasFixedSize(true);
    }

    private void setupFAB() {
        binding.fab.setOnClickListener(view -> {
            Intent intent = new Intent(AllPasswords.this, AddPassword.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_passwords, menu);

        // Set initial theme icon
        MenuItem themeItem = menu.findItem(R.id.action_toggle_theme);
        if (themeItem != null) {
            boolean isDarkMode = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
                    .getBoolean(DARK_MODE_KEY, false);
            themeItem.setIcon(isDarkMode ? R.drawable.ic_moon : R.drawable.ic_sun);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_theme) {
            toggleTheme();
            return true;
        } else if (id == R.id.action_add_password) {
            startAddPasswordActivity();
            return true;
        } else if (id == R.id.action_call) {
            makePhoneCall();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        boolean newMode = !prefs.getBoolean(DARK_MODE_KEY, false);
        prefs.edit().putBoolean(DARK_MODE_KEY, newMode).apply();

        // Update the theme icon
        MenuItem themeItem = binding.toolbar.getMenu().findItem(R.id.action_toggle_theme);
        if (themeItem != null) {
            themeItem.setIcon(newMode ? R.drawable.ic_moon : R.drawable.ic_sun);
        }

        recreate();
    }

    private void startAddPasswordActivity() {
        Intent intent = new Intent(this, AddPassword.class);
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Clear theme preferences
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        SharedPreferences.Editor themeEditor = themePrefs.edit();
        themeEditor.clear();
        themeEditor.apply();

        // Navigate to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPasswords();
    }

    private void fetchPasswords() {
        SharedPreferences prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String userKey = prefs.getString("userKey", null);

        if (userId == null || userKey == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String url = "http://10.21.136.222//myproject/getps.php";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                this::handleResponse,
                error -> {
                    Log.e(TAG, "Network error", error);
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                params.put("user_key", userKey);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void handleResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            if (!json.getBoolean("success")) {
                Toast.makeText(this, json.optString("message", "Server error"), Toast.LENGTH_LONG).show();
                return;
            }

            JSONArray dataArray = json.getJSONArray("data");
            passwordList.clear();

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                passwordList.add(new Password(
                        item.getInt("id"),
                        item.getString("account"),
                        item.optString("username", ""),
                        item.getString("password"),
                        item.optString("notes", ""),
                        item.getString("createdAt")
                ));
            }

            runOnUiThread(() -> {
                passwordAdapter.notifyDataSetChanged();
                if (passwordList.isEmpty()) {
                    Toast.makeText(this, "No passwords found", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST_CODE);
        } else {
            startPhoneCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPhoneCall();
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startPhoneCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:1234567890")); // Replace with your phone number
            startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Error making call", Toast.LENGTH_SHORT).show();
        }
    }
}