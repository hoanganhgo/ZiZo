package com.example.zizo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.zizo.HomeActivity;
import com.example.zizo.MainActivity;
import com.example.zizo.R;
import com.example.zizo.adapter.CustomListAdapterStatus;
import com.example.zizo.object.Comment;
import com.example.zizo.object.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Stack;

public class ProfileFragment extends Fragment {

    private static final int PICKFILE_RESULT_CODE = 0;

    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private String myEmail;
    private TextView nickName;
    private TextView sumLikes;
    private TextView sumComments;
    private TextView sumFriends;
    private TextView sumFollows;
    private ImageView avatar;
    private ListView lv_status;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //do nothing
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        AppCompatActivity activity=(AppCompatActivity)getActivity();
        activity.getSupportActionBar().show();

        nickName=view.findViewById(R.id.nickName);
        Button btn_avatar = view.findViewById(R.id.btn_avatar);
        avatar=view.findViewById(R.id.avatar);
        sumLikes=view.findViewById(R.id.sumLikes);
        sumComments=view.findViewById(R.id.sumComments);
        sumFriends=view.findViewById(R.id.sumFriends);
        sumFollows=view.findViewById(R.id.sumFollows);
        progressBar=view.findViewById(R.id.progressBar_Profile);

        MainActivity.startProgressBar(progressBar);


        //Lấy thông tin user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        String temp=null;
        if (user!=null)
        {
            temp=user.getEmail();
        }
        assert temp != null;
        myEmail=temp.replace('.','-');

        myRef= FirebaseDatabase.getInstance().getReference("User").child(myEmail);

        Thread thread1=new Thread()
        {
          @Override
          public void run()
          {
              //Lấy nickname
              myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      String nickname=dataSnapshot.child("nickName").getValue().toString();
                      nickName.setText(nickname);

                      if (dataSnapshot.child("friends").exists())
                      {
                          long sum_friends=dataSnapshot.child("friends").getChildrenCount();
                          sumFriends.setText(Long.toString(sum_friends));
                      }else{
                          sumFriends.setText("0");
                      }

                      if (dataSnapshot.child("follows").exists())
                      {
                          long sum_follows=dataSnapshot.child("follows").getChildrenCount();
                          sumFollows.setText(Long.toString(sum_follows));
                      }else{
                          sumFollows.setText("0");
                      }
                  }

                  @Override
                  public void onCancelled(DatabaseError error) {
                      // Failed to read value
                      Log.w("test", "Failed to read value.", error.toException());
                  }
              });
          }
        };
        thread1.start();

        Thread thread2=new Thread(){
          @Override
          public void run(){
              //set avatar
              myRef.child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      String url = dataSnapshot.getValue(String.class);

                      //Set avatar by Url
                      float widthAvatar=300*(HomeActivity.widthPixels/720f);
                      Picasso.get().load(url).resize((int)widthAvatar,0).into(avatar);
                  }

                  @Override
                  public void onCancelled(DatabaseError error) {
                      // Failed to read value
                  }
              });
          }
        };
        thread2.start();

        //get and set avatar
        mStorageRef = FirebaseStorage.getInstance().getReference();

        btn_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);

                chooseFile = Intent.createChooser(chooseFile, "Chọn hình ảnh");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        //Set ListView Status
        lv_status=(ListView)view.findViewById(R.id.my_status_list);

        final ArrayList<Status> status_list=new ArrayList<Status>();

        Thread thread3=new Thread(){
          @Override
          public void run(){
              //add my status
              DatabaseReference refStatus = FirebaseDatabase.getInstance().getReference("Status");
              refStatus.child(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      if (dataSnapshot.exists())
                      {
                          int sum_likes=0;
                          int sum_comments=0;
                          Stack<Status> stack =new Stack<Status>();
                          for (DataSnapshot item: dataSnapshot.getChildren()) {
                              String email=myEmail;
                              String content=item.child("content").getValue().toString();
                              String image = HomeActivity.imageDefault;
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

                              stack.push(status);
                              sum_likes+=likes.size();
                              sum_comments+=comments.size();
                          }

                          while (!stack.isEmpty())
                          {
                              status_list.add(stack.pop());
                          }

                          //hiển thị tổng số like và comment
                          sumLikes.setText(Integer.toString(sum_likes));
                          sumComments.setText(Integer.toString(sum_comments));

                          lv_status.setAdapter(new CustomListAdapterStatus(getContext(),status_list, myEmail, true));

                          MainActivity.finishProgressBar(progressBar);
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
        thread3.start();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {
                    Uri fileUri = data.getData();

                    //Uri file = Uri.fromFile(new File("/document/image:100656"));
                    String filename=myEmail+ fileUri.getLastPathSegment()+".jpg";
                    StorageReference riversRef = mStorageRef.child("avatars").child(filename);

                    riversRef.putFile(fileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!urlTask.isSuccessful()){
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    };
                                    Uri downloadUrl = urlTask.getResult();
                                    //Log.e("test",downloadUrl.toString());

                                    //Set avatar by Url
                                    Picasso.get().load(downloadUrl).into(avatar);

                                    //set avatar Url on databases
                                    myRef.child("avatar").setValue(downloadUrl.toString());

                                    //Ghi chú: avatar cũ vẫn còn trên Storage
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    //Log.e("test","Upload Fail");
                                }
                            });
                }

                break;
        }
    }

}
