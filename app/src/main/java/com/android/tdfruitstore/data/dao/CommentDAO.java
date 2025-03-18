package com.android.tdfruitstore.data.dao;

import android.util.Log;
import androidx.annotation.NonNull;
import com.android.tdfruitstore.data.entities.Comment;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private static final String COLLECTION_NAME = "comments";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * 🔥 Thêm một bình luận mới vào Firestore.
     */
    public void insertComment(Comment comment, FirestoreCallback<String> callback) {
        db.collection(COLLECTION_NAME)
                .add(comment)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * 🔥 Lấy tất cả bình luận theo `productId`.
     */
    public void getCommentsByProduct(String productId, FirestoreCallback<List<Comment>> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("productId", productId)
                .orderBy("createdAt", Query.Direction.DESCENDING) // Lấy theo thứ tự mới nhất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> commentList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comment.setId(doc.getId());
                            commentList.add(comment);
                        }
                    }
                    callback.onSuccess(commentList);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * 🔥 Cập nhật bình luận dựa trên ID.
     */
    public void updateComment(String commentId, String newContent, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_NAME)
                .document(commentId)
                .update("commentText", newContent, "createdAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * 🔥 Xóa bình luận theo ID.
     */
    public void deleteComment(String commentId, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_NAME)
                .document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * 🔥 Kiểm tra xem user đã bình luận sản phẩm chưa.
     */
    public void hasUserCommented(String productId, String userId, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("productId", productId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("parentCommentId", null) // Chỉ kiểm tra bình luận cha
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(!querySnapshot.isEmpty()))
                .addOnFailureListener(callback::onFailure);
    }

    // Đếm số lượng bình luận theo `productId`
    public void countCommentsByProduct(String productId, FirestoreCallback<Integer> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(querySnapshot.size()))
                .addOnFailureListener(callback::onFailure);
    }
}
