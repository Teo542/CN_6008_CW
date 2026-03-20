package com.cityfix.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cityfix.R;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerReports;
    private SwipeRefreshLayout swipeRefresh;

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

        recyclerReports.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefresh.setOnRefreshListener(() -> {
            // TODO Sprint 3: reload reports from Firestore
            swipeRefresh.setRefreshing(false);
        });

        // TODO Sprint 3: set up ReportAdapter and Firestore listener
    }
}
