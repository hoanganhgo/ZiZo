package com.example.zizo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zizo.object.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private EditText nickName;
    private RadioButton male;
    private RadioButton female;
    private EditText dateOfBirth;
    private Button btn_signUp;
    final String TAG="signUp123";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        email=(EditText)findViewById(R.id.inputEmail);
        password=(EditText)findViewById(R.id.inputPassWord);
        nickName=(EditText)findViewById(R.id.inputNick);
        male=(RadioButton)findViewById(R.id.male);
        female=(RadioButton)findViewById(R.id.female);
        dateOfBirth=(EditText)findViewById(R.id.dateOfBirth);
        btn_signUp=(Button)findViewById(R.id.signUp_Click);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(email.getText().toString(), password.getText().toString());

                String sex="";
                if (male.isChecked())
                {
                    sex="Nam";
                }
                if (female.isChecked())
                {
                    sex="Nữ";
                }
                String emailUser = email.getText().toString().replace('.','-');
                upLoadUser(emailUser,nickName.getText().toString(),dateOfBirth.getText().toString(),sex);
            }
        });
    }

    //Ghi nhận thông tin user vào Authetication
    private void register(String email,String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(SignUpActivity.this, "Tạo tài khoản thành công",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent=new Intent(SignUpActivity.this, HomeActivity.class);
                            startActivity(intent);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Tạo tài khoản thất bại",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }

    //Ghi thông tin user vào database
    private void upLoadUser(String email, String nickName, String dateOfBirth, String sex)
    {
        long realTime=(new Date()).getTime();

        String avatar=null;
        if (sex.contentEquals("Nam"))
        {
            avatar="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/avatars%2Fman.png?alt=media&token=6506dc54-0ff9-4a1e-93b1-a3f6b81dd27b";
        }else {
            avatar="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/avatars%2Fwoman.png?alt=media&token=7d5b9698-4854-4979-8e56-dff8d06ffe5d";
        }

        User user=new User(email,nickName,avatar,dateOfBirth,sex,realTime,null,null, null);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("User").child(email);

        myRef.setValue(user);
    }
}
