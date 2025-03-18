package com.android.tdfruitstore.data.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class Comment {
    @Exclude
    private String id; // Firestore dùng String ID

    @PropertyName("userId")
    private String userId;

    @PropertyName("productId")
    private String productId;

    @PropertyName("parentCommentId")
    private String parentCommentId;

    @PropertyName("createdAt")
    private Timestamp createdAt;

    @PropertyName("commentText")
    private String commentText;

    @PropertyName("rating")
    private float rating;

    private int indentLevel = 0; // Mặc định là 0 (bình luận cha)

    public Comment() {}

    public Comment(String userId, String productId, String parentCommentId, Timestamp createdAt, String commentText, float rating) {
        this.userId = userId;
        this.productId = productId;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
        this.commentText = commentText;
        this.rating = rating;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getIndentLevel() {
        return indentLevel;
    }
    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

}
