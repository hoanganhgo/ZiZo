package com.example.zizo.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.zizo.CommentActivity;
import com.example.zizo.HomeActivity;
import com.example.zizo.PostActivity;
import com.example.zizo.R;
import com.example.zizo.object.Status;
import com.example.zizo.object.UserBasic;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class CustomListAdapterStatus extends BaseAdapter {
    private ArrayList<Status> listData;
    private LayoutInflater layoutInflater;
    private String myEmail;
    private Context context;

    public CustomListAdapterStatus(Context context, ArrayList<Status> listData, String myEmail)
    {
        this.context=context;
        this.listData=listData;
        this.myEmail=myEmail;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        final CustomListAdapterStatus.ViewHolder holder;
        if (convertView==null)
        {
            convertView=layoutInflater.inflate(R.layout.adapter_status, null);
            holder=new CustomListAdapterStatus.ViewHolder();

            holder.avatar=convertView.findViewById(R.id.avatar_poster);
            holder.nickName=convertView.findViewById(R.id.nickName_poster);
            holder.time=convertView.findViewById(R.id.time_post);
            holder.content=convertView.findViewById(R.id.content_post);
            holder.image=convertView.findViewById(R.id.image_post);
            holder.like=convertView.findViewById(R.id.like_status);
            holder.amountOfLikes=convertView.findViewById(R.id.amount_of_likes);
            holder.comment=convertView.findViewById(R.id.comment_status);
            holder.amountOfComments=convertView.findViewById(R.id.amount_of_comments);

            convertView.setTag(holder);
        }
        else {
            holder=(CustomListAdapterStatus.ViewHolder)convertView.getTag();
        }

        final Status status=this.listData.get(position);
        final int[] sumOfLikes = {0};
        final int[] sumOfComments={0};

        if (status.getComments()!=null)
        {
            sumOfComments[0]=status.getComments().size();
        }
        holder.amountOfComments.setText(Integer.toString(sumOfComments[0]));

        //Kiểm tra xem status đã được like chưa
        if (status.getLikes()!=null)
        {
            Boolean liked=false;
            for (String item : status.getLikes())
            {
                if (myEmail.contentEquals(item))
                {
                    liked=true;
                }
            }

            if (liked)
            {
                holder.like.setTag(Boolean.TRUE);
                holder.like.setBackgroundResource(R.drawable.pink_heart);
            }
            else{
                holder.like.setTag(Boolean.FALSE);
            }
            sumOfLikes[0]=status.getLikes().size();
            holder.amountOfLikes.setText(Integer.toString(sumOfLikes[0]));
        }else{
            holder.like.setTag(Boolean.FALSE);
            holder.amountOfLikes.setText("0");
        }


        //lấy avatar và nickname từ email
        DatabaseReference myRef= FirebaseDatabase.getInstance().getReference("User").child(status.getEmail());
        // Read from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String avatar = dataSnapshot.child("avatar").getValue(String.class);
                String nickName=dataSnapshot.child("nickName").getValue(String.class);
                //Log.d("test123", "Value is: " + avatar);
                //Log.d("test123", "Value is: " + nickName);
                holder.nickName.setText(nickName);
                Picasso.get().load(avatar).into(holder.avatar);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        holder.content.setText(status.getContent());
        Picasso.get().load(status.getImage()).into(holder.image, new Callback() {
            @Override
            public void onSuccess() {
                PostActivity.scaleImage(holder.image, HomeActivity.widthPixels);
            }

            @Override
            public void onError(Exception e) {

            }
        });

        long time=status.getDateTime();
        holder.time.setText(DateFormat.format("dd/MM/yyyy - HH:mm", time));

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("test", "Like"+position);

                if (!((Boolean) holder.like.getTag()))
                {
                    holder.like.setBackgroundResource(R.drawable.pink_heart);
                    holder.like.setTag(Boolean.TRUE);
                    holder.amountOfLikes.setText(Integer.toString(++sumOfLikes[0]));

                    FirebaseDatabase.getInstance().getReference("Status").child(status.getEmail())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot item : dataSnapshot.getChildren())
                                    {
                                        if (Long.parseLong(item.child("dateTime").getValue().toString())==status.getDateTime())
                                        {
                                            Log.e("test", "LIKE: "+item.child("content").getValue());
                                            item.getRef().child("likes").push().setValue(myEmail);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }else{
                    holder.like.setBackgroundResource(R.drawable.heart);
                    holder.like.setTag(Boolean.FALSE);
                    holder.amountOfLikes.setText(Integer.toString(--sumOfLikes[0]));

                    FirebaseDatabase.getInstance().getReference("Status").child(status.getEmail())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot item : dataSnapshot.getChildren())
                                    {
                                        if (Long.parseLong(item.child("dateTime").getValue().toString())==status.getDateTime())
                                        {
                                            Log.e("test", "DISLIKE: "+item.child("content").getValue());
                                            item.getRef().child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot item : dataSnapshot.getChildren())
                                                    {
                                                        if (item.getValue().toString().contentEquals(myEmail))
                                                        {
                                                            item.getRef().removeValue();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent=new Intent(context, CommentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("myEmail",myEmail);
                intent.putExtra("likes",sumOfLikes[0]);
                intent.putExtra("comments",sumOfComments[0]);
                FirebaseDatabase.getInstance().getReference("Status").child(status.getEmail())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot item : dataSnapshot.getChildren())
                                {
                                    if (Long.parseLong(item.child("dateTime").getValue().toString())==status.getDateTime())
                                    {
                                        //Log.e("test",item.getRef().toString());
                                        String ref=item.getRef().toString().substring(34);
                                        ref=ref.replaceAll("%40","@");
                                        intent.putExtra("refStatus", ref);
                                        context.startActivity(intent);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }
        });


        return convertView;
    }

    static class ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView avatar;
        TextView nickName;
        TextView content;
        ImageView image;
        TextView time;
        ImageButton like;
        TextView amountOfLikes;
        ImageButton comment;
        TextView amountOfComments;
    }
}
