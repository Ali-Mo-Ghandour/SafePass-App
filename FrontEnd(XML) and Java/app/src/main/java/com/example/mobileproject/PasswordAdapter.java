package com.example.mobileproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {
    private final List<Password> passwordList;
    private static final String TAG = "PasswordAdapter";

    public PasswordAdapter(List<Password> passwordList) {
        this.passwordList = passwordList;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        Password password = passwordList.get(position);

        Log.d(TAG, "Displaying: " + password.account);

        holder.account.setText(password.account);
        holder.username.setText(String.format("Username: %s", password.username));
        holder.password.setText("••••••••"); // Initially masked
        holder.createdAt.setText(String.format("Added: %s", password.createdAt));

        if (password.notes != null && !password.notes.isEmpty()) {
            holder.notes.setText(password.notes);
            holder.notes.setVisibility(View.VISIBLE);
        } else {
            holder.notes.setVisibility(View.GONE);
        }

        holder.isPasswordVisible = false;
        holder.eyeButton.setImageResource(android.R.drawable.ic_menu_view); // Initial eye icon

        holder.eyeButton.setOnClickListener(v -> {
            holder.isPasswordVisible = !holder.isPasswordVisible;
            if (holder.isPasswordVisible) {
                holder.password.setText(password.password);
                holder.eyeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                holder.password.setText("••••••••");
                holder.eyeButton.setImageResource(android.R.drawable.ic_menu_view);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddPassword.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("id", password.id);
            intent.putExtra("account", password.account);
            intent.putExtra("username", password.username);
            intent.putExtra("password", password.password);
            intent.putExtra("notes", password.notes);
            v.getContext().startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Password")
                    .setMessage("Are you sure you want to delete this password?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deletePasswordFromServer(v, password.id, holder.getAdapterPosition());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return passwordList.size();
    }

    static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView account, username, password, notes, createdAt;
        ImageButton eyeButton, editButton, deleteButton;
        boolean isPasswordVisible;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            account = itemView.findViewById(R.id.accountText);
            username = itemView.findViewById(R.id.usernameText);
            password = itemView.findViewById(R.id.passwordText);
            notes = itemView.findViewById(R.id.notesText);
            createdAt = itemView.findViewById(R.id.createdAtText);
            eyeButton = itemView.findViewById(R.id.btnToggleVisibility);
            editButton = itemView.findViewById(R.id.btnEdit);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void deletePasswordFromServer(View view, int id, int position) {
        // Get user ID from SharedPreferences
        SharedPreferences prefs = view.getContext().getSharedPreferences("UserPrefs", 0);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Toast.makeText(view.getContext(), "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.21.136.222/myproject/deletepasso.php";

        Log.d(TAG, "Attempting to delete password - ID: " + id + ", User ID: " + userId);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Server response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");

                        if (success) {
                            passwordList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(view.getContext(), "Password deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        Toast.makeText(view.getContext(), "Error processing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.getMessage());
                    Toast.makeText(view.getContext(), "Delete failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                params.put("id", String.valueOf(id));
                Log.d(TAG, "Request parameters: " + params.toString());
                return params;
            }
        };

        Volley.newRequestQueue(view.getContext()).add(request);
    }
}