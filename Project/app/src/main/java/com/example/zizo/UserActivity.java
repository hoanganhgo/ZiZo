package com.example.zizo;

import android.content.Intent;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class UserActivity extends AppCompatActivity {

    private DatabaseReference myRef=null;
    private DatabaseReference otherRef=null;

    TextView nickName;
    Button addFriend;
    Button addFollow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //Lấy thông tin đăng nhập
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        nickName=(TextView)findViewById(R.id.nickName);
        addFriend=(Button)findViewById(R.id.addFriend);
        addFollow=(Button)findViewById(R.id.addFollow);

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
                    addFriend.setText("Kết bạn");
                    removeEmailToList(email, myRef.child("friends"));
                    removeEmailToList(finalMyEmail, otherRef.child("friends"));
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
