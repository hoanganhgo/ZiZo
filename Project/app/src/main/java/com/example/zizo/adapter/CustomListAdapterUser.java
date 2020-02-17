package com.example.zizo.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.zizo.ChatActivity;
import com.example.zizo.HomeActivity;
import com.example.zizo.R;
import com.example.zizo.UserActivity;
import com.example.zizo.object.UserBasic;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomListAdapterUser extends BaseAdapter {

    private ArrayList<UserBasic> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapterUser(Context context, ArrayList<UserBasic> listData)
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

        ViewHolder holder;
        if (convertView==null)
        {
            convertView=layoutInflater.inflate(R.layout.adapter_friend, null);
            holder=new ViewHolder();
            holder.avatar=convertView.findViewById(R.id.avatar_friend);
            holder.online=convertView.findViewById(R.id.online);
            holder.nickName=convertView.findViewById(R.id.nickName_friend);
            holder.btn_goTo=convertView.findViewById(R.id.btn_goTo);
            holder.btn_message=convertView.findViewById(R.id.btn_message);
            convertView.setTag(holder);
        }
        else {
            holder=(ViewHolder)convertView.getTag();
        }

        final UserBasic userBasic=this.listData.get(position);

        //Set avatar by Url
        float widthAvatar=200*(HomeActivity.widthPixels/720f);
        Picasso.get().load(userBasic.getAvatar()).resize((int)widthAvatar,0).into(holder.avatar);

        if (userBasic.isOnline())
        {
            holder.online.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.online.setVisibility(View.INVISIBLE);
        }
        holder.nickName.setText(userBasic.getNickName());

        holder.btn_goTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, UserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //Log.e("test",userBasic.getEmail());
                intent.putExtra("email",userBasic.getEmail());
                context.startActivity(intent);

            }
        });

        holder.btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ChatActivity.class);
                //Log.e("test",userBasic.getEmail());
                intent.putExtra("email",userBasic.getEmail());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView avatar;
        de.hdodenhof.circleimageview.CircleImageView online;
        TextView nickName;
        Button btn_goTo;
        ImageButton btn_message;
    }
}
