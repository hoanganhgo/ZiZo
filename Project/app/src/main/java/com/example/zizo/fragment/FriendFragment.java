package com.example.zizo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.zizo.ChatActivity;
import com.example.zizo.InvitationActivity;
import com.example.zizo.MainActivity;
import com.example.zizo.R;
import com.example.zizo.SearchActivity;
import com.example.zizo.UserActivity;
import com.example.zizo.adapter.CustomListAdapterUser;
import com.example.zizo.object.UserBasic;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class FriendFragment extends Fragment{

    private ListView lv_friends;
    private String myEmail;
    private ArrayList<UserBasic> friends_list;
    private ProgressBar progressBar;
    private ImageView new_invitation;
    private DatabaseReference myRef=null;

    private int size=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //do nothing
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.activity_friends, container, false);

        AppCompatActivity activity=(AppCompatActivity)getActivity();
        activity.getSupportActionBar().hide();

        ImageButton btn_search = (ImageButton) view.findViewById(R.id.button_search);
        ImageButton btn_invitation = (ImageButton) view.findViewById(R.id.button_invitation);
        lv_friends=(ListView)view.findViewById(R.id.friends_list);
        progressBar=view.findViewById(R.id.progressBar_friend);
        new_invitation=view.findViewById(R.id.new_invitation);

        MainActivity.startProgressBar(progressBar);

        //Search button
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), SearchActivity.class);
                intent.putExtra("myEmail",myEmail);
                startActivity(intent);
            }
        });

        //Invitation button
        btn_invitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), InvitationActivity.class);
                intent.putExtra("myEmail",myEmail);
                startActivity(intent);
            }
        });

        //Lấy thông tin user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
        {
            myEmail=user.getEmail().replace('.','-');
        }

        //Set friends list
        friends_list=new ArrayList<UserBasic>();

        //Lấy danh sách người dùng từ firebase
        myRef= FirebaseDatabase.getInstance().getReference().child("User");
        // Read from the database
        myRef.child(myEmail).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                size=(int)dataSnapshot.getChildrenCount();
                if (size==0){
                    MainActivity.finishProgressBar(progressBar);
                }
                //Log.e("test123", String.valueOf(size));
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    final String friend=item.getValue().toString();
                    //Log.e("test123", friend);
                    Thread thread=new Thread(){
                      @Override
                      public void run(){
                          myRef.child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(DataSnapshot dataSnapshot) {
                                  String avatar=dataSnapshot.child("avatar").getValue().toString();
                                  //Log.e("test123",avatar);
                                  String realTime=dataSnapshot.child("realTime").getValue().toString();
                                  //Log.e("test123",realTime);
                                  String nickName=dataSnapshot.child("nickName").getValue().toString();
                                  //Log.e("test123",nickName);

                                  long time=Long.parseLong(realTime);

                                  UserBasic userBasic=new UserBasic(friend, avatar, time, nickName);

                                  friends_list.add(userBasic);

                                  //Sau khi dữ liệu đã được thêm vào đầy đủ ta tiến hành setAdapter
                                  if (friends_list.size()==size)
                                  {
                                      //Log.e("test123", "OK");
                                      lv_friends.setAdapter(new CustomListAdapterUser(view.getContext(),friends_list));
                                  }

                                  MainActivity.finishProgressBar(progressBar);
                                  //Log.e("test123", friend.getAvatar()+"  "+friend.getRealTime()+"  "+friend.getNickName());
                              }

                              @Override
                              public void onCancelled(DatabaseError error) {
                                  // Failed to read value
                                  Log.w("Test123", "Failed to read value.", error.toException());
                              }
                          });
                      }
                    };
                    thread.start();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Test123", "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        myRef.child(myEmail).child("invitation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0){
                    new_invitation.setVisibility(View.VISIBLE);
                }else{
                    new_invitation.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
