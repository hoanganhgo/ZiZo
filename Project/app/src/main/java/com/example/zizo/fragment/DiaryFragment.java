package com.example.zizo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.zizo.HomeActivity;
import com.example.zizo.MainActivity;
import com.example.zizo.PostActivity;
import com.example.zizo.R;
import com.example.zizo.adapter.CustomListAdapterStatus;
import com.example.zizo.object.Comment;
import com.example.zizo.object.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DiaryFragment extends Fragment {

    private ListView lv_status;
    private ProgressBar progressBar;

    private int sizeFriend=0;
    private int countFriend=0;
    private String myEmail=null;
    private DatabaseReference myRef=null;
    private DatabaseReference refStatus=null;

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

        AppCompatActivity activity=(AppCompatActivity)getActivity();
        activity.getSupportActionBar().hide();

        //Set ListView Status
        lv_status=(ListView)view.findViewById(R.id.diary);
        ImageButton btn_post = view.findViewById(R.id.btn_post);
        progressBar=(ProgressBar)view.findViewById(R.id.progressBar_Diary);

        MainActivity.startProgressBar(progressBar);

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

        //Không reset khi vào comment và trả ra
        if (!HomeActivity.resetDiary){
            HomeActivity.resetDiary=true;
            return;
        }

        final ArrayList<Status> status_list=new ArrayList<Status>();
        //Log.e("test","start");

        Thread thread1=new Thread(){
          @Override
          public void run()
          {
              //add my status
              refStatus.child(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                      if (dataSnapshot.exists())
                      {
                          for (DataSnapshot item: dataSnapshot.getChildren()) {
                              String email=myEmail;
                              String content=item.child("content").getValue().toString();
                              String image=HomeActivity.imageDefault;
                              if (item.child("image").exists())
                              {
                                  image = item.child("image").getValue().toString();
                              }

                              ArrayList<String> likes=new ArrayList<>();
                              for (DataSnapshot like: item.child("likes").getChildren())
                              {
                                  likes.add(like.getValue().toString());
                                  //Log.e("test", like.getValue().toString());
                              }

                              ArrayList<Comment> comments=new ArrayList<>();
                              for (DataSnapshot comment : item.child("comments").getChildren())
                              {
                                  comments.add(comment.getValue(Comment.class));
                              }

                              long time=Long.parseLong(item.child("dateTime").getValue().toString());
                              Status status=new Status(email,content,image,time,likes,comments);

                              status_list.add(status);

                              //Log.e("test",image);
                          }
                      }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
          }
        };
        thread1.start();


        Thread thread2=new Thread(){
          @Override
          public void run(){
              myRef.child(myEmail).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(final DataSnapshot dataSnapshot) {
                      if (dataSnapshot.exists())
                      {
                          for (DataSnapshot item: dataSnapshot.getChildren()) {
                              final String friend=item.getValue().toString();
                              //Log.e("test",friend);
                              sizeFriend++;
                              refStatus.child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      if (dataSnapshot.exists())
                                      {
                                          for (DataSnapshot item: dataSnapshot.getChildren()) {
                                              String email=friend;
                                              String content=item.child("content").getValue().toString();
                                              String image= HomeActivity.imageDefault;
                                              if (item.child("image").exists())
                                              {
                                                  image = item.child("image").getValue().toString();
                                              }

                                              ArrayList<String> likes=new ArrayList<>();
                                              for (DataSnapshot like: item.child("likes").getChildren())
                                              {
                                                  likes.add(like.getValue().toString());
                                                  //Log.e("test", like.getValue().toString());
                                              }

                                              ArrayList<Comment> comments=new ArrayList<>();
                                              for (DataSnapshot comment : item.child("comments").getChildren())
                                              {
                                                  comments.add(comment.getValue(Comment.class));
                                              }

                                              long time=Long.parseLong(item.child("dateTime").getValue().toString());
                                              Status status=new Status(email,content,image,time,likes,comments);

                                              status_list.add(status);
                                              //Log.e("test",image);
                                          }
                                      }

                                      countFriend++;
                                      if (countFriend==sizeFriend)
                                      {
                                          sortStatus(status_list);

                                          lv_status.setAdapter(new CustomListAdapterStatus(getContext(),status_list, myEmail, false));
                                          //Log.e("test","finish");
                                          MainActivity.finishProgressBar(progressBar);
                                      }
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                  }
                              });
                          }
                      }else{
                          MainActivity.finishProgressBar(progressBar);
                      }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
          }
        };
        thread2.start();

    }


    private void sortStatus(ArrayList<Status> list)
    {
        int n=list.size();

        for (int i=0;i<n-1;i++)
        {
            int max=i;

            for (int j=i+1;j<n;j++)
            {
                if (list.get(j).getDateTime()>list.get(max).getDateTime())
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
