package com.cityfix.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cityfix.R;
import com.cityfix.activities.AdminDashboardActivity;
import com.cityfix.activities.AuthActivity;
import com.cityfix.models.User;
import com.cityfix.repositories.UserRepository;
import com.cityfix.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private UserRepository userRepository;

    private TextView tvAvatar, tvDisplayName, tvEmail, tvReportsCount;
    private MaterialButton btnAdminDashboard, btnSignOut;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository();

        tvAvatar = view.findViewById(R.id.tv_avatar);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvReportsCount = view.findViewById(R.id.tv_reports_count);
        btnAdminDashboard = view.findViewById(R.id.btn_admin_dashboard);
        btnSignOut = view.findViewById(R.id.btn_sign_out);

        loadUserProfile();
        setupButtons();
    }

    private void loadUserProfile() {
        String uid = userRepository.getCurrentUserId();
        if (uid == null) return;

        userRepository.getUser(uid).addOnSuccessListener(snapshot -> {
            if (!isAdded() || snapshot == null) return;
            User user = snapshot.toObject(User.class);
            if (user == null) return;

            populateUI(user);
        });
    }

    private void populateUI(User user) {
        String name = user.getDisplayName();
        tvDisplayName.setText(name);
        tvEmail.setText(user.getEmail());
        tvReportsCount.setText(String.valueOf(user.getReportsSubmitted()));

        // Initials avatar
        if (name != null && !name.isEmpty()) {
            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        // Show admin dashboard button only for admins
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            btnAdminDashboard.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {
        btnAdminDashboard.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AdminDashboardActivity.class)));

        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
