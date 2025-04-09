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

    public void insertComment(Comment comment, FirestoreCallback<String> callback) {
        db.collection(COLLECTION_NAME)
                .add(comment)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

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

    public void countCommentsByProduct(String productId, FirestoreCallback<Integer> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(querySnapshot.size()))
                .addOnFailureListener(callback::onFailure);
    }
}
