package com.cityfix.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cityfix.R;
import com.cityfix.adapters.CommentAdapter;
import com.cityfix.models.Comment;
import com.cityfix.models.StatusUpdate;
import com.cityfix.repositories.ReportRepository;
import com.cityfix.utils.StatusFormatter;
import com.cityfix.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays the full details of a single fault report, including the photo,
 * status history timeline, upvote count, and a live comment thread.
 * Report metadata is received via Intent extras. Real-time comment updates
 * are delivered through a {@link androidx.lifecycle.MutableLiveData} listener
 * registered against {@link ReportRepository}.
 */
public class ReportDetailActivity extends AppCompatActivity {

    private ReportRepository reportRepository;
    private String reportId;
    private TextView tvUpvoteCount;
    private LinearLayout llStatusHistory;
    private TextView tvHistoryEmpty;
    private CommentAdapter commentAdapter;
    private TextView tvCommentsEmpty;
    private TextView tvTitle, tvCategory, tvStatus, tvAddress, tvDescription, tvReporter;
    private ImageView ivReportPhoto;
    private MaterialButton btnUpvote;
    private SwipeRefreshLayout swipeRefresh;
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

        swipeRefresh = findViewById(R.id.swipe_refresh_detail);
        tvTitle = findViewById(R.id.tv_title);
        tvCategory = findViewById(R.id.tv_category);
        tvStatus = findViewById(R.id.tv_status);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvReporter = findViewById(R.id.tv_reporter);
        ivReportPhoto = findViewById(R.id.iv_report_photo);
        tvUpvoteCount = findViewById(R.id.tv_upvote_count);
        llStatusHistory = findViewById(R.id.ll_status_history);
        tvHistoryEmpty = findViewById(R.id.tv_history_empty);
        tvCommentsEmpty = findViewById(R.id.tv_comments_empty);
        btnUpvote = findViewById(R.id.btn_upvote);
        swipeRefresh.setOnRefreshListener(() -> refreshReportDetail(true));

        tvTitle.setText(title);
        tvDescription.setText(description);
        tvAddress.setText(address != null ? address : "Location unavailable");
        tvReporter.setText("Reported by: " + userName);
        tvCategory.setText(category != null ? category.toUpperCase() : "");
        tvStatus.setText(StatusFormatter.formatStatus(status).toUpperCase());
        if (tvStatus.getBackground() != null) tvStatus.getBackground().mutate().setTint(statusColor(status));
        if (tvCategory.getBackground() != null) tvCategory.getBackground().mutate().setTint(categoryColor(category));

        // Comments RecyclerView
        RecyclerView recyclerComments = findViewById(R.id.recycler_comments);
        commentAdapter = new CommentAdapter(new ArrayList<>());
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setAdapter(commentAdapter);

        commentsLiveData.observe(this, comments -> {
            commentAdapter.updateComments(comments);
            tvCommentsEmpty.setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        reportRepository = new ReportRepository();
        refreshReportDetail(false);
        if (reportId != null) {
            reportRepository.listenToComments(reportId, commentsLiveData);
        }

        btnUpvote.setOnClickListener(v -> {
            if (reportId == null) return;
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Sign in to upvote", Toast.LENGTH_SHORT).show();
                return;
            }
            btnUpvote.setEnabled(false);
            reportRepository.upvoteReport(reportId, currentUser.getUid())
                    .addOnSuccessListener(unused -> refreshReportDetail(false))
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
            if (reportId == null) return;
            String commentError = ValidationUtils.commentError(text);
            if (commentError != null) {
                Toast.makeText(this, commentError, Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser == null) {
                Toast.makeText(this, "Sign in to comment", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = fbUser.getUid();
            String name = fbUser.getDisplayName() != null ? fbUser.getDisplayName() : "User";

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

    private void refreshReportDetail(boolean showErrors) {
        if (reportId == null) {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }

        reportRepository.getReport(reportId)
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        if (showErrors) Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    populateFromSnapshot(doc);
                })
                .addOnFailureListener(e -> {
                    if (showErrors) Toast.makeText(this, "Could not refresh report", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    loadStatusHistory();
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                });
    }

    private void populateFromSnapshot(DocumentSnapshot doc) {
        String title = doc.getString("title");
        String description = doc.getString("description");
        String category = doc.getString("category");
        String status = doc.getString("status");
        String address = doc.getString("address");
        String userName = doc.getString("userName");

        tvTitle.setText(title != null ? title : "");
        tvDescription.setText(description != null ? description : "");
        tvAddress.setText(address != null ? address : "Location unavailable");
        tvReporter.setText("Reported by: " + (userName != null ? userName : "Unknown user"));
        tvCategory.setText(category != null ? category.toUpperCase(Locale.ROOT) : "");
        tvStatus.setText(StatusFormatter.formatStatus(status).toUpperCase(Locale.ROOT));
        if (tvStatus.getBackground() != null) tvStatus.getBackground().mutate().setTint(statusColor(status));
        if (tvCategory.getBackground() != null) tvCategory.getBackground().mutate().setTint(categoryColor(category));

        Long upvotes = doc.getLong("upvotes");
        long count = upvotes != null ? upvotes : 0;
        tvUpvoteCount.setText(count + " people agree");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            java.util.List<?> upvoterIds = (java.util.List<?>) doc.get("upvoterIds");
            boolean alreadyUpvoted = upvoterIds != null && upvoterIds.contains(user.getUid());
            btnUpvote.setEnabled(!alreadyUpvoted);
            btnUpvote.setText(alreadyUpvoted ? "Upvoted" : "Upvote");
        } else {
            btnUpvote.setEnabled(true);
            btnUpvote.setText("Upvote");
        }

        loadPhotoFromSnapshot(doc);
    }

    private void loadPhotoFromSnapshot(DocumentSnapshot doc) {
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            ivReportPhoto.setImageDrawable(null);
            ivReportPhoto.setVisibility(View.GONE);
            return;
        }

        try {
            byte[] bytes = Base64.decode(imageUrl, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                ivReportPhoto.setImageDrawable(null);
                ivReportPhoto.setVisibility(View.GONE);
                return;
            }
            ivReportPhoto.setImageBitmap(bitmap);
            ivReportPhoto.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            ivReportPhoto.setImageDrawable(null);
            ivReportPhoto.setVisibility(View.GONE);
            Toast.makeText(this, "Could not load photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStatusHistory() {
        if (reportId == null) return;
        reportRepository.getStatusHistory(reportId).addOnSuccessListener(snapshots -> {
            llStatusHistory.removeAllViews();
            if (snapshots == null || snapshots.isEmpty()) {
                tvHistoryEmpty.setVisibility(View.VISIBLE);
                return;
            }
            tvHistoryEmpty.setVisibility(View.GONE);
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());
            for (var doc : snapshots.getDocuments()) {
                StatusUpdate u = doc.toObject(StatusUpdate.class);
                if (u == null) continue;

                TextView tv = new TextView(this);
                String time = u.getTimestamp() != null
                        ? sdf.format(new Date(u.getTimestamp().toDate().getTime())) : "";
                tv.setText("- " + StatusFormatter.formatStatusTransition(
                        u.getPreviousStatus(), u.getNewStatus()) + "  (" + time + ")");
                tv.setTextColor(Color.parseColor("#9E9EB8"));
                tv.setTextSize(13f);
                tv.setPadding(0, 4, 0, 4);
                llStatusHistory.addView(tv);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (reportRepository != null) reportRepository.removeListener();
        super.onDestroy();
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
