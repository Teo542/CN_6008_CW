package com.cityfix.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.io.ByteArrayOutputStream;

public class SubmitReportFragment extends BottomSheetDialogFragment {

    private LocationHelper locationHelper;
    private ReportRepository reportRepository;
    private UserRepository userRepository;

    private ChipGroup chipGroupCategory;
    private TextInputEditText etTitle, etDescription;
    private TextView tvLocation, tvError;
    private MaterialButton btnSubmit;
    private ImageView ivPhotoPreview;
    private FrameLayout layoutPhotoPreview;

    private double currentLat = 0, currentLng = 0;
    private String currentAddress = "";
    private String selectedCategory = "other";
    private Bitmap capturedPhoto = null;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) detectLocation();
                else tvLocation.setText("Location permission denied");
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) takePictureLauncher.launch(null);
            });

    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) showPhotoPreview(bitmap);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_submit_report, container, false);
    }

    @Override
    public int getTheme() {
        return R.style.TransparentBottomSheetDialog;
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
        ivPhotoPreview = view.findViewById(R.id.iv_photo_preview);
        layoutPhotoPreview = view.findViewById(R.id.layout_photo_preview);

        view.findViewById(R.id.btn_add_photo).setOnClickListener(v -> openCamera());
        view.findViewById(R.id.tv_remove_photo).setOnClickListener(v -> removePhoto());

        setupCategoryChips();
        requestLocationAndDetect();
        btnSubmit.setOnClickListener(v -> submitReport());

        view.findViewById(R.id.row_location).setOnClickListener(v -> {
            android.widget.Toast.makeText(requireContext(),
                    "Pan the map to your location, then tap +", android.widget.Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(null);
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showPhotoPreview(Bitmap bitmap) {
        capturedPhoto = bitmap;
        ivPhotoPreview.setImageBitmap(bitmap);
        layoutPhotoPreview.setVisibility(View.VISIBLE);
    }

    private void removePhoto() {
        capturedPhoto = null;
        ivPhotoPreview.setImageBitmap(null);
        layoutPhotoPreview.setVisibility(View.GONE);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
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
        android.os.Bundle args = getArguments();
        if (args != null && args.containsKey("lat")) {
            currentLat = args.getDouble("lat");
            currentLng = args.getDouble("lng");
            currentAddress = locationHelper.getAddressFromLatLng(currentLat, currentLng);
            tvLocation.setText("📍 " + currentAddress);
            return;
        }

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
                tvLocation.setText("📍 " + currentAddress);
            } else {
                currentLat = 37.9838;
                currentLng = 23.7275;
                currentAddress = "Athens, Greece (approximate)";
                tvLocation.setText("📍 " + currentAddress);
            }
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            currentLat = 37.9838;
            currentLng = 23.7275;
            currentAddress = "Athens, Greece (approximate)";
            tvLocation.setText("📍 " + currentAddress);
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
            currentLat = 37.9838;
            currentLng = 23.7275;
            currentAddress = "Athens, Greece (approximate)";
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

            if (capturedPhoto != null) {
                report.setImageUrl(bitmapToBase64(capturedPhoto));
            }

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
