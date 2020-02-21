package com.example.zizo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.zizo.fragment.ChatBoxFragment;
import com.example.zizo.fragment.DiaryFragment;
import com.example.zizo.fragment.FriendFragment;
import com.example.zizo.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    public static float widthPixels=0f;
    public static float heightPixels=0f;
    public static String imageDefault="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/default%2Fempty.png?alt=media&token=ecc7ef9c-98ee-4324-bf6b-e564d25ae7a6";
    public static boolean resetDiary=true;

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
                    auth=FirebaseAuth.getInstance();
                    FirebaseUser user=auth.getCurrentUser();
                    assert user != null;
                    String myEmail=user.getEmail().replace('.','-');
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
            //Logout
            auth.signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            //Toast.makeText(HomeActivity.this, "Đăng xuất thành công",
            //        Toast.LENGTH_SHORT).show();
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

}
