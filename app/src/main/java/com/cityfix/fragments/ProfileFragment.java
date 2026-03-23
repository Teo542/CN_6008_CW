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
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cityfix.R;
import com.cityfix.activities.AdminDashboardActivity;
import com.cityfix.activities.AuthActivity;
import com.cityfix.activities.ReportDetailActivity;
import com.cityfix.adapters.ReportAdapter;
import com.cityfix.models.FaultReport;
import com.cityfix.models.User;
import com.cityfix.repositories.ReportRepository;
import com.cityfix.repositories.UserRepository;
import com.cityfix.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private UserRepository userRepository;
    private ReportRepository reportRepository;
    private ReportAdapter myReportsAdapter;
    private MutableLiveData<List<FaultReport>> myReportsLiveData = new MutableLiveData<>();

    private TextView tvAvatar, tvDisplayName, tvEmail, tvReportsCount, tvMyReportsEmpty;
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
        reportRepository = new ReportRepository();

        tvAvatar = view.findViewById(R.id.tv_avatar);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvReportsCount = view.findViewById(R.id.tv_reports_count);
        btnAdminDashboard = view.findViewById(R.id.btn_admin_dashboard);
        btnSignOut = view.findViewById(R.id.btn_sign_out);

        RecyclerView recyclerMyReports = view.findViewById(R.id.recycler_my_reports);
        myReportsAdapter = new ReportAdapter(new ArrayList<>(), this::openDetail);
        recyclerMyReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMyReports.setAdapter(myReportsAdapter);

        tvMyReportsEmpty = view.findViewById(R.id.tv_my_reports_empty);

        myReportsLiveData.observe(getViewLifecycleOwner(), reports -> {
            myReportsAdapter.updateReports(reports);
            tvMyReportsEmpty.setVisibility(reports.isEmpty() ? View.VISIBLE : View.GONE);
        });

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

        reportRepository.listenToUserReports(uid, myReportsLiveData);
    }

    private void populateUI(User user) {
        String name = user.getDisplayName();
        tvDisplayName.setText(name);
        tvEmail.setText(user.getEmail());
        tvReportsCount.setText(String.valueOf(user.getReportsSubmitted()));

        if (name != null && !name.isEmpty()) {
            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

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

    private void openDetail(FaultReport report) {
        Intent intent = new Intent(getActivity(), ReportDetailActivity.class);
        intent.putExtra("report_id", report.getReportId());
        intent.putExtra("title", report.getTitle());
        intent.putExtra("description", report.getDescription());
        intent.putExtra("category", report.getCategory());
        intent.putExtra("status", report.getStatus());
        intent.putExtra("address", report.getAddress());
        intent.putExtra("userName", report.getUserName());
        startActivity(intent);
    }
}
