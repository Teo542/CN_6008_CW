package com.cityfix.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cityfix.R;

public class ReportDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");
        String status = getIntent().getStringExtra("status");
        String address = getIntent().getStringExtra("address");
        String userName = getIntent().getStringExtra("userName");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Report Detail");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvStatus = findViewById(R.id.tv_status);
        TextView tvAddress = findViewById(R.id.tv_address);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvReporter = findViewById(R.id.tv_reporter);

        tvTitle.setText(title);
        tvDescription.setText(description);
        tvAddress.setText(address != null ? address : "Location unavailable");
        tvReporter.setText("Reported by: " + userName);

        tvCategory.setText(category != null ? category.toUpperCase() : "");
        tvStatus.setText(status != null ? status.replace("_", " ").toUpperCase() : "");

        tvStatus.getBackground().setTint(statusColor(status));
        tvCategory.getBackground().setTint(categoryColor(category));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private int statusColor(String status) {
        if (status == null) return Color.GRAY;
        switch (status) {
            case "open": return Color.parseColor("#F44336");
            case "in_progress": return Color.parseColor("#FF9800");
            case "resolved": return Color.parseColor("#4CAF50");
            default: return Color.GRAY;
        }
    }

    private int categoryColor(String category) {
        if (category == null) return Color.GRAY;
        switch (category) {
            case "pothole": return Color.parseColor("#795548");
            case "streetlight": return Color.parseColor("#FFC107");
            case "flooding": return Color.parseColor("#2196F3");
            case "vandalism": return Color.parseColor("#9C27B0");
            default: return Color.parseColor("#607D8B");
        }
    }
}
