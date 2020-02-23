package com.example.zizo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedUsername=sharedPreferences.getString("Username",null);
        String savedPass=sharedPreferences.getString("PassWord",null);
        boolean autoLogin=sharedPreferences.getBoolean("AutoLogin", false);

        if (savedUsername!=null && savedPass!=null && autoLogin)
        {
            String email=savedUsername+"@zizo.com";
            mAuth.signInWithEmailAndPassword(email, savedPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Intent intent=new Intent(SplashActivity.this, HomeActivity.class);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                                startActivity(intent);
                            }

                            // ...
                        }
                    });
        }else
        {
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
        }

    }
}
