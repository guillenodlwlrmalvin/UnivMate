package com.example.univmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView btnSignUp, btnForgotPassword, tvLoggedInUser;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup Firestore offline persistence
        try {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (Exception e) {
            System.out.println("Error setting up Firestore persistence: " + e.getMessage());
        }

        // Initialize UI components
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        progressBar = findViewById(R.id.progressBar);
        tvLoggedInUser = findViewById(R.id.tvLoggedInUser);

        // Set click listeners
        btnLogin.setOnClickListener(v -> loginUser());

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        btnForgotPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send reset email: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get current user
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Check user type and get additional user data
                            checkUserType(firebaseUser.getUid());
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Authentication failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);

                    if (document != null && document.exists()) {
                        String userType = document.getString("userType");
                        String username = document.getString("username");
                        String email = document.getString("email");
                        String fullName = document.getString("fullName");

                        // Create intent based on user type
                        Intent intent;
                        if (userType != null && userType.equals("admin")) {
                            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        }

                        // Pass all user data to the next activity
                        intent.putExtra("userId", userId);
                        intent.putExtra("username", username != null ? username : "");
                        intent.putExtra("email", email != null ? email : "");
                        intent.putExtra("fullName", fullName != null ? fullName : "");
                        intent.putExtra("userType", userType != null ? userType : "regular");

                        startActivity(intent);
                        finish();
                    } else {
                        createDefaultUserProfile(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    System.out.println("Failed to check user type: " + e.getMessage());
                    createDefaultUserProfile(userId);
                });
    }

    private void createDefaultUserProfile(String userId) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("userType", "regular");
        userData.put("email", firebaseUser.getEmail());
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Pass minimum required data
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("email", firebaseUser.getEmail());
                    intent.putExtra("userType", "regular");

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Even if profile creation fails, proceed with basic data
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("email", firebaseUser.getEmail());

                    startActivity(intent);
                    finish();
                });
    }
}