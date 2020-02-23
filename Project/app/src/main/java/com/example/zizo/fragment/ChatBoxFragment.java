package com.example.zizo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.zizo.ChatActivity;
import com.example.zizo.MainActivity;
import com.example.zizo.R;
import com.example.zizo.adapter.CustomListAdapterChatBox;
import com.example.zizo.object.ChatBox;
import com.example.zizo.object.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class ChatBoxFragment extends Fragment {

    private ListView lv_chatbox;
    private String myEmail;
    private ArrayList<ChatBox> chatbox_list=null;
    private ArrayList<String> friends_email=new ArrayList<String>();
    private ProgressBar progressBar;

    private int size=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //do nothing
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.activity_chatbox, container, false);

        AppCompatActivity activity=(AppCompatActivity)getActivity();
        activity.getSupportActionBar().hide();

        lv_chatbox=(ListView)view.findViewById(R.id.friends_list);
        progressBar=view.findViewById(R.id.progressBar_ChatBox);

        MainActivity.startProgressBar(progressBar,50);

        //Lấy thông tin user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
        {
            myEmail=user.getEmail().replace('.','-');
        }

        //Set friends list
        chatbox_list=new ArrayList<ChatBox>();

        //Lấy danh sách người dùng từ firebase
        final DatabaseReference myRef= FirebaseDatabase.getInstance().getReference().child("User");

        // Read from the database
        myRef.child(myEmail).child("chatBox").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                size=(int)dataSnapshot.getChildrenCount();
                //Log.e("test123", String.valueOf(size));
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    String idChatBox=item.getValue().toString();
                    String friend=findFriend(myEmail,idChatBox);
                    friends_email.add(friend);
                    final ArrayList<String> messagesTheEnd=new ArrayList<String>();
                    final int[] count = {0};

                    //Lấy tin nhắn cuối cùng
                    FirebaseDatabase.getInstance().getReference("ChatBox").child(idChatBox)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    long size=dataSnapshot.getChildrenCount();
                                    long count=0;
                                    for (DataSnapshot item : dataSnapshot.getChildren())
                                    {
                                        count++;
                                        if (count==size)
                                        {
                                            MessageModel message=item.getValue(MessageModel.class);
                                            //Log.e("test123",message.getContent());
                                            String messageContent=message.getContent();
                                            if (messageContent.length()>30)
                                            {
                                                messageContent = messageContent.substring(0,30)+"...";
                                            }
                                            messagesTheEnd.add(messageContent);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    //Lấy thông tin bạn bè
                    myRef.child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String avatar=dataSnapshot.child("avatar").getValue().toString();
                            //Log.e("test123",avatar);
                            String realTime=dataSnapshot.child("realTime").getValue().toString();
                            //Log.e("test123",realTime);
                            String nickName=dataSnapshot.child("nickName").getValue().toString();
                            //Log.e("test123",nickName);

                            //Xác định xem bạn bè còn online hay không
                            boolean online=false;
                            long current=(new Date()).getTime();
                            if (current-Long.parseLong(realTime)<60000)
                            {
                                online=true;
                            }

                            ChatBox chatBox=new ChatBox(avatar,online,nickName,messagesTheEnd.get(count[0]++));

                            chatbox_list.add(chatBox);

                            messagesTheEnd.clear();

                            //Sau khi dữ liệu đã được thêm vào đầy đủ ta tiến hành setAdapter
                            if (chatbox_list.size()==size)
                            {
                                //Log.e("test123", "OK");
                                lv_chatbox.setAdapter(new CustomListAdapterChatBox(view.getContext(), chatbox_list));
                            }

                            MainActivity.finishProgressBar(progressBar);
                            //Log.e("test123", friend.getAvatar()+"  "+friend.getRealTime()+"  "+friend.getNickName());
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("Test123", "Failed to read value.", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Test123", "Failed to read value.", error.toException());
            }
        });

        lv_chatbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra("email",friends_email.get(position));
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        friends_email.clear();
    }

    private String findFriend(String myEmail, String idChatBox)
    {
        int index=idChatBox.indexOf('+');
        int len=idChatBox.length();
        String s1=idChatBox.substring(0,index);
        String s2=idChatBox.substring(index+1,len);

        if (s1.contentEquals(myEmail))
        {
            return s2;
        }
        else
        {
            return s1;
        }
    }
}
