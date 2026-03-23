package com.cityfix.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cityfix.R;
import com.cityfix.activities.ReportDetailActivity;
import com.cityfix.adapters.ReportAdapter;
import com.cityfix.models.FaultReport;
import com.cityfix.viewmodels.MapViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerReports;
    private SwipeRefreshLayout swipeRefresh;
    private ReportAdapter adapter;
    private List<FaultReport> allReports = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerReports = view.findViewById(R.id.recycler_reports);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        adapter = new ReportAdapter(new ArrayList<>(), this::openDetail);
        recyclerReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReports.setAdapter(adapter);

        MapViewModel viewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        viewModel.reports.observe(getViewLifecycleOwner(), reports -> {
            allReports = reports;
            applySearch(getSearchQuery(view));
            swipeRefresh.setRefreshing(false);
        });

        swipeRefresh.setOnRefreshListener(() -> swipeRefresh.setRefreshing(false));

        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private String getSearchQuery(View view) {
        TextInputEditText et = view.findViewById(R.id.et_search);
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void applySearch(String query) {
        if (query.isEmpty()) {
            adapter.updateReports(allReports);
            return;
        }
        String lower = query.toLowerCase();
        List<FaultReport> filtered = new ArrayList<>();
        for (FaultReport r : allReports) {
            if (r.getTitle().toLowerCase().contains(lower)
                    || r.getCategory().toLowerCase().contains(lower)) {
                filtered.add(r);
            }
        }
        adapter.updateReports(filtered);
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
