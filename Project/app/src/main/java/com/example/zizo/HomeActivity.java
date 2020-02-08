package com.example.zizo;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    public static int widthPixels=0;

    BottomNavigationView bottomNavigation;
    private FirebaseAuth auth;
    private ChatBoxFragment chatBoxFragment;
    private FriendFragment friendFragment;
    private DiaryFragment diaryFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_home);

        auth=FirebaseAuth.getInstance();

        //Lấy kích thước màn hình
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthPixels = displayMetrics.widthPixels;

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
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
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

        thread.start();
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

    @Override
    public void onBackPressed()
    {
        auth.signOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        Toast.makeText(HomeActivity.this, "Đăng xuất thành công",
                Toast.LENGTH_SHORT).show();
    }

}
