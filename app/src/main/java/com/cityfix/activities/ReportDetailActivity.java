package com.cityfix.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cityfix.R;
import com.cityfix.adapters.CommentAdapter;
import com.cityfix.models.Comment;
import com.cityfix.models.StatusUpdate;
import com.cityfix.repositories.ReportRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportDetailActivity extends AppCompatActivity {

    private ReportRepository reportRepository;
    private String reportId;
    private TextView tvUpvoteCount;
    private LinearLayout llStatusHistory;
    private TextView tvHistoryEmpty;
    private CommentAdapter commentAdapter;
    private TextView tvCommentsEmpty;
    private MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        reportId = getIntent().getStringExtra("report_id");
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
        tvUpvoteCount = findViewById(R.id.tv_upvote_count);
        llStatusHistory = findViewById(R.id.ll_status_history);
        tvHistoryEmpty = findViewById(R.id.tv_history_empty);
        tvCommentsEmpty = findViewById(R.id.tv_comments_empty);
        MaterialButton btnUpvote = findViewById(R.id.btn_upvote);

        tvTitle.setText(title);
        tvDescription.setText(description);
        tvAddress.setText(address != null ? address : "Location unavailable");
        tvReporter.setText("Reported by: " + userName);
        tvCategory.setText(category != null ? category.toUpperCase() : "");
        tvStatus.setText(status != null ? status.replace("_", " ").toUpperCase() : "");
        tvStatus.getBackground().setTint(statusColor(status));
        tvCategory.getBackground().setTint(categoryColor(category));

        // Comments RecyclerView
        RecyclerView recyclerComments = findViewById(R.id.recycler_comments);
        commentAdapter = new CommentAdapter(new ArrayList<>());
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setAdapter(commentAdapter);

        commentsLiveData.observe(this, comments -> {
            commentAdapter.updateComments(comments);
            tvCommentsEmpty.setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
        });

        reportRepository = new ReportRepository();
        loadUpvotes();
        loadStatusHistory();
        if (reportId != null) {
            reportRepository.listenToComments(reportId, commentsLiveData);
        }

        btnUpvote.setOnClickListener(v -> {
            if (reportId == null) return;
            btnUpvote.setEnabled(false);
            reportRepository.upvoteReport(reportId)
                    .addOnSuccessListener(unused -> loadUpvotes())
                    .addOnFailureListener(e -> {
                        btnUpvote.setEnabled(true);
                        Toast.makeText(this, "Failed to upvote", Toast.LENGTH_SHORT).show();
                    });
        });

        // Send comment
        TextInputEditText etComment = findViewById(R.id.et_comment);
        MaterialButton btnSend = findViewById(R.id.btn_send_comment);
        btnSend.setOnClickListener(v -> {
            if (etComment.getText() == null) return;
            String text = etComment.getText().toString().trim();
            if (text.isEmpty() || reportId == null) return;

            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            String uid = fbUser != null ? fbUser.getUid() : "";
            String name = fbUser != null && fbUser.getDisplayName() != null
                    ? fbUser.getDisplayName() : "User";

            btnSend.setEnabled(false);
            Comment comment = new Comment(text, uid, name, false);
            reportRepository.addComment(reportId, comment)
                    .addOnSuccessListener(ref -> {
                        etComment.setText("");
                        btnSend.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        btnSend.setEnabled(true);
                        Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadStatusHistory() {
        if (reportId == null) return;
        reportRepository.getStatusHistory(reportId).addOnSuccessListener(snapshots -> {
            if (snapshots == null || snapshots.isEmpty()) {
                tvHistoryEmpty.setVisibility(View.VISIBLE);
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());
            for (var doc : snapshots.getDocuments()) {
                StatusUpdate u = doc.toObject(StatusUpdate.class);
                if (u == null) continue;

                TextView tv = new TextView(this);
                String time = u.getTimestamp() != null
                        ? sdf.format(new Date(u.getTimestamp().toDate().getTime())) : "";
                String prev = u.getPreviousStatus() != null ? u.getPreviousStatus().replace("_", " ") : "?";
                String next = u.getNewStatus() != null ? u.getNewStatus().replace("_", " ") : "?";
                tv.setText("• " + prev + " → " + next + "  (" + time + ")");
                tv.setTextColor(Color.parseColor("#9E9EB8"));
                tv.setTextSize(13f);
                tv.setPadding(0, 4, 0, 4);
                llStatusHistory.addView(tv);
            }
        });
    }

    private void loadUpvotes() {
        if (reportId == null) return;
        reportRepository.getReport(reportId).addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) return;
            Long upvotes = doc.getLong("upvotes");
            long count = upvotes != null ? upvotes : 0;
            tvUpvoteCount.setText(count + " people agree");
            findViewById(R.id.btn_upvote).setEnabled(true);
        });
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
