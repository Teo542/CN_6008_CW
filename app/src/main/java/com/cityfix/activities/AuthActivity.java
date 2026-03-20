package com.cityfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cityfix.R;
import com.cityfix.models.User;
import com.cityfix.repositories.UserRepository;
import com.cityfix.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private UserRepository userRepository;

    private TextInputLayout tilDisplayName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etDisplayName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnPrimary;
    private TextView tvError, tvToggle;

    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        // If already signed in, go to main
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        bindViews();
        setupToggle();
        setupPrimaryButton();
    }

    private void bindViews() {
        tilDisplayName = findViewById(R.id.til_display_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnPrimary = findViewById(R.id.btn_primary);
        tvError = findViewById(R.id.tv_error);
        tvToggle = findViewById(R.id.tv_toggle);
    }

    private void setupToggle() {
        tvToggle.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateFormMode();
        });
    }

    private void updateFormMode() {
        if (isLoginMode) {
            tilDisplayName.setVisibility(View.GONE);
            tilConfirmPassword.setVisibility(View.GONE);
            btnPrimary.setText(R.string.sign_in);
            tvToggle.setText(R.string.no_account);
        } else {
            tilDisplayName.setVisibility(View.VISIBLE);
            tilConfirmPassword.setVisibility(View.VISIBLE);
            btnPrimary.setText(R.string.create_account);
            tvToggle.setText(R.string.already_have_account);
        }
        tvError.setVisibility(View.GONE);
    }

    private void setupPrimaryButton() {
        btnPrimary.setOnClickListener(v -> {
            if (isLoginMode) {
                attemptLogin();
            } else {
                attemptRegister();
            }
        });
    }

    private void attemptLogin() {
        String email = getTextFrom(etEmail);
        String password = getTextFrom(etPassword);

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> goToMain())
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(e.getMessage());
                });
    }

    private void attemptRegister() {
        String displayName = getTextFrom(etDisplayName);
        String email = getTextFrom(etEmail);
        String password = getTextFrom(etPassword);
        String confirmPassword = getTextFrom(etConfirmPassword);

        if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) return;

                    User user = new User(
                            firebaseUser.getUid(),
                            displayName,
                            email,
                            Constants.ROLE_CITIZEN
                    );

                    userRepository.createUser(user)
                            .addOnSuccessListener(v -> goToMain())
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                showError("Account created but profile save failed: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(e.getMessage());
                });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        btnPrimary.setEnabled(!loading);
        tvToggle.setEnabled(!loading);
    }

    private String getTextFrom(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
