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
import com.example.zizo.HomeActivity;
import com.example.zizo.MainActivity;
import com.example.zizo.R;
import com.example.zizo.adapter.CustomListAdapterChatBox;
import com.example.zizo.object.ChatBox;
import com.example.zizo.object.MailBox;
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

    private DatabaseReference myRef=null;
    private DatabaseReference mailRef=null;
    private ListView lv_chatbox;
    private String myEmail;
    private ArrayList<ChatBox> chatbox_list=null;
    private ArrayList<String> friends_email=new ArrayList<String>();
    private ProgressBar progressBar;

    private int size=0;
    private boolean started=false;

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
        started=false;

        MainActivity.startProgressBar(progressBar);

        //Lấy thông tin user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
        {
            myEmail=user.getEmail().replace('.','-');
        }

        //Set friends list
        chatbox_list=new ArrayList<ChatBox>();

        //Lấy đường dẫn User
        myRef= FirebaseDatabase.getInstance().getReference().child("User");
        //Lấy đường dẫn Mail Box
        mailRef= FirebaseDatabase.getInstance().getReference().child("MailBox");

        // Lấy danh sách những chat box
        mailRef.child(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                size=(int)dataSnapshot.getChildrenCount();
                if (size==0){
                    MainActivity.finishProgressBar(progressBar);
                }
                //Log.e("test123", String.valueOf(size));
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    final MailBox mail=item.getValue(MailBox.class);
                    //Log.e("test",mail.getId());
                    String friend=findFriend(myEmail,mail.getId());
                    int kind=findKindUser(myEmail,mail.getId());
                    boolean isNew=false;
                    if (kind==1){
                        if (mail.getUser1Viewed()==0)
                        {
                            isNew=true;
                        }
                    }
                    else
                    {
                        if (mail.getUser2Viewed()==0)
                        {
                            isNew=true;
                        }
                    }
                    final boolean finalIsNew = isNew;

                    friends_email.add(friend);

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

                            ChatBox chatBox=new ChatBox(avatar,online,nickName,mail.getFinalMessage(), finalIsNew, mail.getTimeOfFinalMessage());

                            chatbox_list.add(chatBox);

                            //Sau khi dữ liệu đã được thêm vào đầy đủ ta tiến hành setAdapter
                            if (chatbox_list.size()==size)
                            {
                                //Log.e("test123", "OK");
                                //ArrayList<ChatBox> data=sortChatBox(chatbox_list);
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

        final boolean[] listen={false};
        //Tiến trình lắng nghe tin nhắn
        Thread listenMessage=new Thread()
        {
            @Override
            public void run()
            {
                mailRef.child(myEmail).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (listen[0] && HomeActivity.chatting){
                            UpdateChatBox();
                        }else{
                            listen[0]=true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        listenMessage.start();
        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (!started){
            started=true;
            return;
        }

        UpdateChatBox();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
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

    private void UpdateChatBox()
    {
        //Mặc định danh sách chat box không thay đổi
        // Lấy danh sách những chat box
        mailRef.child(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int index=0;
                //Log.e("test123", String.valueOf(size));
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    final MailBox mail=item.getValue(MailBox.class);
                    //Neu co mot chat box moi xuat hien ta sẽ bỏ qua nó
                    if (index>=chatbox_list.size()){
                        break;
                    }
                    //Cap nhat tin nhat cuoi cung
                    chatbox_list.get(index).setMessage(mail.getFinalMessage());
                    //Cap nhat thoi gian
                    chatbox_list.get(index).setTimeOfMessage(mail.getTimeOfFinalMessage());
                    //Cap nhat thong bao tin nhan moi
                    int kind=findKindUser(myEmail,mail.getId());
                    boolean isNew=false;
                    if (kind==1){
                        if (mail.getUser1Viewed()==0)
                        {
                            isNew=true;
                        }
                    }
                    else
                    {
                        if (mail.getUser2Viewed()==0)
                        {
                            isNew=true;
                        }
                    }
                    final boolean finalIsNew = isNew;
                    chatbox_list.get(index).setNew(finalIsNew);

                    index++;
                }

                //ArrayList<ChatBox> data=sortChatBox(chatbox_list);
                lv_chatbox.setAdapter(new CustomListAdapterChatBox(getContext(), chatbox_list));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Test123", "Failed to read value.", error.toException());
            }
        });
    }

//    private void swap(ArrayList<ChatBox> array, int i, int j)
//    {
//        ChatBox temp=new ChatBox(array.get(i).getAvatar(), array.get(i).isOnline(), array.get(i).getNickName(),
//                array.get(i).getMessage(), array.get(i).isNew(), array.get(i).getTimeOfMessage());
//
//        //array[i]=array[j]
//        array.get(i).setAvatar(array.get(j).getAvatar());
//        array.get(i).setOnline(array.get(j).isOnline());
//        array.get(i).setNickName(array.get(j).getNickName());
//        array.get(i).setMessage(array.get(j).getMessage());
//        array.get(i).setNew(array.get(j).isNew());
//        array.get(i).setTimeOfMessage(array.get(j).getTimeOfMessage());
//
//        //array[j]=temp
//        array.get(j).setAvatar(temp.getAvatar());
//        array.get(j).setOnline(temp.isOnline());
//        array.get(j).setNickName(temp.getNickName());
//        array.get(j).setMessage(temp.getMessage());
//        array.get(j).setNew(temp.isNew());
//        array.get(j).setTimeOfMessage(temp.getTimeOfMessage());
//    }

//    private ArrayList<ChatBox> sortChatBox(ArrayList<ChatBox> array)
//    {
//        int n=array.size();
//        for (int i=0;i<n-1;i++)
//        {
//            int min=i;
//            for (int j=i+1;j<n;j++)
//            {
//                if (array.get(j).getTimeOfMessage()<array.get(min).getTimeOfMessage())
//                {
//                    min=j;
//                }
//            }
//
//            if (min!=i)
//            {
//                swap(array, min, i);
//            }
//        }
//
//        return array;
//    }
}
