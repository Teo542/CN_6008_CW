package com.cityfix.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cityfix.R;
import com.cityfix.activities.AdminDashboardActivity;
import com.cityfix.activities.AuthActivity;
import com.cityfix.models.User;
import com.cityfix.repositories.UserRepository;
import com.cityfix.utils.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends BottomSheetDialogFragment {

    private final UserRepository userRepository = new UserRepository();
    private User currentUser;

    // Avatar color options
    private static final int[] AVATAR_COLORS = {
        Color.parseColor("#FF6B35"), // orange (default)
        Color.parseColor("#6C63FF"), // purple
        Color.parseColor("#2196F3"), // blue
        Color.parseColor("#4CAF50"), // green
        Color.parseColor("#E91E63"), // pink
        Color.parseColor("#FF9800"), // amber
    };
    private static final String[] AVATAR_COLOR_HEX = {
        "#FF6B35", "#6C63FF", "#2196F3", "#4CAF50", "#E91E63", "#FF9800"
    };

    @Override
    public int getTheme() {
        return R.style.TransparentBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout rowChangeName = view.findViewById(R.id.row_change_name);
        LinearLayout rowChangeAvatar = view.findViewById(R.id.row_change_avatar);
        LinearLayout rowAdmin = view.findViewById(R.id.row_admin);
        LinearLayout rowSignOut = view.findViewById(R.id.row_sign_out);

        String uid = userRepository.getCurrentUserId();
        if (uid != null) {
            userRepository.getUser(uid).addOnSuccessListener(snapshot -> {
                if (!isAdded() || snapshot == null) return;
                currentUser = snapshot.toObject(User.class);
                if (currentUser != null && Constants.ROLE_ADMIN.equals(currentUser.getRole())) {
                    rowAdmin.setVisibility(View.VISIBLE);
                }
            });
        }

        rowChangeName.setOnClickListener(v -> showChangeNameDialog());

        rowChangeAvatar.setOnClickListener(v -> showAvatarColorPicker());

        rowAdmin.setOnClickListener(v -> {
            dismiss();
            startActivity(new Intent(requireContext(), AdminDashboardActivity.class));
        });

        rowSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void showChangeNameDialog() {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        til.setHint("Display Name");
        til.setPadding(48, 24, 48, 8);

        TextInputEditText et = new TextInputEditText(til.getContext());
        if (currentUser != null) et.setText(currentUser.getDisplayName());
        til.addView(et);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change Display Name")
                .setView(til)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = et.getText() != null ? et.getText().toString().trim() : "";
                    if (!newName.isEmpty()) saveName(newName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveName(String name) {
        String uid = userRepository.getCurrentUserId();
        if (uid == null) return;
        userRepository.updateDisplayName(uid, name)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    android.widget.Toast.makeText(requireContext(),
                            "Name updated!", android.widget.Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    private void showAvatarColorPicker() {
        String[] colorNames = {"Orange", "Purple", "Blue", "Green", "Pink", "Amber"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose Avatar Color")
                .setItems(colorNames, (dialog, which) -> saveAvatarColor(AVATAR_COLOR_HEX[which]))
                .show();
    }

    private void saveAvatarColor(String colorHex) {
        String uid = userRepository.getCurrentUserId();
        if (uid == null) return;
        userRepository.updateAvatarColor(uid, colorHex)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    android.widget.Toast.makeText(requireContext(),
                            "Avatar color updated!", android.widget.Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }
}
