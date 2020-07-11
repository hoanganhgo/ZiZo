package com.example.zizo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    //final String TAG="signUp123";
    Button btn_signUp;
    EditText username;
    EditText password;
    CheckBox saveUser;
    Button btn_signIn;

    SharedPreferences sharedPreferences;
    ProgressBar progressBar;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.my_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId())
//        {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//            case R.id.menu1:
//                //code xử lý khi bấm menu1
//                Log.e("test","menu1");
//                break;
//            case R.id.menu2:
//                //code xử lý khi bấm menu2
//                Log.e("test","menu2");
//                break;
//            case R.id.menu3:
//                //code xử lý khi bấm menu3
//                Log.e("test","menu3");
//                break;
//
//            default:break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btn_signUp=(Button)findViewById(R.id.signUp);
        username=(EditText)findViewById(R.id.Email);
        password=(EditText)findViewById(R.id.PassWord);
        saveUser=(CheckBox)findViewById(R.id.saveUserName);
        btn_signIn=(Button)findViewById(R.id.signIn_Click);
        progressBar=(ProgressBar)findViewById(R.id.progressBar_Login);


        //Tải lại tài khoản và mật khẩu đã lưu
        Thread thread=new Thread(){
            @Override
            public void run(){
                sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String savedUsername=sharedPreferences.getString("Username",null);
                String savedPass=sharedPreferences.getString("PassWord",null);

                if (savedUsername!=null && savedPass!=null)
                {
                    username.setText(savedUsername);
                    password.setText(savedPass);
                    saveUser.setChecked(true);
                }
            }
        };
        thread.start();

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLogin(username.getText().toString(),password.getText().toString()))
                {
                    startProgressBar(progressBar);
                    String email=username.getText().toString()+"@zizo.com";
                    signIn(email, password.getText().toString());
                }
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signIn(final String email, final String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công",
                                    Toast.LENGTH_SHORT).show();

                            //Lưu tài khoản mật khẩu
                            Thread thread=new Thread(){
                              @Override
                              public void run()
                              {
                                  if (saveUser.isChecked()) {
                                      //sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                      SharedPreferences.Editor editor = sharedPreferences.edit();

                                      editor.putString("Username", username.getText().toString());
                                      editor.putString("PassWord", password);
                                      editor.putBoolean("AutoLogin", true);
                                      editor.apply();
                                  }
                                  else
                                  {
                                      //sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                      SharedPreferences.Editor editor = sharedPreferences.edit();

                                      editor.clear();
                                      editor.apply();
                                  }
                              }
                            };

                            thread.start();

                            //Chuyển màn hình
                            Intent intent=new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            finishProgressBar(progressBar);
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại",
                                    Toast.LENGTH_SHORT).show();
                           // updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private boolean checkLogin(String username, String password)
    {
        if (username.isEmpty())
        {
            this.username.requestFocus();
            return false;
        }

        if (password.isEmpty())
        {
            this.password.requestFocus();
            return false;
        }
        return true;
    }

    public static void startProgressBar(final ProgressBar progressBar)
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    public static void finishProgressBar(final ProgressBar progressBar)
    {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed()
    {
        this.finishAffinity();
    }
}
