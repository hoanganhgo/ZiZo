package com.example.zizo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InvitationActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

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

        FirebaseListAdapter<String> myAdapter=new FirebaseListAdapter<String>(this, String.class, android.R.layout.simple_list_item_1,myRef) {
            @Override
            protected void populateView(View v, String model, int position) {
                TextView text = (TextView) v.findViewById(android.R.id.text1);
                text.setText(model.replace('-','.'));
            }
        };

        listView=(ListView)findViewById(R.id.list_invitation);
        listView.setAdapter(myAdapter);

        //Cài đặt sự kiện click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String user=parent.getItemAtPosition(position).toString();

                AlertDialog.Builder alert=new AlertDialog.Builder(view.getContext());
                alert.setMessage("Chấp nhận lời mời kết bạn");
                alert.setTitle(user.replace('-','.'));
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("test", "Chap nhan "+user);
                        UserActivity.addEmailToList(user,database.getReference("User").child(myEmail).child("friends"));
                        UserActivity.addEmailToList(myEmail,database.getReference("User").child(user).child("friends"));
                        UserActivity.removeEmailToList(user,database.getReference("User").child(myEmail).child("invitation"));
                    }
                });
                alert.setCancelable(true);
                alert.create().show();
            }
        });
    }
}
