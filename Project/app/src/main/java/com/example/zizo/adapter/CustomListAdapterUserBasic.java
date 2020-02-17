package com.example.zizo.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.zizo.HomeActivity;
import com.example.zizo.R;
import com.example.zizo.UserActivity;
import com.example.zizo.object.UserBasic;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.zizo.UserActivity.addEmailToList;
import static com.example.zizo.UserActivity.removeEmailToList;

public class CustomListAdapterUserBasic extends BaseAdapter {
    private ArrayList<UserBasic> listData=new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;
    private String myEmail;

    public CustomListAdapterUserBasic(Context context, ArrayList<UserBasic> listData, String myEmail) {
        this.context = context;
        this.listData=listData;
        this.layoutInflater=LayoutInflater.from(context);
        this.myEmail=myEmail;
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
        final ViewHolder holder;

        if (convertView==null)
        {
            convertView=layoutInflater.inflate(R.layout.adapter_user,null);

            holder=new ViewHolder();
            holder.avatar=convertView.findViewById(R.id.avatar_user);
            holder.nickName=convertView.findViewById(R.id.nickName_user);
            holder.addFriend=convertView.findViewById(R.id.btn_addFriend);
            holder.goTo_user=convertView.findViewById(R.id.btn_goTo_user);

            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }

        final UserBasic model=this.listData.get(position);

        float widthAvatar=250*(HomeActivity.widthPixels/720f);
        Picasso.get().load(model.getAvatar()).resize((int)widthAvatar,0).into(holder.avatar);
        holder.nickName.setText(model.getNickName());

        final DatabaseReference otherRef= FirebaseDatabase.getInstance().getReference("User").child(model.getEmail());
        //Set invitation
        otherRef.child("invitation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren())
                {
                    if (myEmail.contentEquals(item.getValue().toString()))
                    {
                        holder.addFriend.setText("Đã gửi lời\nmời kết bạn");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        holder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("test","Click addFriend");
                String status=holder.addFriend.getText().toString();
                if (status.contentEquals("Kết bạn"))
                {
                    holder.addFriend.setText("Đã gửi lời\nmời kết bạn");
                    addEmailToList(myEmail, otherRef.child("invitation"));
                }
                else if (status.contentEquals("Đã gửi lời\nmời kết bạn"))
                {
                    holder.addFriend.setText("Kết bạn");
                    removeEmailToList(myEmail, otherRef.child("invitation"));
                }
            }
        });

        holder.goTo_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("test", "Click: "+model.getNickName());
                Intent intent=new Intent(context, UserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("email",model.getEmail());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    static class ViewHolder
    {
        de.hdodenhof.circleimageview.CircleImageView avatar;
        TextView nickName;
        Button addFriend;
        Button goTo_user;
    }
}
