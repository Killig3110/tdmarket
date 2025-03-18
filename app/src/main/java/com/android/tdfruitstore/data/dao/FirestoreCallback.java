package com.android.tdfruitstore.data.dao;

public interface FirestoreCallback<T> {
    void onFailure(Exception e);
    void onSuccess(T result);
}
