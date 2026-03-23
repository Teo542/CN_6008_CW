package com.cityfix.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cityfix.R;
import com.cityfix.models.FaultReport;
import com.cityfix.repositories.ReportRepository;
import com.cityfix.repositories.UserRepository;
import com.cityfix.utils.LocationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class SubmitReportFragment extends BottomSheetDialogFragment {

    private LocationHelper locationHelper;
    private ReportRepository reportRepository;
    private UserRepository userRepository;

    private ChipGroup chipGroupCategory;
    private TextInputEditText etTitle, etDescription;
    private TextView tvLocation, tvError;
    private MaterialButton btnSubmit;

    private double currentLat = 0, currentLng = 0;
    private String currentAddress = "";
    private String selectedCategory = "other";

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) detectLocation();
                else tvLocation.setText("Location permission denied");
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_submit_report, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationHelper = new LocationHelper(requireContext());
        reportRepository = new ReportRepository();
        userRepository = new UserRepository();

        chipGroupCategory = view.findViewById(R.id.chip_group_category);
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        tvLocation = view.findViewById(R.id.tv_location);
        tvError = view.findViewById(R.id.tv_error);
        btnSubmit = view.findViewById(R.id.btn_submit);

        setupCategoryChips();
        requestLocationAndDetect();
        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void setupCategoryChips() {
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip == null) return;
            int id = chip.getId();
            if (id == R.id.chip_pothole) selectedCategory = "pothole";
            else if (id == R.id.chip_streetlight) selectedCategory = "streetlight";
            else if (id == R.id.chip_flooding) selectedCategory = "flooding";
            else if (id == R.id.chip_vandalism) selectedCategory = "vandalism";
            else selectedCategory = "other";
        });
    }

    private void requestLocationAndDetect() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void detectLocation() {
        locationHelper.getLastLocation().addOnSuccessListener(location -> {
            if (!isAdded()) return;
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                currentAddress = locationHelper.getAddressFromLatLng(currentLat, currentLng);
                tvLocation.setText(currentAddress);
            } else {
                tvLocation.setText("Could not detect location");
            }
        });
    }

    private void submitReport() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (title.isEmpty()) {
            showError("Please enter a title.");
            return;
        }
        if (currentLat == 0 && currentLng == 0) {
            showError("Location not detected yet.");
            return;
        }

        String uid = userRepository.getCurrentUserId();
        if (uid == null) return;

        btnSubmit.setEnabled(false);

        userRepository.getUser(uid).addOnSuccessListener(snapshot -> {
            if (!isAdded() || snapshot == null) return;

            String userName = snapshot.getString("displayName");
            if (userName == null) userName = "Anonymous";

            FaultReport report = new FaultReport(
                    title, description, selectedCategory,
                    currentLat, currentLng, currentAddress,
                    uid, userName
            );

            reportRepository.addReport(report)
                    .addOnSuccessListener(ref -> {
                        userRepository.incrementReportsSubmitted(uid);
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        btnSubmit.setEnabled(true);
                        showError("Failed to submit: " + e.getMessage());
                    });
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
