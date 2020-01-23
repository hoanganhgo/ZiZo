package com.example.zizo;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zizo.object.MessageModel;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference myRef=null;
    private boolean existChatBox=false;
    private int kindUser=0;
    private String idChatBox="";
    private String urlAvatar=null;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_chat);

        //Lấy thông tin đăng nhập
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        final String myEmail=user.getEmail().replace('.','-');

        //Lấy email người nhận
        Intent intent=getIntent();
        final String friend=intent.getStringExtra("email");

        //Kiểm tra sự tồn tại của chatbox
        final String id1=myEmail+"+"+friend;
        final String id2=friend+"+"+myEmail;

        // Read from the database
        myRef=FirebaseDatabase.getInstance().getReference("User");
        myRef.child(myEmail).child("chatBox").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren())
                {
                    //Log.d("test123", "Value is: " + item.getValue());
                    if (id1.contentEquals(item.getValue().toString()))
                    {
                        idChatBox=id1;
                        kindUser=findKindUser(myEmail,idChatBox);
                        existChatBox=true;

                        loadAvatarOfFriend(myEmail, idChatBox);
                        //displayChatMessages();
                        return;
                    }
                }

                for (DataSnapshot item : dataSnapshot.getChildren())
                {
                    //Log.d("test123", "Value is: " + item.getValue());
                    if (id2.contentEquals(item.getValue().toString()))
                    {
                        idChatBox=id2;
                        kindUser=findKindUser(myEmail,idChatBox);
                        existChatBox=true;

                        loadAvatarOfFriend(myEmail, idChatBox);
                        //displayChatMessages();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        // gửi tin nhắn
        Button btnSend = (Button)findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input_Message);   //Nội dung tin nhắn

                //Nếu chưa tồn tại ChatBox
                if (!existChatBox)
                {
                    idChatBox=myEmail+"+"+friend;
                    kindUser=findKindUser(myEmail,idChatBox);

                    myRef.child(myEmail).child("chatBox").push().setValue(idChatBox);
                    assert friend != null;
                    myRef.child(friend).child("chatBox").push().setValue(idChatBox);
                    existChatBox=true;

                    loadAvatarOfFriend(myEmail, idChatBox);
                    //displayChatMessages();
                }

                //Lấy thời điểm hiện tại
                long time=(new Date()).getTime();

                //Tạo đối tượng tin nhắn
                final MessageModel newMess = new MessageModel(kindUser, time, input.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("ChatBox").child(idChatBox)
                        .push()
                        .setValue(newMess);

//                FirebaseDatabase.getInstance().getReference().child("Joining").child(userName)
//                        .child(nameCircle).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        JoinModel joinModel = dataSnapshot.getValue(JoinModel.class);
//                        joinModel.setLastMessage(newMess);
//                        FirebaseDatabase.getInstance().getReference().child("Joining").child(userName)
//                                .child(nameCircle).setValue(joinModel);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

                // Clear the input
                input.setText("");
            }
        });
    }

    // hiển thị tin nhắn
    private void displayChatMessages(){
        FirebaseListAdapter<MessageModel> adapter = new FirebaseListAdapter<MessageModel>(this, MessageModel.class,
                R.layout.adapter_message, FirebaseDatabase.getInstance().getReference("ChatBox").child(idChatBox)
        ) {
            @Override
            protected void populateView(View v, MessageModel model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_content);
                de.hdodenhof.circleimageview.CircleImageView avatarSender = v.findViewById(R.id.avatar_sender);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                TextView textFriend=(TextView)v.findViewById(R.id.message_content_friend);
                TextView timeFriend=(TextView)v.findViewById(R.id.message_time_friend);

                //Log.e("test123", model.getContent());

                // Set their text
                messageText.setText(model.getContent());
                //messageUser.setText(Integer.toString(model.getSender()));

                if (model.getSender()==kindUser)
                {
                    textFriend.setVisibility(View.VISIBLE);
                    timeFriend.setVisibility(View.VISIBLE);

                    textFriend.setText(model.getContent());
                    // Format the date before showing it
                    timeFriend.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            model.getTime()));

                    avatarSender.setVisibility(View.INVISIBLE);
                    messageText.setVisibility(View.INVISIBLE);
                    messageTime.setVisibility(View.INVISIBLE);
                }
                else
                {
                    avatarSender.setVisibility(View.VISIBLE);
                    messageText.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.VISIBLE);

                    //Set avatar by Url
                    Picasso.get().load(urlAvatar).into(avatarSender);
                    messageText.setText(model.getContent());
                    // Format the date before showing it
                    messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            model.getTime()));
                    textFriend.setVisibility(View.INVISIBLE);
                    timeFriend.setVisibility(View.INVISIBLE);
                }
            }
        };

        ListView listOfMessages = (ListView)findViewById(R.id.messages_list);
        listOfMessages.setAdapter(adapter);
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

    private void loadAvatarOfFriend(String myEmail, String idChatBox)
    {
        String email=null;
        int index=idChatBox.indexOf('+');
        int len=idChatBox.length();
        String s1=idChatBox.substring(0,index);
        String s2=idChatBox.substring(index+1,len);

        if (s1.contentEquals(myEmail))
        {
            //Log.e("test123",s2);
            email=s2;
        }
        else
        {
            //Log.e("test123",s1);
            email=s1;
        }

        myRef.child(email).child("avatar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                //Log.e("test123",value);
                urlAvatar=value;

                //Sau khi có được URL avatar ta sẽ load màn hình chat lên
                displayChatMessages();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }
}