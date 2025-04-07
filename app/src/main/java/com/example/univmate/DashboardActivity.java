package com.example.univmate;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView requestsRecyclerView;
    private RequestAdapter requestAdapter;
    private ProgressBar progressBar;
    private swipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private LinearLayout emptyStateView;
    private MaterialButton btnNewRequest, btnStartRequest;
    private TextView greetingText;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Request> requests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Make sure this matches your XML filename

        initializeFirebase();
        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        setupSwipeRefresh();
        setupButtonListeners();
        handleUserData();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        requestsRecyclerView = findViewById(R.id.requestsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        drawerLayout = findViewById(R.id.drawerLayout);
        emptyStateView = findViewById(R.id.emptyStateView);
        btnNewRequest = findViewById(R.id.btnNewRequest);
        btnStartRequest = findViewById(R.id.btnStartRequest);
        greetingText = findViewById(R.id.greetingText);

        // Configure swipe refresh colors
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
        );
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        TextView txtUserName = headerView.findViewById(R.id.txtUserName);
        TextView txtUserEmail = headerView.findViewById(R.id.txtUserEmail);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            txtUserEmail.setText(user.getEmail());
            if (user.getDisplayName() != null) {
                txtUserName.setText(user.getDisplayName());
            }
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                logoutUser();
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupRecyclerView() {
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestAdapter = new RequestAdapter(this, requests, request -> {
            Intent intent = new Intent(this, RequestDetailActivity.class);
            intent.putExtra("request_id", request.getRequestId());
            startActivity(intent);
        });
        requestsRecyclerView.setAdapter(requestAdapter);
        requestsRecyclerView.setHasFixedSize(true);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(true);
            }
            loadRequests();
        });
    }

    private void setupButtonListeners() {
        btnNewRequest.setOnClickListener(v ->
                startActivity(new Intent(this, NewRequestActivity.class)));

        btnStartRequest.setOnClickListener(v ->
                startActivity(new Intent(this, NewRequestActivity.class)));
    }

    private void handleUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            greetingText.setText(user.getDisplayName() != null ?
                    getString(R.string.greeting_with_name, user.getDisplayName()) :
                    getString(R.string.greeting_default));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void loadRequests() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);

        db.collection("requests")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (task.isSuccessful()) {
                        requests.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Request request = document.toObject(Request.class);
                            request.setRequestId(document.getId());
                            requests.add(request);
                        }
                        requestAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    } else {
                        Toast.makeText(this, "Error loading requests", Toast.LENGTH_SHORT).show();
                        Log.e("DashboardActivity", "Error loading requests", task.getException());
                    }
                });
    }

    private void updateEmptyState() {
        emptyStateView.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
        requestsRecyclerView.setVisibility(requests.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}