package com.example.zizo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class InvitationActivity extends AppCompatActivity {

    GridView gv_invitation;

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
        setContentView(R.layout.activity_invitation);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable drawable= getDrawable(R.drawable.background_title);
        actionBar.setBackgroundDrawable(drawable);

        //Lấy thông tin đăng nhập
        FirebaseUser users= FirebaseAuth.getInstance().getCurrentUser();
        String temp=null;
        if (users!=null)
        {
            temp=users.getEmail().replace('.','-');
        }
        final String myEmail=temp;

        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("User").child(myEmail).child("invitation");

        FirebaseListAdapter<String> myAdapter=new FirebaseListAdapter<String>(this, String.class, R.layout.adapter_user,myRef) {
            @Override
            protected void populateView(View v, final String email, int position) {
                Button btn_goTo=v.findViewById(R.id.btn_goTo_user);
                final de.hdodenhof.circleimageview.CircleImageView avatar=v.findViewById(R.id.avatar_user);
                final TextView nickName=v.findViewById(R.id.nickName_user);
                Button btn_accept=v.findViewById(R.id.btn_addFriend);

                btn_accept.setText("Chấp nhận");

                FirebaseDatabase.getInstance().getReference("User").child(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
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

                btn_accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserActivity.addEmailToList(email,database.getReference("User").child(myEmail).child("friends"));
                        UserActivity.addEmailToList(myEmail,database.getReference("User").child(email).child("friends"));
                        UserActivity.removeEmailToList(email,database.getReference("User").child(myEmail).child("invitation"));
                    }
                });

                btn_goTo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(InvitationActivity.this, UserActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("email",email);
                        startActivity(intent);
                    }
                });
            }
        };

        gv_invitation=(GridView) findViewById(R.id.list_invitation);
        gv_invitation.setAdapter(myAdapter);
    }
}
