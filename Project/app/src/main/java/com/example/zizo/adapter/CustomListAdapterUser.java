package com.example.zizo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.zizo.R;
import com.example.zizo.object.User;
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
            convertView.setTag(holder);
        }
        else {
            holder=(ViewHolder)convertView.getTag();
        }

        UserBasic userBasic=this.listData.get(position);

        //Set avatar by Url
        Picasso.get().load(userBasic.getAvatar()).into(holder.avatar);

        if (userBasic.isOnline())
        {
            holder.online.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.online.setVisibility(View.INVISIBLE);
        }
        holder.nickName.setText(userBasic.getNickName());

        return convertView;
    }

    static class ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView avatar;
        de.hdodenhof.circleimageview.CircleImageView online;
        TextView nickName;
    }
}
