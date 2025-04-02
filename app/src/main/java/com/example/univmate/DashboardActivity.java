    package com.example.univmate;

    import android.content.Intent;
    import android.content.res.Resources;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.ArrayAdapter;
    import android.widget.AutoCompleteTextView;
    import android.widget.ImageView;
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
        private RecyclerView recyclerViewRequests, recyclerViewNotifications;
        private RequestAdapter requestAdapter;
        private NotificationAdapter notificationAdapter;
        private ProgressBar progressBar;
        private SwipeRefreshLayout swipeRefreshLayout;
        private DrawerLayout drawerLayout;
        private AutoCompleteTextView categoryDropdown;
        private LinearLayout emptyStateView, errorStateView;
        private TextView txtUserName, txtUserEmail;

        // Firebase
        private FirebaseFirestore db;
        private FirebaseAuth auth;
        private List<Request> requests = new ArrayList<>();
        private List<Notification> notifications = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dashboard);

            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            initializeViews();
            setupToolbar();
            setupNavigationDrawer();
            setupCategoryDropdown();
            setupRecyclerViews();
            setupSwipeRefresh();
            setupButtonListeners();
            handleUserData();
            loadInitialData();
        }

        private void initializeViews() {
            recyclerViewRequests = findViewById(R.id.recyclerViewRequests);
            recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
            progressBar = findViewById(R.id.progressBar);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            drawerLayout = findViewById(R.id.drawerLayout);
            categoryDropdown = findViewById(R.id.dropdownCategory);
            emptyStateView = findViewById(R.id.emptyStateView);
            errorStateView = findViewById(R.id.errorStateView);
        }

        private void setupToolbar() {
            MaterialToolbar toolbar = findViewById(R.id.topAppBar);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        private void setupNavigationDrawer() {
            NavigationView navigationView = findViewById(R.id.navigationView);

            // Initialize navigation header views
            View headerView = navigationView.getHeaderView(0);
            txtUserName = headerView.findViewById(R.id.txtUserName);
            txtUserEmail = headerView.findViewById(R.id.txtUserEmail);

            // Add null checks for header views
            if (txtUserName == null) {
                Log.e("DashboardActivity", "txtUserName TextView not found in nav header");
            }
            if (txtUserEmail == null) {
                Log.e("DashboardActivity", "txtUserEmail TextView not found in nav header");
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
                }
                return false;
            });

            // Set click listener for profile image if needed
            ImageView profileImage = headerView.findViewById(R.id.imageView);
            if (profileImage != null) {
                profileImage.setOnClickListener(v -> {
                    startActivity(new Intent(this, ProfileActivity.class));
                });
            }
        }

        private void handleUserData() {
            // Get data from intent
            Intent intent = getIntent();
            String username = intent.getStringExtra("username");
            String email = intent.getStringExtra("email");
            String fullName = intent.getStringExtra("fullName");

            // Update nav header
            updateNavHeader(username, email, fullName);

            // If data is missing, fetch from Firestore
            if (username == null || email == null) {
                fetchUserData();
            }
        }



        private void updateNavHeader(String username, String email, String fullName) {
            if (txtUserName == null || txtUserEmail == null) {
                Log.e("DashboardActivity", "TextViews not initialized");
                return;
            }

            String displayName = fullName != null && !fullName.isEmpty() ? fullName :
                    (username != null && !username.isEmpty() ? username :
                            (email != null ? email.split("@")[0] : "User"));

            txtUserName.setText(displayName);
            if (email != null) {
                txtUserEmail.setText(email);
            }
        }
        private void fetchUserData() {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) return;

            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String email = documentSnapshot.getString("email");
                            String fullName = documentSnapshot.getString("fullName");
                            updateNavHeader(username, email, fullName);
                        } else {
                            // Fallback to Firebase Auth email
                            updateNavHeader(null, currentUser.getEmail(), null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DashboardActivity", "Error fetching user data", e);
                        // Fallback to Firebase Auth email
                        updateNavHeader(null, currentUser.getEmail(), null);
                    });
        }

        private void setupCategoryDropdown() {
            try {
                String[] categories = getResources().getStringArray(R.array.maintenance_categories);
                // Create a simple layout for dropdown items (dropdown_item.xml)
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_item,  // Make sure this is a simple TextView layout
                        categories
                );

                AutoCompleteTextView dropdown = findViewById(R.id.dropdownCategory);
                dropdown.setAdapter(adapter);

    // Optional: Set threshold to show suggestions immediately
                dropdown.setThreshold(1);
                categoryDropdown.setAdapter(adapter);
                categoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    String selected = parent.getItemAtPosition(position).toString();
                    if (selected.equals("Select Category")) { // Changed from string resource
                        categoryDropdown.setText("", false);
                    }
                });
            } catch (Resources.NotFoundException e) {
                Toast.makeText(this, "Categories not found", Toast.LENGTH_SHORT).show(); // Removed string resource
                Log.e("DashboardActivity", "Error loading categories", e);
            }
        }


        private void setupRecyclerViews() {
            // Requests RecyclerView
            recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));
            requestAdapter = new RequestAdapter(this, requests, new RequestAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Request request) {
                    onRequestClicked(request);
                }
            });

            recyclerViewRequests.setAdapter(requestAdapter);

            // Notifications RecyclerView
            recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
            notificationAdapter = new NotificationAdapter(
                    this,
                    notifications,
                    new NotificationAdapter.OnNotificationClickListener() {
                        @Override
                        public void onNotificationClick(Notification notification) {
                            onNotificationClicked(notification);
                        }
                    }
            );
            recyclerViewNotifications.setAdapter(notificationAdapter);
        }


        private void setupSwipeRefresh() {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                String userId = auth.getCurrentUser().getUid();
                loadRequests(userId);
                loadNotifications(userId);
            });
        }

        private void setupButtonListeners() {
            MaterialButton submitButton = findViewById(R.id.btnSubmitRequest);
            submitButton.setOnClickListener(v -> submitRequest());

            MaterialButton retryButton = findViewById(R.id.btnRetry); // Changed ID to match your layout
            retryButton.setOnClickListener(v -> {
                String userId = auth.getCurrentUser().getUid();
                loadRequests(userId);
            });
        }

        private void loadInitialData() {
            if (auth.getCurrentUser() == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            loadRequests(userId);
            loadNotifications(userId);
        }

        private void loadRequests(String userId) {
            progressBar.setVisibility(View.VISIBLE);
            errorStateView.setVisibility(View.GONE);

            db.collection("requests")
                    .whereEqualTo("userId", userId)
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
                            requestAdapter.updateRequests(requests);
                            updateEmptyState();
                        } else {
                            showErrorState(task.getException().getMessage());
                        }
                    });
        }

        private void loadNotifications(String userId) {
            db.collection("notifications")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            notifications.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notification notification = document.toObject(Notification.class);
                                notification.setId(document.getId());
                                notifications.add(notification);
                            }
                            notificationAdapter.updateNotifications(notifications);
                        }
                    });
        }

        private void submitRequest() {
            String category = categoryDropdown.getText().toString().trim();
            String userId = auth.getCurrentUser().getUid();

            if (category.isEmpty() || category.equals("Select Category")) { // Changed from string resource
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show(); // Removed string resource
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            Request newRequest = new Request(
                    userId,
                    category,
                    "Medium", // Default urgency
                    "Maintenance request", // Default description
                    "Building A", // Default location
                    "Pending" // Default status
            );

            db.collection("requests")
                    .add(newRequest)
                    .addOnSuccessListener(documentReference -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Request submitted successfully", Toast.LENGTH_SHORT).show(); // Removed string resource
                        categoryDropdown.setText("");
                        loadRequests(userId);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show(); // Removed string resource
                        Log.e("DashboardActivity", "Request submission failed", e);
                    });
        }

        private void onRequestClicked(Request request) {
            Intent intent = new Intent(this, RequestDetailActivity.class);
            intent.putExtra("request_id", request.getRequestId());
            startActivity(intent);
        }

        private void onNotificationClicked(Notification notification) {
            Intent intent = new Intent(this, NotificationDetailActivity.class);
            intent.putExtra("notification_id", notification.getId());
            startActivity(intent);
        }

        private void updateEmptyState() {
            emptyStateView.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerViewRequests.setVisibility(requests.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private void showErrorState(String errorMessage) {
            errorStateView.setVisibility(View.VISIBLE);
            recyclerViewRequests.setVisibility(View.GONE);
            TextView errorText = findViewById(R.id.tvError); // Changed to match your layout
            if (errorText != null) {
                errorText.setText(errorMessage);
            }
        }

        private void logoutUser() {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        @Override
        protected void onResume() {
            super.onResume();
            if (auth.getCurrentUser() != null) {
                loadRequests(auth.getCurrentUser().getUid());
            }
        }
    }