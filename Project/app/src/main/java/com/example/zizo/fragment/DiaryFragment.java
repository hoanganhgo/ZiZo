package com.example.zizo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zizo.PostActivity;
import com.example.zizo.R;
import com.example.zizo.adapter.CustomListAdapterStatus;
import com.example.zizo.object.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class DiaryFragment extends Fragment {

    private ListView lv_status;
    private Button btn_post;

    private int sizeFriend=0;
    private int countFriend=0;
    private String myEmail=null;
    private DatabaseReference myRef=null;
    private DatabaseReference refStatus=null;
    private int widthPixels;

    public DiaryFragment(int widthPixels)
    {
        this.widthPixels=widthPixels;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //do nothing
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.activity_diary, container, false);

        //Set ListView Status
        lv_status=(ListView)view.findViewById(R.id.diary);
        btn_post=view.findViewById(R.id.btn_post);

        //Lấy thông tin user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
        {
            myEmail=user.getEmail().replace('.','-');
        }

        myRef= FirebaseDatabase.getInstance().getReference().child("User");
        refStatus=FirebaseDatabase.getInstance().getReference("Status");

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), PostActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        final ArrayList<Status> status_list=new ArrayList<Status>();
        Log.e("test","start");

        //add my status
        refStatus.child(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        String email=myEmail;
                        String content=item.child("content").getValue().toString();
                        String image = item.child("image").getValue().toString();
                        long time=Long.parseLong(item.child("dateTime").getValue().toString());
                        Status status=new Status(email,content,image,time,null,null);

                        status_list.add(status);
                        Log.e("test",image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child(myEmail).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    final String friend=item.getValue().toString();
                    Log.e("test",friend);
                    sizeFriend++;
                    refStatus.child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             if (dataSnapshot.exists())
                             {
                                for (DataSnapshot item: dataSnapshot.getChildren()) {
                                    String email=friend;
                                    String content=item.child("content").getValue().toString();
                                    String image = item.child("image").getValue().toString();
                                    long time=Long.parseLong(item.child("dateTime").getValue().toString());
                                    Status status=new Status(email,content,image,time,null,null);

                                    status_list.add(status);
                                    Log.e("test",image);
                                }
                             }

                             countFriend++;
                             if (countFriend==sizeFriend)
                             {
                                 sortStatus(status_list);

                                 String email="";
                                 String content="";
                                 String image ="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/images%2FtheEnd.png?alt=media&token=de8146f9-b3ef-4f18-8b77-a5d4b481f5a1";
                                 long time=0;
                                 Status status=new Status(email,content,image,time,null,null);

                                 status_list.add(status);
                                 lv_status.setAdapter(new CustomListAdapterStatus(getContext(),status_list, widthPixels, myEmail));
                                 Log.e("test","finish");
                             }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sortStatus(ArrayList<Status> list)
    {
        int n=list.size();

        for (int i=0;i<n-1;i++)
        {
            int max=i;

            for (int j=i+1;j<n;j++)
            {
                if (list.get(j).getDateTime()>list.get(i).getDateTime())
                {
                    max=j;
                }
            }

            if (max!=i)
            {
                Status temp=list.get(max);
                list.set(max, list.get(i));
                list.set(i, temp);
            }
        }
    }
}