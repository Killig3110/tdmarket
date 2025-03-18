package com.android.tdfruitstore.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.ui.home.HomeActivity;
import com.android.tdfruitstore.ui.home.ProductDetailActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Handler;

import kotlin.random.URandomKt;

public class Widget extends AppWidgetProvider {
    private static List<Product> productList = new ArrayList<>();
    private static int currentIndex = 0;
    private static final Handler handler = new Handler();
    private static final int UPDATE_INTERVAL = 5000; // 5 giây

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ProductDetailActivity productDetailActivity = new ProductDetailActivity();
        productDetailActivity.defaultProduct(productList);
        updateWidget(context, appWidgetManager);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager) {
        handler.postDelayed(() -> {
            if (!productList.isEmpty()) {
                currentIndex = new Random().nextInt(productList.size());
                Product product = productList.get(currentIndex);

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                views.setTextViewText(R.id.widget_product_name, product.getName());
                String price = String.format("$ %.2f", product.getPrice());
                views.setTextViewText(R.id.widget_product_price, price );
                // Tải ảnh từ URL và cập nhật vào Widget
                loadImageFromUrl(context, product.getImageUrl(), views, appWidgetManager);

                // Khi nhấn vào widget, mở ProductDetailActivity
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("product", product);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                views.setOnClickPendingIntent(R.id.widget_order_button, pendingIntent);

                ComponentName widget = new ComponentName(context, Widget.class);
                appWidgetManager.updateAppWidget(widget, views);
            }
            updateWidget(context, appWidgetManager);
        }, UPDATE_INTERVAL);
    }

    private void loadImageFromUrl(Context context, String imageUrl, RemoteViews views, AppWidgetManager appWidgetManager) {
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        views.setImageViewBitmap(R.id.widget_product_image, bitmap);
                        ComponentName widget = new ComponentName(context, Widget.class);
                        appWidgetManager.updateAppWidget(widget, views);
                    }
                });
    }

}