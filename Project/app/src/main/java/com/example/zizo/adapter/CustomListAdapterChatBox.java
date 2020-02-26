package com.example.zizo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zizo.HomeActivity;
import com.example.zizo.R;
import com.example.zizo.object.ChatBox;
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
            holder.news_message=convertView.findViewById(R.id.new_message);
            holder.timeOffinalMessage=convertView.findViewById(R.id.time_of_message);
            convertView.setTag(holder);
        }
        else {
            holder=(CustomListAdapterChatBox.ViewHolder)convertView.getTag();
        }

        ChatBox chatBox=this.listData.get(position);

        //Set avatar by Url
        float widthAvatar=250*(HomeActivity.widthPixels/720f);
        Picasso.get().load(chatBox.getAvatar()).resize((int)widthAvatar,0).into(holder.avatar);

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

        if (chatBox.isNew()){
            holder.news_message.setVisibility(View.VISIBLE);
        }else{
            holder.news_message.setVisibility(View.INVISIBLE);
        }

        int hours=0;
        hours=(int)(chatBox.getTimeOfMessage()/3600000);
        if (hours<24 && hours>0){
            holder.timeOffinalMessage.setText(hours+" giờ trước");
        }else if (hours==0){
            holder.timeOffinalMessage.setVisibility(View.INVISIBLE);
        }

        int day=hours/24;
        if (day>0){
            holder.timeOffinalMessage.setText(day+" ngày trước");
        }

        return convertView;
    }

    static class ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView avatar;
        de.hdodenhof.circleimageview.CircleImageView online;
        TextView nickName;
        TextView message;
        ImageView news_message;
        TextView timeOffinalMessage;
    }
}
