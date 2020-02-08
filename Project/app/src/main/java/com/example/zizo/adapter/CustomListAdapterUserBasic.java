package com.example.zizo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.zizo.R;
import com.example.zizo.object.UserBasic;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomListAdapterUserBasic extends BaseAdapter {
    private ArrayList<UserBasic> listData=new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapterUserBasic(Context context, ArrayList<UserBasic> listData) {
        this.context = context;
        this.listData=listData;
        this.layoutInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
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
            convertView=layoutInflater.inflate(R.layout.adapter_user,null);

            holder=new ViewHolder();
            holder.avatar=convertView.findViewById(R.id.avatar_user);
            holder.nickName=convertView.findViewById(R.id.nickName_user);
            holder.addFriend=convertView.findViewById(R.id.btn_addFriend);

            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }

        UserBasic model=this.listData.get(position);
        Picasso.get().load(model.getAvatar()).into(holder.avatar);
        holder.nickName.setText(model.getNickName());
        holder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }

    static class ViewHolder
    {
        de.hdodenhof.circleimageview.CircleImageView avatar;
        TextView nickName;
        Button addFriend;
    }
}
