package com.cityfix.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cityfix.R;
import com.cityfix.activities.ReportDetailActivity;
import com.cityfix.adapters.ReportAdapter;
import com.cityfix.models.FaultReport;
import com.cityfix.models.User;
import com.cityfix.repositories.ReportRepository;
import com.cityfix.repositories.UserRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the authenticated user's profile: avatar, display name, email,
 * report submission count, and a personal list of submitted reports.
 * Data is delivered through {@link androidx.lifecycle.MutableLiveData} listeners
 * so the UI updates in real time. A settings icon opens {@link SettingsFragment}.
 */
public class ProfileFragment extends Fragment {

    private UserRepository userRepository;
    private ReportRepository reportRepository;
    private ReportAdapter myReportsAdapter;
    private MutableLiveData<List<FaultReport>> myReportsLiveData = new MutableLiveData<>();
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();

    private TextView tvAvatar, tvDisplayName, tvEmail, tvReportsCount, tvMyReportsEmpty;
    private LinearLayout avatarContainer;
    private SwipeRefreshLayout swipeRefresh;

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
        tvMyReportsEmpty = view.findViewById(R.id.tv_my_reports_empty);
        avatarContainer = view.findViewById(R.id.avatar_container);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_profile);
        swipeRefresh.setOnRefreshListener(this::refreshUserProfile);

        ImageButton btnSettings = view.findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v ->
                new SettingsFragment().show(getChildFragmentManager(), "settings"));

        RecyclerView recyclerMyReports = view.findViewById(R.id.recycler_my_reports);
        myReportsAdapter = new ReportAdapter(new ArrayList<>(), this::openDetail);
        recyclerMyReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMyReports.setAdapter(myReportsAdapter);

        myReportsLiveData.observe(getViewLifecycleOwner(), reports -> {
            myReportsAdapter.updateReports(reports);
            tvMyReportsEmpty.setVisibility(reports.isEmpty() ? View.VISIBLE : View.GONE);
        });

        userLiveData.observe(getViewLifecycleOwner(), user -> {
            if (user != null) populateUI(user);
        });

        loadUserProfile();
    }

    private void loadUserProfile() {
        String uid = userRepository.getCurrentUserId();
        if (uid == null) return;
        userRepository.listenToUser(uid, userLiveData);
        reportRepository.listenToUserReports(uid, myReportsLiveData);
    }

    private void refreshUserProfile() {
        String uid = userRepository.getCurrentUserId();
        if (uid == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        final int[] pendingRequests = {2};
        Runnable finishOne = () -> {
            pendingRequests[0]--;
            if (pendingRequests[0] == 0) {
                swipeRefresh.setRefreshing(false);
            }
        };

        userRepository.getUser(uid)
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) userLiveData.postValue(user);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Could not refresh profile", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> finishOne.run());

        reportRepository.getUserReports(uid)
                .addOnSuccessListener(snapshots -> {
                    List<FaultReport> reports = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            FaultReport report = doc.toObject(FaultReport.class);
                            if (report != null) {
                                report.setReportId(doc.getId());
                                reports.add(report);
                            }
                        }
                    }
                    reportRepository.sortReportsByTimestampDesc(reports);
                    myReportsLiveData.postValue(reports);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Could not refresh reports", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> finishOne.run());
    }

    private void populateUI(User user) {
        String name = user.getDisplayName();
        tvDisplayName.setText(name);
        tvEmail.setText(user.getEmail());
        tvReportsCount.setText(String.valueOf(user.getReportsSubmitted()));

        if (name != null && !name.isEmpty()) {
            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        // Apply saved avatar color if present
        if (avatarContainer != null) {
            String colorHex = user.getAvatarColor();
            if (colorHex != null && !colorHex.isEmpty()) {
                try {
                    avatarContainer.getBackground().mutate().setTint(Color.parseColor(colorHex));
                } catch (Exception ignored) {}
            }
        }
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
