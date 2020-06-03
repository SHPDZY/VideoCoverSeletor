package com.zy.videocoverselector.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zy.videocoverselector.R;

import java.util.ArrayList;

public class SelectCoverAdapter extends RecyclerView.Adapter<SelectCoverAdapter.ViewHolder> {
    private final Context mContext;
    @NonNull
    private ArrayList<Bitmap> data = new ArrayList<Bitmap>();

    public SelectCoverAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int height = mContext.getResources().getDimensionPixelOffset(R.dimen.item_thumb_height);
        int width = mContext.getResources().getDimensionPixelOffset(R.dimen.item_thumb_width);
        ImageView view = new ImageView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).load(data.get(position)).into(holder.thumb);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(int position, Bitmap b) {
        data.add(b);
        notifyItemInserted(position);
    }

    public void clearAllBitmap() {
        data.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ImageView thumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = (ImageView) itemView;
        }
    }

}
