package com.android.tdfruitstore.data;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonReader {
    private static final String TAG = "JSON_READER";

    public static String readJsonFromRaw(Context context, int rawResource) {
        try {
            InputStream is = context.getResources().openRawResource(rawResource);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đọc file JSON!", e);
            return null;
        }
    }
}
