package com.cityfix.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cityfix.R;
import com.cityfix.models.FaultReport;
import com.cityfix.repositories.ReportRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private AdminAdapter adapter;
    private final ReportRepository repo = new ReportRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recycler = findViewById(R.id.recycler_admin_reports);
        adapter = new AdminAdapter(new ArrayList<>(), (reportId, status) -> {
            repo.updateStatus(reportId, status)
                .addOnSuccessListener(v -> Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        loadReports();
    }

    private void loadReports() {
        FirebaseFirestore.getInstance()
            .collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null || snapshots == null) return;
                List<FaultReport> list = new ArrayList<>();
                for (var doc : snapshots.getDocuments()) {
                    FaultReport r = doc.toObject(FaultReport.class);
                    if (r != null) {
                        r.setReportId(doc.getId());
                        list.add(r);
                    }
                }
                adapter.update(list);
            });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    // ---- Inner Adapter ----
    interface OnStatusChange { void onChange(String reportId, String status); }

    static class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.VH> {
        private List<FaultReport> items;
        private final OnStatusChange listener;

        AdminAdapter(List<FaultReport> items, OnStatusChange listener) {
            this.items = items;
            this.listener = listener;
        }

        void update(List<FaultReport> newItems) { this.items = newItems; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_report, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            FaultReport r = items.get(position);
            h.tvTitle.setText(r.getTitle());
            h.tvAddress.setText(r.getAddress() != null ? r.getAddress() : "No address");
            h.tvStatus.setText(r.getStatus().replace("_", " ").toUpperCase());
            h.tvStatus.getBackground().setTint(statusColor(r.getStatus()));

            h.btnOpen.setOnClickListener(v -> listener.onChange(r.getReportId(), "open"));
            h.btnInProgress.setOnClickListener(v -> listener.onChange(r.getReportId(), "in_progress"));
            h.btnResolved.setOnClickListener(v -> listener.onChange(r.getReportId(), "resolved"));
        }

        @Override public int getItemCount() { return items.size(); }

        private int statusColor(String s) {
            switch (s) {
                case "open": return Color.parseColor("#F44336");
                case "in_progress": return Color.parseColor("#FF9800");
                case "resolved": return Color.parseColor("#4CAF50");
                default: return Color.GRAY;
            }
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStatus, tvAddress;
            Button btnOpen, btnInProgress, btnResolved;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_title);
                tvStatus = v.findViewById(R.id.tv_status);
                tvAddress = v.findViewById(R.id.tv_address);
                btnOpen = v.findViewById(R.id.btn_open);
                btnInProgress = v.findViewById(R.id.btn_in_progress);
                btnResolved = v.findViewById(R.id.btn_resolved);
            }
        }
    }
}
