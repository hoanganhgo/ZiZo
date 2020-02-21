package com.example.zizo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

public class UserActivity extends AppCompatActivity {

    private DatabaseReference myRef=null;
    private DatabaseReference otherRef=null;

    private de.hdodenhof.circleimageview.CircleImageView avatar;
    private TextView nickName;
    private Button addFriend;
    private Button addFollow;
    private ListView lv_status;
    private ProgressBar progressBar;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Lấy thông tin đăng nhập
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        avatar=(de.hdodenhof.circleimageview.CircleImageView)findViewById(R.id.avatarUser);
        nickName=(TextView)findViewById(R.id.nickName);
        addFriend=(Button)findViewById(R.id.addFriend);
        addFollow=(Button)findViewById(R.id.addFollow);
        progressBar=(ProgressBar)findViewById(R.id.progressBar_User);

        MainActivity.startProgressBar(progressBar,30);

        //Lấy email người mình truy cập đến
        Intent intent=getIntent();
        String data=intent.getStringExtra("email");
        nickName.setText(data);
        final String email=data.replace('.','-');

        //Kiểm tra lời mời kết bạn
        String myEmail=null;
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        if (user!=null)
        {
            myEmail= Objects.requireNonNull(user.getEmail()).replace('.','-');
            myRef= database.getReference("User").child(myEmail);
            otherRef=database.getReference("User").child(email);
        }
        final String finalMyEmail = myEmail;

        //Set avatar and nick name
        otherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String refAvatar=dataSnapshot.child("avatar").getValue(String.class);
                String name=dataSnapshot.child("nickName").getValue(String.class);

                Picasso.get().load(refAvatar).into(avatar);
                nickName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Kiểm tra tình trạng bạn bè, theo dõi
        if (myRef!=null)
        {
            myRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot item: dataSnapshot.getChildren())
                    {
                        if (email.contentEquals(item.getValue().toString()))
                        {
                            addFriend.setText("Bạn bè");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });

            otherRef.child("invitation").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot item: dataSnapshot.getChildren())
                    {
                        assert finalMyEmail != null;
                        if (finalMyEmail.contentEquals(item.getValue().toString()))
                        {
                            addFriend.setText("Đã gửi lời\nmời kết bạn");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });

            myRef.child("follows").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot item: dataSnapshot.getChildren())
                    {
                        if (email.contentEquals(item.getValue().toString()))
                        {
                            addFollow.setText("Đang\ntheo dõi");
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });
        }

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status=addFriend.getText().toString();
                if (status.contentEquals("Kết bạn"))
                {
                    addFriend.setText("Đã gửi lời\nmời kết bạn");
                    addEmailToList(finalMyEmail, otherRef.child("invitation"));
                }
                else if (status.contentEquals("Đã gửi lời\nmời kết bạn"))
                {
                    addFriend.setText("Kết bạn");
                    removeEmailToList(finalMyEmail, otherRef.child("invitation"));
                }
                else if (status.contentEquals("Bạn bè"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                    builder.setTitle("Hủy kết bạn");
                    builder.setMessage("Bạn thật sự muốn hủy kết bạn");
                    builder.setCancelable(true);

                    builder.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    addFriend.setText("Kết bạn");
                                    removeEmailToList(email, myRef.child("friends"));
                                    removeEmailToList(finalMyEmail, otherRef.child("friends"));
                                }
                            });

                    builder.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        addFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status=addFollow.getText().toString();
                if (status.contentEquals("Theo dõi"))
                {
                    addFollow.setText("Đang\ntheo dõi");
                    addEmailToList(email, myRef.child("follows"));
                }
                else
                {
                    addFollow.setText("Theo dõi");
                    removeEmailToList(email,myRef.child("follows"));
                }
            }
        });

        //Set ListView Status
        lv_status=(ListView)findViewById(R.id.status_list);

        final ArrayList<Status> status_list=new ArrayList<Status>();
        //add my status
        DatabaseReference refStatus = FirebaseDatabase.getInstance().getReference("Status");
        refStatus.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    Stack<Status> stack =new Stack<Status>();
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
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
                    }

                    while (!stack.isEmpty())
                    {
                        status_list.add(stack.pop());
                    }

                    lv_status.setAdapter(new CustomListAdapterStatus(getApplication(),status_list, finalMyEmail));

                    MainActivity.finishProgressBar(progressBar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public static void addEmailToList(final String email,final DatabaseReference myRef)
    {
        final ArrayList<String> listEmail=new ArrayList<String>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren())
                {
                    listEmail.add(item.getValue().toString());
                }

                listEmail.add(email);
                myRef.setValue(listEmail);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

    }

    public static void removeEmailToList(final String email, final DatabaseReference myRef)
    {
        final ArrayList<String> listEmail=new ArrayList<String>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren())
                {
                    listEmail.add(item.getValue().toString());
                }

                listEmail.remove(email);
                myRef.setValue(listEmail);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

}
