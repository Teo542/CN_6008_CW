package com.cityfix.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cityfix.R;
import com.cityfix.models.FaultReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(FaultReport report);
    }

    private List<FaultReport> reports;
    private final OnReportClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    public ReportAdapter(List<FaultReport> reports, OnReportClickListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    public void updateReports(List<FaultReport> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaultReport report = reports.get(position);
        holder.bind(report);
        holder.itemView.setOnClickListener(v -> listener.onReportClick(report));
    }

    @Override
    public int getItemCount() { return reports == null ? 0 : reports.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvStatus, tvTitle, tvAddress, tvReporter, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvReporter = itemView.findViewById(R.id.tv_reporter);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(FaultReport report) {
            tvTitle.setText(report.getTitle());
            tvCategory.setText(report.getCategory().toUpperCase());
            tvStatus.setText(report.getStatus().replace("_", " ").toUpperCase());
            tvAddress.setText(report.getAddress() != null ? report.getAddress() : "Unknown location");
            tvReporter.setText("By " + report.getUserName());

            if (report.getTimestamp() != null) {
                Date date = report.getTimestamp().toDate();
                tvTime.setText(new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(date));
            }

            tvStatus.getBackground().setTint(statusColor(report.getStatus()));
            tvCategory.getBackground().setTint(categoryColor(report.getCategory()));
        }

        private int statusColor(String status) {
            switch (status) {
                case "open": return Color.parseColor("#F44336");
                case "in_progress": return Color.parseColor("#FF9800");
                case "resolved": return Color.parseColor("#4CAF50");
                default: return Color.GRAY;
            }
        }

        private int categoryColor(String category) {
            switch (category) {
                case "pothole": return Color.parseColor("#795548");
                case "streetlight": return Color.parseColor("#FFC107");
                case "flooding": return Color.parseColor("#2196F3");
                case "vandalism": return Color.parseColor("#9C27B0");
                default: return Color.parseColor("#607D8B");
            }
        }
    }
}
