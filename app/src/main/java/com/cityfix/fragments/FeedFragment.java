package com.cityfix.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

/**
 * Displays a scrollable, searchable list of all fault reports.
 * Observes the shared {@link com.cityfix.viewmodels.MapViewModel} for live report
 * updates and filters the list client-side as the user types in the search field.
 * Tapping a report opens {@link ReportDetailActivity}; tapping the map icon
 * navigates to the corresponding pin in {@link com.cityfix.fragments.MapFragment}.
 */
public class FeedFragment extends Fragment {

    private RecyclerView recyclerReports;
    private SwipeRefreshLayout swipeRefresh;
    private ReportAdapter adapter;
    private List<FaultReport> allReports = new ArrayList<>();
    private LinearLayout layoutEmpty;

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
        layoutEmpty = view.findViewById(R.id.layout_empty);

        adapter = new ReportAdapter(new ArrayList<>(), this::openDetail);
        adapter.setOnMapClickListener(report ->
                ((com.cityfix.activities.MainActivity) requireActivity())
                        .navigateToMap(report.getLatitude(), report.getLongitude()));
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
        List<FaultReport> result;
        if (query.isEmpty()) {
            result = allReports;
        } else {
            String lower = query.toLowerCase();
            result = new ArrayList<>();
            for (FaultReport r : allReports) {
                String t = r.getTitle() != null ? r.getTitle().toLowerCase() : "";
                String c = r.getCategory() != null ? r.getCategory().toLowerCase() : "";
                if (t.contains(lower) || c.contains(lower)) result.add(r);
            }
        }
        adapter.updateReports(result);
        layoutEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
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
