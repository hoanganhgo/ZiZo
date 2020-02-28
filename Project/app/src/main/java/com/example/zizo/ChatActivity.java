package com.example.zizo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zizo.object.MailBox;
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
    private DatabaseReference mailRef=null;
    private boolean existChatBox=false;
    private int kindUser=0;
    private String idChatBox="";
    private String urlAvatar=null;
    private long message_time=0;

    private RelativeLayout.LayoutParams paramsZERO=new RelativeLayout.LayoutParams(android.app.ActionBar.LayoutParams.WRAP_CONTENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(android.app.ActionBar.LayoutParams.WRAP_CONTENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT);
    private de.hdodenhof.circleimageview.CircleImageView avatar;
    private TextView greeting;
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
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_chat);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable drawable= getDrawable(R.drawable.background_title);
        actionBar.setBackgroundDrawable(drawable);

        avatar=findViewById(R.id.avatar_new_chat);
        greeting=(TextView)findViewById(R.id.name_chat);
        progressBar=(ProgressBar)findViewById(R.id.progressBar_Chat);

        paramsZERO.setMargins(0,0,0,0);
        params.setMargins(0,10,0,10);

        MainActivity.startProgressBar(progressBar,35);
        HomeActivity.chatting=false;

        //Lấy thông tin đăng nhập
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        final String myEmail=user.getEmail().replace('.','-');

        //Lấy email người nhận
        Intent intent=getIntent();
        final String friend=intent.getStringExtra("email");

        //Đường dẫn User
        myRef=FirebaseDatabase.getInstance().getReference("User");
        //Đường dẫn đến Mail Box
        mailRef=FirebaseDatabase.getInstance().getReference("MailBox");


        Thread thread1=new Thread(){
          @Override
          public void run()
          {
              //Set title
              myRef.child(friend).child("nickName").addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      String nickName=dataSnapshot.getValue(String.class);
                      setTitle(nickName);
                      greeting.setText("Hãy gửi lời chào đến "+nickName+" nào!");
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
          }
        };
        thread1.start();

        //Kiểm tra sự tồn tại của chatbox
        Thread thread2=new Thread(){
          @Override
          public void run(){
              mailRef.child(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                      String id1=myEmail+"+"+friend;
                      String id2=friend+"+"+myEmail;

                      for (DataSnapshot item : dataSnapshot.getChildren())
                      {
                          MailBox mail=item.getValue(MailBox.class);
                          //Log.e("test",mail.getId());
                          if (id1.contentEquals(mail.getId()))
                          {
                              idChatBox=id1;
                              kindUser=findKindUser(myEmail,idChatBox);
                              existChatBox=true;

                              loadAvatarOfFriend(myEmail, idChatBox);
                              setViewed(mailRef,kindUser,myEmail,friend);
                              return;
                          }
                      }

                      for (DataSnapshot item : dataSnapshot.getChildren())
                      {
                          MailBox mail=item.getValue(MailBox.class);
                          if (id2.contentEquals(mail.getId()))
                          {
                              idChatBox=id2;
                              kindUser=findKindUser(myEmail,idChatBox);
                              existChatBox=true;

                              loadAvatarOfFriend(myEmail, idChatBox);
                              setViewed(mailRef,kindUser,myEmail,friend);
                              return;
                          }
                      }

                      //Chưa tồn tại chat box
                      avatar.setVisibility(View.VISIBLE);
                      greeting.setVisibility(View.VISIBLE);
                      MainActivity.finishProgressBar(progressBar);

                      Thread thread=new Thread(){
                          @Override
                          public void run(){
                              myRef.child(friend).child("avatar").addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(DataSnapshot dataSnapshot) {
                                      String url = dataSnapshot.getValue(String.class);
                                      //Log.e("test123",value);
                                      float widthAvatar=200*(HomeActivity.widthPixels/720f);
                                      Picasso.get().load(url).resize((int)widthAvatar,0).into(avatar);
                                  }

                                  @Override
                                  public void onCancelled(DatabaseError error) {
                                      // Failed to read value
                                  }
                              });
                          }
                      };
                      thread.start();
                  }

                  @Override
                  public void onCancelled(DatabaseError error) {
                      // Failed to read value
                  }
              });
          }
        };
        thread2.start();

        // gửi tin nhắn
        ImageButton btnSend = (ImageButton) findViewById(R.id.btnSend);
        final MediaPlayer media=MediaPlayer.create(this,R.raw.send_click);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input_Message);   //Nội dung tin nhắn
                media.start();
                if (input.getText().toString().isEmpty())
                {
                    return;
                }

                //Nếu chưa tồn tại ChatBox
                if (!existChatBox)
                {
                    idChatBox=myEmail+"+"+friend;
                    kindUser=findKindUser(myEmail,idChatBox);

                    //Tạo đối tượng Mail Box
                    MailBox mailBox=new MailBox(idChatBox,"",0,1,1);

                    mailRef.child(myEmail).child(idChatBox).setValue(mailBox);
                    mailRef.child(friend).child(idChatBox).setValue(mailBox);
                    existChatBox=true;

                    //Tắt lời chào
                    avatar.setVisibility(View.INVISIBLE);
                    greeting.setVisibility(View.INVISIBLE);

                    loadAvatarOfFriend(myEmail, idChatBox);
                }

                //Lấy thời điểm hiện tại
                long time=(new Date()).getTime();

                //Tạo đối tượng tin nhắn
                final MessageModel newMess = new MessageModel(kindUser, time, input.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("ChatBox").child(idChatBox)
                        .push()
                        .setValue(newMess);

                //Lưu giữ tin nhắn cuối cùng
                String messageTheEnd=input.getText().toString();
                if (messageTheEnd.length()>30){
                    messageTheEnd = messageTheEnd.substring(0,30)+"...";
                }
                mailRef.child(myEmail).child(idChatBox).child("finalMessage").setValue(messageTheEnd);
                mailRef.child(myEmail).child(idChatBox).child("timeOfFinalMessage").setValue(time);

                mailRef.child(friend).child(idChatBox).child("finalMessage").setValue(messageTheEnd);
                mailRef.child(friend).child(idChatBox).child("timeOfFinalMessage").setValue(time);

                if (kindUser==1){
                    mailRef.child(myEmail).child(idChatBox).child("user1Viewed").setValue(1); //đã xem
                    mailRef.child(myEmail).child(idChatBox).child("user2Viewed").setValue(0);  //chưa xem

                    mailRef.child(friend).child(idChatBox).child("user1Viewed").setValue(1); //đã xem
                    mailRef.child(friend).child(idChatBox).child("user2Viewed").setValue(0);  //chưa xem
                }else{
                    mailRef.child(myEmail).child(idChatBox).child("user1Viewed").setValue(0);
                    mailRef.child(myEmail).child(idChatBox).child("user2Viewed").setValue(1);

                    mailRef.child(friend).child(idChatBox).child("user1Viewed").setValue(0);
                    mailRef.child(friend).child(idChatBox).child("user2Viewed").setValue(1);
                }

                // Clear the input
                input.setText("");
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        HomeActivity.chatting=true;
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
                //Log.e("test123", model.getContent());

                // Set their text
                messageText.setText(model.getContent());
                //messageUser.setText(Integer.toString(model.getSender()));

                if (model.getSender()==kindUser)
                {
                    //Hiển thị tin nhắn của mình
                    avatarSender.setVisibility(View.GONE);
                    textFriend.setVisibility(View.GONE);

                    messageText.setText(model.getContent());
                    messageText.setVisibility(View.VISIBLE);
                }
                else
                {
                    //Hiển thị tin nhắn của bạn chat
                    messageText.setVisibility(View.GONE);

                    avatarSender.setVisibility(View.VISIBLE);
                    textFriend.setVisibility(View.VISIBLE);

                    //Set avatar by Url
                    float widthAvatar=150*(HomeActivity.widthPixels/720f);
                    Picasso.get().load(urlAvatar).resize((int)widthAvatar,0).into(avatarSender);
                    textFriend.setText(model.getContent());
                }

                if (message_time - model.getTime() > 1800000)
                {
                    messageTime.setVisibility(View.VISIBLE);

                    // Format the date before showing it
                    messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",
                            model.getTime()));
                }else{
                    messageTime.setVisibility(View.GONE);
                }
                message_time=model.getTime();
            }
        };

        ListView listOfMessages = (ListView)findViewById(R.id.messages_list);
        listOfMessages.setAdapter(adapter);

        MainActivity.finishProgressBar(progressBar);
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

        final String finalEmail = email;
        Thread thread=new Thread(){
          @Override
          public void run(){
              myRef.child(finalEmail).child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
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
        };
        thread.start();
    }

    private void setViewed(DatabaseReference mailRef, int kindUser, String myEmail, String friend)
    {
        if (kindUser==1){

            mailRef.child(myEmail).child(idChatBox).child("user1Viewed").setValue(1); //đã xem
            mailRef.child(friend).child(idChatBox).child("user1Viewed").setValue(1); //đã xem
        }else if (kindUser==2){
            mailRef.child(myEmail).child(idChatBox).child("user2Viewed").setValue(1);
            mailRef.child(friend).child(idChatBox).child("user2Viewed").setValue(1);
        }
    }
}
