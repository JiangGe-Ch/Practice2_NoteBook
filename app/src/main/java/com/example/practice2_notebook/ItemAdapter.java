package com.example.practice2_notebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/*
主活动垂直滚动列表适配器
 */
public class ItemAdapter extends ArrayAdapter<ListItem> {
    private int resourceId;


    public ItemAdapter(Context context, int textViewResourceId, List<ListItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId=textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ListItem item= getItem(position);
        View view;

        ViewHolder holder;
        if(convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            holder=new ViewHolder();
            holder.title=(TextView)view.findViewById(R.id.Itemtitle);
            holder.author=(TextView) view.findViewById(R.id.author);
            holder.date=(TextView) view.findViewById(R.id.date);
            holder.content=(TextView) view.findViewById(R.id.content);
            view.setTag(holder);
        }else {
            view=convertView;
            holder=(ViewHolder)view.getTag();
        }
        holder.title.setText(item.getTitle());
        holder.author.setText(item.getAuthor());
        holder.date.setText(item.getDate());
        holder.content.setText(item.getContent());
        return view;
    }

    class ViewHolder{
        TextView title;
        TextView author;
        TextView date;
        TextView content;
    }
}
