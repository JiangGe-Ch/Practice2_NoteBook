package com.example.practice2_notebook;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/*
内容中图片列表的适配器
 */
public class ContentImgAdapter extends RecyclerView.Adapter<ContentImgAdapter.ViewHolder> {
    final String TAG="debug";
    
    private int resourceId;

    private List<ContentImgItem> imgList;

    private OnItemClickListener mOnItemClickListener=null;

    /*
    用于优化滚动加载速度
     */
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(View view){
            super(view);
            imageView=(ImageView) view.findViewById(R.id.content_img);
        }
    }

    public ContentImgAdapter(List<ContentImgItem> contentImgItemList){
        imgList=contentImgItemList;
    }

    /*
    项点击监听器接口
     */
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    /*
    项点击监听器设置接口
     */
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener=mOnItemClickListener;
        Log.d(TAG, "setOnItemClickListener: ");
    }

    /*
    布局加载
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.diary_content_img, parent, false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    /*
    布局内容初始化
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        ContentImgItem contentImgItem=imgList.get(position);
//        holder.imageView.setImageResource(contentImgItem.getImgId());
        holder.imageView.setImageBitmap(contentImgItem.getImgBitmap());
        /*
        项点击监听器回调方法
         */
        if(this.mOnItemClickListener!=null){
            Log.d(TAG, "onBindViewHolder: ififif");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.imageView, position);
                    Log.d(TAG, "onBindViewHolder: onClick: ");
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return imgList.size();
    }
}
