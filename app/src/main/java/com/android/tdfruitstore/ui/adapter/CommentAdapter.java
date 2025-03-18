package com.android.tdfruitstore.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.UserDAO;
import com.android.tdfruitstore.data.entities.Comment;
import com.android.tdfruitstore.data.entities.User;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> commentList;
    private OnReplyClickListener replyClickListener;
    private UserDAO userDAO;

    public interface OnReplyClickListener {
        void onReplyClick(Comment comment);
    }

    public CommentAdapter(Context context, List<Comment> commentList, OnReplyClickListener listener) {
        this.context = context;
        this.replyClickListener = listener;
        this.userDAO = new UserDAO();

        if (commentList == null || commentList.isEmpty()) {
            Log.e("CommentAdapter", "commentList bị null hoặc rỗng.");
            this.commentList = new ArrayList<>();
            return;
        }

        // Xây dựng danh sách comment và replies
        Map<String, List<Comment>> repliesMap = commentList.stream()
                .filter(c -> c.getParentCommentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentCommentId));

        this.commentList = commentList.stream()
                .filter(c -> c.getParentCommentId() == null)
                .flatMap(parent -> {
                    List<Comment> replies = repliesMap.getOrDefault(parent.getId(), new ArrayList<>());

                    List<Comment> combinedList = new ArrayList<>();
                    combinedList.add(parent);
                    combinedList.addAll(replies);
                    return combinedList.stream();
                })
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (commentList.isEmpty()) {
            Log.e("DEBUG", "⚠️ Adapter không có dữ liệu để hiển thị!");
            return;
        }

        Comment comment = commentList.get(position);

        if (comment.getCreatedAt() instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) comment.getCreatedAt();
            holder.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(timestamp.toDate()));  // ✅ Chuyển từ Timestamp sang Date
        } else {
            holder.tvDate.setText("Không xác định");
        }

        // 🔥 Tô màu các từ có dạng "@Username"
        SpannableString spannable = new SpannableString(comment.getCommentText());
        Pattern pattern = Pattern.compile("@\\w+[.\\w+]*:");
        Matcher matcher = pattern.matcher(comment.getCommentText());

        while (matcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.tvCommentText.setText(spannable);

        // Tải thông tin user từ Firestore (dùng đúng user ID từ comment)
        userDAO.getUserById(comment.getUserId(), new FirestoreCallback<User>() {
            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy user từ Firestore!", e);
            }

            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    holder.tvUsername.setText(user.getName());

                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(context)
                                .load(user.getAvatarUrl()) // 🔥 Load ảnh từ Firestore URL
                                .placeholder(R.drawable.ic_avatar)
                                .into(holder.ivAvatar);
                    } else {
                        holder.ivAvatar.setImageResource(R.drawable.ic_avatar);
                    }
                } else {
                    Log.e("Firestore", "❌ Không tìm thấy user trong Firestore!");
                }
            }
        });

        // 🔥 Nếu là bình luận con, hiển thị icon mũi tên
        if (comment.getParentCommentId() != null) {

            // 🔥 Thụt lề bình luận con cố định 1 cấp
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setMargins(50, 10, 10, 10); // 🔥 Chỉ thụt vào 1 cấp (50dp)
            holder.itemView.setLayoutParams(layoutParams);
        } else {

            // 🔥 Bình luận cha giữ nguyên vị trí
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setMargins(10, 10, 10, 10);
            holder.itemView.setLayoutParams(layoutParams);
        }

        // Hiển thị rating nếu có
        holder.ratingBar.setVisibility(comment.getRating() > 0 ? View.VISIBLE : View.GONE);
        holder.ratingBar.setRating(comment.getRating());

        // Xử lý khi bấm vào nút "Trả lời"
        holder.btnReply.setOnClickListener(v -> {
            if (replyClickListener != null) {
                replyClickListener.onReplyClick(comment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    public void setComments(List<Comment> newComments) {
        this.commentList.clear();
        this.commentList.addAll(newComments);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvDate, tvCommentText;
        RatingBar ratingBar;
        Button btnReply;
        CircleImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnReply = itemView.findViewById(R.id.btnReply);
        }
    }
}
