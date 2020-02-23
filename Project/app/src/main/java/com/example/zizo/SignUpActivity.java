package com.example.zizo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
    private EditText username;
    private EditText password;
    private EditText rePassword;
    private EditText nickName;
    private RadioButton male;
    private RadioButton female;
    private EditText dateOfBirth;
    private TextView notify;
    private ProgressBar progressBar;
    //final String TAG="signUp123";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable drawable= getDrawable(R.drawable.background_title);
        actionBar.setBackgroundDrawable(drawable);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        username=(EditText)findViewById(R.id.inputEmail);
        password=(EditText)findViewById(R.id.inputPassWord);
        rePassword=(EditText)findViewById(R.id.rePassWord);
        nickName=(EditText)findViewById(R.id.inputNick);
        male=(RadioButton)findViewById(R.id.male);
        female=(RadioButton)findViewById(R.id.female);
        dateOfBirth=(EditText)findViewById(R.id.dateOfBirth);
        notify=(TextView)findViewById(R.id.notify);
        Button btn_signUp = (Button) findViewById(R.id.signUp_Click);
        progressBar=(ProgressBar)findViewById(R.id.progressBar_register);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSignUp(username.getText().toString(),password.getText().toString(),rePassword.getText().toString(),
                        nickName.getText().toString(),male.isChecked() | female.isChecked(),dateOfBirth.getText().toString()))
                {
                    MainActivity.startProgressBar(progressBar,80);

                    String email=username.getText().toString()+"@zizo.com";
                    register(email, password.getText().toString());
                }
            }
        });
    }

    //Ghi nhận thông tin user vào Authetication
    private void register(final String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");

                            //Upload User
                            String sex="";
                            if (male.isChecked())
                            {
                                sex="Nam";
                            }
                            if (female.isChecked())
                            {
                                sex="Nữ";
                            }
                            String emailUser = email.replace('.','-');
                            upLoadUser(emailUser,nickName.getText().toString(),dateOfBirth.getText().toString(),sex);

                            //Thong bao tao tai khoan thanh cong
                            Toast.makeText(SignUpActivity.this, "Tạo tài khoản thành công",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent=new Intent(SignUpActivity.this, HomeActivity.class);
                            startActivity(intent);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            MainActivity.finishProgressBar(progressBar);
                            Toast.makeText(SignUpActivity.this, "Tạo tài khoản thất bại",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }

    //Ghi thông tin user vào Firebase database
    private void upLoadUser(String email, String nickName, String dateOfBirth, String sex)
    {
        long realTime=(new Date()).getTime();

        String avatar=null;
        if (sex.contentEquals("Nam"))
        {
            avatar="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/default%2Fman.png?alt=media&token=a8cfc07a-a4f4-4e80-b302-4d1fa6ddbb8c";
        }else {
            avatar="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/default%2Fwoman.png?alt=media&token=bfe3bb42-0faf-4d52-9e69-81439f52cc0d";
        }

        User user=new User(email,nickName,avatar,dateOfBirth,sex,realTime,null,null, null);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("User").child(email);

        myRef.setValue(user);
    }

    private boolean checkSignUp(String username, String password, String rePassword, String nickName, boolean sex, String dateOfBirth)
    {
        if (username.isEmpty())
        {
            this.username.requestFocus();
            this.notify.setText("Bạn chưa nhập tên tài khoản");
            return false;
        }
        if (password.isEmpty())
        {
            this.password.requestFocus();
            this.notify.setText("Bạn chưa nhập mật khẩu");
            return false;
        }
        if (rePassword.isEmpty())
        {
            this.rePassword.requestFocus();
            this.notify.setText("Bạn chưa nhập lại mật khẩu");
            return false;
        }
        if (nickName.isEmpty())
        {
            this.nickName.requestFocus();
            this.notify.setText("Bạn chưa nhập nick name");
            return false;
        }
        if (dateOfBirth.isEmpty())
        {
            this.dateOfBirth.requestFocus();
            this.notify.setText("Bạn chưa nhập ngày sinh");
            return false;
        }
        if (!sex)
        {
            this.notify.setText("Bạn chưa chọn giới tính");
            return false;
        }

        if (username.indexOf(' ')>-1 || username.indexOf('\n')>-1)
        {
            this.username.requestFocus();
            this.username.setText("");
            this.notify.setText("Bạn không được sử dụng ký tự khoảng trắng");
            this.notify.setTextSize(15f);
            return false;
        }

        if (username.indexOf(' ')>-1 || username.indexOf('\n')>-1)
        {
            this.password.requestFocus();
            this.password.setText("");
            this.notify.setText("Bạn không được sử dụng ký tự khoảng trắng");
            this.notify.setTextSize(15f);
            return false;
        }

        if (password.length()<6)
        {
            this.password.requestFocus();
            this.password.setText("");
            this.rePassword.setText("");
            this.notify.setText("Mật khẩu của bạn phải có ít nhất 6 ký tự");
            this.notify.setTextSize(15f);
            return false;
        }

        for (int i=0;i<username.length();i++)
        {
            if (!isCharater(username.charAt(i)))
            {
                this.username.requestFocus();
                this.username.setText("");
                this.notify.setText("Bạn không được sử dụng các ký tự đặc biệt");
                this.notify.setTextSize(15f);
                return false;
            }
            if (username.charAt(i)>='A' && username.charAt(i)<='Z')
            {
                this.username.requestFocus();
                this.username.setText("");
                this.notify.setText("Bạn không được sử dụng các ký tự in hoa cho tên tài khoản");
                this.notify.setTextSize(12f);
                return false;
            }
        }

        for (int i=0;i<password.length();i++)
        {
            if (!isCharater(password.charAt(i)))
            {
                this.password.requestFocus();
                this.password.setText("");
                this.rePassword.setText("");
                this.notify.setText("Bạn không được sử dụng các ký tự đặc biệt");
                this.notify.setTextSize(15f);
                return false;
            }
        }

        if (!password.contentEquals(rePassword))
        {
            this.password.requestFocus();
            this.password.setText("");
            this.rePassword.setText("");
            this.notify.setText("Mật khẩu không trùng khớp");
            return false;
        }

        return true;
    }

    private boolean isCharater(char c)
    {
        if (c>='a' && c<='z')
        {
            return true;
        }
        if (c>='0' && c<='9')
        {
            return true;
        }
        if (c>='A' && c<='Z')
        {
            return true;
        }
        return false;
    }
}
