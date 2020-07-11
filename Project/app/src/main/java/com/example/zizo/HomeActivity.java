package com.example.zizo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.zizo.fragment.ChatBoxFragment;
import com.example.zizo.fragment.DiaryFragment;
import com.example.zizo.fragment.FriendFragment;
import com.example.zizo.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    public static float widthPixels=0f;
    public static float heightPixels=0f;
    public static String imageDefault="https://firebasestorage.googleapis.com/v0/b/zizo-44e08.appspot.com/o/default%2Fempty.png?alt=media&token=d0ef7d28-0e49-415b-901a-a044621a9cd7";
    public static boolean resetDiary=true;
    public static boolean chatting=true;

    BottomNavigationView bottomNavigation;
    private FirebaseAuth auth;
    private ChatBoxFragment chatBoxFragment;
    private FriendFragment friendFragment;
    private DiaryFragment diaryFragment;
    private ProfileFragment profileFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                //Logout
                auth.signOut();

                //Auto login set false
                SharedPreferences sharedPreferences=getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("AutoLogin",false);
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                Toast.makeText(HomeActivity.this, "Đăng xuất thành công",
                        Toast.LENGTH_SHORT).show();
                break;

            default:break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_home);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        Drawable drawable= getDrawable(R.drawable.background_title);
        actionBar.setBackgroundDrawable(drawable);

        final MenuView.ItemView item_message=(MenuView.ItemView)findViewById(R.id.navigation_chat);

        //get my Email
        auth=FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        assert user != null;
        final String myEmail=user.getEmail().replace('.','-');

        Thread threadGetSizeScreen=new Thread(){
            @Override
            public void run()
            {
                //Lấy kích thước màn hình
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                widthPixels = displayMetrics.widthPixels;
                heightPixels=displayMetrics.heightPixels;
            }
        };
        threadGetSizeScreen.start();

        final MediaPlayer media=MediaPlayer.create(this,R.raw.message_tone);
        final boolean[] tone={false};
        final Drawable icon=getDrawable(R.drawable.new_chat);

        //Kiểm tra hộp thư xem có tin nhắn hay không?
        final DatabaseReference mailRef=FirebaseDatabase.getInstance().getReference("MailBox").child(myEmail);
        mailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item:dataSnapshot.getChildren())
                {
                    int kind=findKindUser(myEmail,item.getKey());
                    if (kind==1 && item.child("user1Viewed").getValue(Integer.class)==0)
                    {
                        media.start();
                        item_message.setIcon(icon);
                    } else if (kind==2 && item.child("user2Viewed").getValue(Integer.class)==0)
                    {
                        media.start();
                        item_message.setIcon(icon);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Tiến trình lắng nghe tin nhắn
        Thread listenMessage=new Thread()
        {
            @Override
            public void run()
            {
                mailRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (tone[0] && chatting){
                            media.start();
                            item_message.setIcon(icon);
                        }else{
                            tone[0]=true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        listenMessage.start();

        //Khởi tạo các fragment
        chatBoxFragment=new ChatBoxFragment();
        friendFragment=new FriendFragment();
        diaryFragment=new DiaryFragment();
        profileFragment=new ProfileFragment();

        bottomNavigation=findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        bottomNavigation.setSelectedItemId(R.id.navigation_diary);
        openFragment(diaryFragment);

        //Tiến trình cập nhật thời gian thực
        Thread threadUpdateRealTime = new Thread() {
            @Override
            public void run() {
                try {
                    DatabaseReference myRef= FirebaseDatabase.getInstance().getReference("User").child(myEmail).child("realTime");
                    while(auth.getCurrentUser()!=null) {
                        Long realTime=(new Date()).getTime();
                        myRef.setValue(realTime);

                        sleep(59000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        threadUpdateRealTime.start();
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_chat:
                            openFragment(chatBoxFragment);
                            return true;
                        case R.id.navigation_friends:
                            openFragment(friendFragment);
                            return true;
                        case R.id.navigation_diary:
                            openFragment(diaryFragment);
                            return true;
                        case R.id.navigation_home:
                            openFragment(profileFragment);
                            return true;
                    }
                    return false;
                }
            };

    int pressExit=0;
    @Override
    public void onBackPressed()
    {
        if (pressExit>=1){
            //Close
            this.finishAffinity();
        }
        else{
            pressExit++;
            Toast.makeText(HomeActivity.this, "Bấm lần nữa để thoát",
                    Toast.LENGTH_SHORT).show();
            Thread thread=new Thread(){
              @Override
              public void run(){
                  try {
                      sleep(5000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  pressExit=0;
              }
            };
            thread.start();
        }
    }

    //return 1 if me. Return 2 if friend
    private int findKindUser(String myEmail, String idChatBox)
    {
        int index=idChatBox.indexOf('+');
        String s=idChatBox.substring(0,index);

        if (s.contentEquals(myEmail))
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }

}
