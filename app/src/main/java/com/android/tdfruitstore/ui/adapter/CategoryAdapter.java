package com.android.tdfruitstore.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.entities.Category;
import com.android.tdfruitstore.ui.home.AllProductsActivity;
import com.bumptech.glide.Glide;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener categoryClickListener;

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.categoryClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getCategoryName());

        // 🔥 Load ảnh danh mục từ Firestore bằng Glide
        Glide.with(context)
                .load(category.getImageResId()) // URL ảnh từ Firestore
                .placeholder(R.drawable.ic_placeholder) // Ảnh mặc định nếu tải chậm
                .into(holder.ivCategoryImage);

        // 🔥 Bắt sự kiện khi bấm vào danh mục
        holder.itemView.setOnClickListener(v -> {
            if (categoryClickListener != null) {
                categoryClickListener.onCategoryClick(category.getCategoryName());
            }

            // Mở danh sách sản phẩm của danh mục đã chọn
            Intent intent = new Intent(context, AllProductsActivity.class);
            intent.putExtra("category_name", category.getCategoryName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivCategoryImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivCategoryImage = itemView.findViewById(R.id.ivCategoryImage);
        }
    }

    // 🔥 Interface để xử lý sự kiện click vào danh mục
    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }
}