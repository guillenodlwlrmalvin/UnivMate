package com.example.univmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class SignupActivity extends AppCompatActivity {

    // UI Elements
    private TextInputEditText edtEmail, edtUsername, edtFullName, edtPassword, edtConfirmPassword;
    private AutoCompleteTextView dropdownUserType;
    private Button btnSignUp;
    private TextView btnLogin;
    private ProgressBar progressBar;
    private LinearLayout dynamicFieldsContainer;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI Elements
        initializeViews();

        // Setup User Type Dropdown
        setupUserTypeDropdown();

        // Set Click Listeners
        setupClickListeners();
    }

    private void initializeViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtFullName = findViewById(R.id.edtFullName);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        dropdownUserType = findViewById(R.id.dropdownUserType);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        dynamicFieldsContainer = findViewById(R.id.dynamicFieldsContainer);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupUserTypeDropdown() {
        String[] userTypes = getResources().getStringArray(R.array.user_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, userTypes);
        dropdownUserType.setAdapter(adapter);

        dropdownUserType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = parent.getItemAtPosition(position).toString();
            updateDynamicFields(selectedType);
        });
    }

    private void updateDynamicFields(String userType) {
        // Clear previous dynamic fields
        dynamicFieldsContainer.removeAllViews();

        switch (userType) {
            case "Student":
                addStudentFields();
                break;
            case "Faculty":
                addFacultyFields();
                break;
            case "Staff":
                addStaffFields();
                break;
        }
    }

    private void addStudentFields() {
        // College Dropdown
        addDropdownField("College/Department", R.array.tcu_colleges, R.drawable.ic_school);

        // Course Dropdown (you'll need to create courses_array in resources)
        addDropdownField("Course", R.array.courses_array, R.drawable.ic_book);

        // Year Level Dropdown
        addDropdownField("Year Level", R.array.year_levels, R.drawable.ic_calendar);

        // Section Input
        addTextInputField("Section", " ", R.drawable.ic_group, "text");
    }

    private void addFacultyFields() {
        // College Dropdown
        addDropdownField("College/Department", R.array.tcu_colleges, R.drawable.ic_school);

        // Role Dropdown
        addDropdownField("Role", R.array.faculty_roles, R.drawable.ic_work);

        // Employee ID
        addTextInputField("Employee ID", "Enter your employee ID", R.drawable.ic_id_card, "number");
    }

    private void addStaffFields() {
        // Department Input
        addTextInputField("Department", "e.g. Maintenance, IT", R.drawable.ic_work, "text");

        // Employee ID
        addTextInputField("Employee ID", "Enter your employee ID", R.drawable.ic_id_card, "number");
    }

    private void addDropdownField(String hint, int arrayResId, int iconResId) {
        TextInputLayout layout = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox_ExposedDropdownMenu);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setHint(hint);
        layout.setStartIconDrawable(iconResId);
        layout.setStartIconTintList(ContextCompat.getColorStateList(this, R.color.primary));
        layout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.primary));
        layout.setHintTextColor(ContextCompat.getColorStateList(this, R.color.primary));

        AutoCompleteTextView dropdown = new AutoCompleteTextView(this);
        dropdown.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        dropdown.setId(View.generateViewId());
        dropdown.setTextColor(ContextCompat.getColor(this, R.color.black));
        dropdown.setInputType(InputType.TYPE_NULL); // Prevent keyboard from showing

        String[] data = getResources().getStringArray(arrayResId);
        Log.d("Dropdown", "Data: " + Arrays.toString(data));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                data);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedValue = parent.getItemAtPosition(position).toString();
            Log.d("Dropdown", "Selected value: " + selectedValue);
            // You can add additional logic here to handle the selected value
        });

        dropdown.setThreshold(1); // Show dropdown on single character input
        dropdown.setDropDownHeight(LinearLayout.LayoutParams.WRAP_CONTENT); // Set dropdown height

        layout.addView(dropdown);
        dynamicFieldsContainer.addView(layout);
    }

    private void addTextInputField(String hint, String placeholder, int iconResId, String inputType) {
        TextInputLayout layout = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox_ExposedDropdownMenu);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setHint(hint);
        layout.setStartIconDrawable(iconResId);
        layout.setStartIconTintList(ContextCompat.getColorStateList(this, R.color.primary));
        layout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.primary));
        layout.setHintTextColor(ContextCompat.getColorStateList(this, R.color.primary));

        TextInputEditText editText = new TextInputEditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint(placeholder);
        editText.setTextColor(ContextCompat.getColor(this, R.color.black));
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.hint_color));
        editText.setInputType(InputType.TYPE_CLASS_TEXT |
                (inputType.equals("number") ? InputType.TYPE_CLASS_NUMBER : 0));

        layout.addView(editText);
        dynamicFieldsContainer.addView(layout);
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> registerUser());
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        // Get input values
        String email = edtEmail.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String userType = dropdownUserType.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, username, fullName, password, confirmPassword, userType)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create our User object with all data
                            User user = new User(email, username, fullName, userType);

                            // Set additional fields based on user type
                            if (!setAdditionalUserFields(userType, user)) {
                                progressBar.setVisibility(View.GONE);
                                return;
                            }

                            // Save to database
                            saveUserToDatabase(firebaseUser.getUid(), user);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignupActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean setAdditionalUserFields(String userType, User user) {
        try {
            switch (userType) {
                case "Student":
                    TextInputLayout collegeLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(0);
                    TextInputLayout courseLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(1);
                    TextInputLayout yearLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(2);
                    TextInputLayout sectionLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(3);

                    user.setCollege(((AutoCompleteTextView) collegeLayout.getEditText()).getText().toString());
                    user.setCourse(((AutoCompleteTextView) courseLayout.getEditText()).getText().toString());
                    user.setYearLevel(((AutoCompleteTextView) yearLayout.getEditText()).getText().toString());
                    user.setSection(((TextInputEditText) sectionLayout.getEditText()).getText().toString());
                    break;

                case "Faculty":
                    TextInputLayout facCollegeLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(0);
                    TextInputLayout roleLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(1);
                    TextInputLayout facIdLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(2);

                    user.setCollege(((AutoCompleteTextView) facCollegeLayout.getEditText()).getText().toString());
                    user.setRole(((AutoCompleteTextView) roleLayout.getEditText()).getText().toString());
                    user.setEmployeeId(((TextInputEditText) facIdLayout.getEditText()).getText().toString());
                    break;

                case "Staff":
                    TextInputLayout deptLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(0);
                    TextInputLayout staffIdLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(1);

                    user.setDepartment(((TextInputEditText) deptLayout.getEditText()).getText().toString());
                    user.setEmployeeId(((TextInputEditText) staffIdLayout.getEditText()).getText().toString());
                    break;
            }
            return true;
        } catch (Exception e) {
            Log.e("SignupActivity", "Error setting additional fields", e);
            Toast.makeText(this, "Error saving user data", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean validateUserSpecificFields(String userType, User user) {
        switch (userType) {
            case "Student":
                return validateStudentFields(user);
            case "Faculty":
                return validateFacultyFields(user);
            case "Staff":
                return validateStaffFields(user);
            default:
                return false;
        }
    }

    private boolean validateInputs(String email, String username, String fullName,
                                   String password, String confirmPassword, String userType) {
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            edtEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Username is required");
            edtUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Full name is required");
            edtFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            edtConfirmPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(userType)) {
            Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateStudentFields(User user) {
        // Get references to all student fields
        TextInputLayout collegeLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(0);
        TextInputLayout courseLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(1);
        TextInputLayout yearLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(2);
        TextInputLayout sectionLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(3);

        AutoCompleteTextView collegeDropdown = (AutoCompleteTextView) collegeLayout.getChildAt(0);
        AutoCompleteTextView courseDropdown = (AutoCompleteTextView) courseLayout.getChildAt(0);
        AutoCompleteTextView yearDropdown = (AutoCompleteTextView) yearLayout.getChildAt(0);
        TextInputEditText sectionEditText = (TextInputEditText) sectionLayout.getChildAt(0);

        String college = collegeDropdown.getText().toString().trim();
        String course = courseDropdown.getText().toString().trim();
        String year = yearDropdown.getText().toString().trim();
        String section = sectionEditText.getText().toString().trim();

        // Validate each field
        if (TextUtils.isEmpty(college)) {
            collegeDropdown.setError("College is required");
            collegeDropdown.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(course)) {
            courseDropdown.setError("Course is required");
            courseDropdown.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(year)) {
            yearDropdown.setError("Year level is required");
            yearDropdown.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(section)) {
            sectionEditText.setError("Section is required");
            sectionEditText.requestFocus();
            return false;
        }

        // Set values to user object
        user.setCollege(college);
        user.setCourse(course);
        user.setYearLevel(year);
        user.setSection(section);
        return true;
    }

    private boolean validateFacultyFields(User user) {
        // Get references to all faculty fields
        TextInputLayout collegeLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(0);
        TextInputLayout roleLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(1);
        TextInputLayout employeeIdLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(2);

        AutoCompleteTextView collegeDropdown = (AutoCompleteTextView) collegeLayout.getChildAt(0);
        AutoCompleteTextView roleDropdown = (AutoCompleteTextView) roleLayout.getChildAt(0);
        TextInputEditText employeeIdEditText = (TextInputEditText) employeeIdLayout.getChildAt(0);

        String college = collegeDropdown.getText().toString().trim();
        String role = roleDropdown.getText().toString().trim();
        String employeeId = employeeIdEditText.getText().toString().trim();

        // Validate each field
        if (TextUtils.isEmpty(college)) {
            collegeDropdown.setError("College is required");
            collegeDropdown.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(role)) {
            roleDropdown.setError("Role is required");
            roleDropdown.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(employeeId)) {
            employeeIdEditText.setError("Employee ID is required");
            employeeIdEditText.requestFocus();
            return false;
        }

        // Set values to user object
        user.setCollege(college);
        user.setRole(role);
        user.setEmployeeId(employeeId);
        return true;
    }

    private boolean validateStaffFields(User user) {
        // Get references to all staff fields
        TextInputLayout departmentLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(0);
        TextInputLayout employeeIdLayout = (TextInputLayout) dynamicFieldsContainer.getChildAt(1);

        TextInputEditText departmentEditText = (TextInputEditText) departmentLayout.getChildAt(0);
        TextInputEditText employeeIdEditText = (TextInputEditText) employeeIdLayout.getChildAt(0);

        String department = departmentEditText.getText().toString().trim();
        String employeeId = employeeIdEditText.getText().toString().trim();

        // Validate each field
        if (TextUtils.isEmpty(department)) {
            departmentEditText.setError("Department is required");
            departmentEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(employeeId)) {
            employeeIdEditText.setError("Employee ID is required");
            employeeIdEditText.requestFocus();
            return false;
        }

        // Set values to user object
        user.setDepartment(department);
        user.setEmployeeId(employeeId);
        return true;
    }

    private void saveUserToDatabase(String userId, User user) {
        Log.d("SignupActivity", "Attempting to save user: " + user.toString());
        Log.d("SignupActivity", "Path: users/" + userId);

        mDatabase.child("users").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Log.d("SignupActivity", "User saved successfully");
                        Toast.makeText(SignupActivity.this,
                                "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Log.e("SignupActivity", "Failed to save user", task.getException());
                        Toast.makeText(SignupActivity.this,
                                "Failed to save user details: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show(); // Changed to LONG to see full error
                    }
                });
    }
}
