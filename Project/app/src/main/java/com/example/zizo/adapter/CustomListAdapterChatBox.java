package com.example.zizo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.zizo.R;
import com.example.zizo.object.ChatBox;
import com.example.zizo.object.UserBasic;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomListAdapterChatBox extends BaseAdapter {
    private ArrayList<ChatBox> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapterChatBox(Context context, ArrayList<ChatBox> listData)
    {
        this.context=context;
        this.listData=listData;
        this.layoutInflater=LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return this.listData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CustomListAdapterChatBox.ViewHolder holder;
        if (convertView==null)
        {
            convertView=layoutInflater.inflate(R.layout.adapter_chatbox, null);
            holder=new CustomListAdapterChatBox.ViewHolder();
            holder.avatar=convertView.findViewById(R.id.avatar_friend);
            holder.online=convertView.findViewById(R.id.online);
            holder.nickName=convertView.findViewById(R.id.nickName_friend);
            holder.message=convertView.findViewById(R.id.message_theEnd);
            convertView.setTag(holder);
        }
        else {
            holder=(CustomListAdapterChatBox.ViewHolder)convertView.getTag();
        }

        ChatBox chatBox=this.listData.get(position);

        //Set avatar by Url
        Picasso.get().load(chatBox.getAvatar()).into(holder.avatar);

        if (chatBox.isOnline())
        {
            holder.online.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.online.setVisibility(View.INVISIBLE);
        }
        holder.nickName.setText(chatBox.getNickName());
        holder.message.setText(chatBox.getMessage());

        return convertView;
    }

    static class ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView avatar;
        de.hdodenhof.circleimageview.CircleImageView online;
        TextView nickName;
        TextView message;
    }
}
