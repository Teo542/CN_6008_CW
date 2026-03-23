package com.cityfix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cityfix.R;
import com.cityfix.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {

    private List<Comment> comments;
    private final SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Comment c = comments.get(position);
        h.tvUser.setText(c.getUserName() != null ? c.getUserName() : "Anonymous");
        h.tvText.setText(c.getText());

        if (c.getTimestamp() != null) {
            h.tvTime.setText(sdf.format(new Date(c.getTimestamp().toDate().getTime())));
        } else {
            h.tvTime.setText("");
        }

        if (c.isOfficial()) {
            h.tvOfficialBadge.setVisibility(View.VISIBLE);
        } else {
            h.tvOfficialBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUser, tvText, tvTime, tvOfficialBadge;
        VH(View v) {
            super(v);
            tvUser = v.findViewById(R.id.tv_comment_user);
            tvText = v.findViewById(R.id.tv_comment_text);
            tvTime = v.findViewById(R.id.tv_comment_time);
            tvOfficialBadge = v.findViewById(R.id.tv_official_badge);
        }
    }
}
