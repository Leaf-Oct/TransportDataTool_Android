package com.leafoct.textmessage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<MessageItem> {
    private List<MessageItem> list;
    private int resourceID;
    public MessageAdapter(Context context, int resource, List<MessageItem> objects) {
        super(context, resource, objects);
        list=objects;
        resourceID=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MessageItem item=(MessageItem) getItem(position);
        View v= LayoutInflater.from(getContext()).inflate(resourceID,parent,false);
        ImageView icon=v.findViewById(R.id.icon_message);
        TextView text=v.findViewById(R.id.text_message);
        icon.setImageResource(item.getImage_id());
        text.setText(item.getContent());
        return v;
    }
    public void add(MessageItem item){
        if(item==null){
            return;
        }
        list.add(item);
        notifyDataSetChanged();
    }
}
