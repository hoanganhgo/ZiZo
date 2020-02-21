package com.example.zizo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zizo.adapter.CustomListAdapterUserBasic;
import com.example.zizo.object.UserBasic;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private ArrayList<String> users=new ArrayList<String>();
    private ArrayList<String> friends=new ArrayList<>();
    private ArrayList<String> invitations=new ArrayList<>();
    private ArrayList<String> suggested_users=new ArrayList<>();
    private GridView gv_suggestions;
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
        setContentView(R.layout.activity_search);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Lay my Email
        Intent intent=getIntent();
        final String myEmail=intent.getStringExtra("myEmail");

        final AutoCompleteTextView search=findViewById(R.id.search_friends);
        gv_suggestions=findViewById(R.id.suggestion_list);
        progressBar=(ProgressBar)findViewById(R.id.progressBar_Search);

        MainActivity.startProgressBar(progressBar,30);

        //Lấy danh sách người dùng từ firebase
        final DatabaseReference myRef= FirebaseDatabase.getInstance().getReference().child("User");

        //Lấy danh bạn bè
        myRef.child(myEmail).child("invitation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item:dataSnapshot.getChildren())
                {
                    String email=item.getValue(String.class);
                    Log.e("test",email);
                    invitations.add(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Lấy danh sách gửi lời mời kết bạn
        myRef.child(myEmail).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item:dataSnapshot.getChildren())
                {
                    String email=item.getValue(String.class);
                    Log.e("test",email);
                    friends.add(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Du lieu goi y ket ban
        final ArrayList<UserBasic> data=new ArrayList<>();

        // Read from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    //Log.e("test123", item.getKey());
                    String email=item.getKey();
                    if (email.contentEquals(myEmail)){
                        continue;
                    }
                    boolean isFriend=false;
                    for (String friend:friends)
                    {
                        if (email.contentEquals(friend))
                        {
                            isFriend=true;
                            break;
                        }
                    }

                    boolean isSendInvitation=false;
                    for (String invitation:invitations)
                    {
                        if (email.contentEquals(invitation))
                        {
                            isSendInvitation=true;
                            break;
                        }
                    }

                    if (!isFriend && !isSendInvitation)
                    {
                        suggested_users.add(email);
                    }
                    users.add(convertEmailtoUserName(email));
                }

                final int[] count = {1};
                for (final String email:suggested_users)
                {
                    myRef.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String avatar=dataSnapshot.child("avatar").getValue(String.class);
                            String nickName=dataSnapshot.child("nickName").getValue(String.class);
                            //Log.e("test","suggest: "+nickName);
                            UserBasic userBasic=new UserBasic(email,avatar,nickName);
                            data.add(userBasic);
                            if (count[0]==suggested_users.size())
                            {
                                //Log.e("test","Set Adapter");
                                //Cai dat gridView
                                gv_suggestions.setAdapter(new CustomListAdapterUserBasic(getApplication(),data, myEmail));

                                MainActivity.finishProgressBar(progressBar);
                            }else
                            {
                                count[0]=count[0]+1;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Test123", "Failed to read value.", error.toException());
            }
        });

        ArrayAdapter<String> adapterUsers=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,users);
        search.setAdapter(adapterUsers);

        //Cài đặt sự kiện Click autocomplete TextView
        search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String user=parent.getItemAtPosition(position).toString()+"@zizo.com";
                //String temp=search.get
                //Log.e("test123", user+"  "+position);
                Intent intent=new Intent(view.getContext(), UserActivity.class);
                intent.putExtra("email",user);
                startActivity(intent);
            }
        });

    }

    private String convertEmailtoUserName(String email)
    {
        int tail = email.indexOf('@');
        return email.substring(0,tail);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        users.clear();
        friends.clear();
        invitations.clear();
        suggested_users.clear();
    }
}
