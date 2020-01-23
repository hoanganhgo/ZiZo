package com.example.zizo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zizo.ChatActivity;
import com.example.zizo.InvitationActivity;
import com.example.zizo.R;
import com.example.zizo.SearchActivity;
import com.example.zizo.adapter.CustomListAdapterUser;
import com.example.zizo.adapter.UserBasic;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageButton btn_search;
    private ImageButton btn_invitation;
    private ListView lv_friends;
    private String myEmail;
    private ArrayList<UserBasic> friends_list;
    private ArrayList<String> friends_email=new ArrayList<String>();

    private int size=0;

    public ChatBoxFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendFragment newInstance(String param1, String param2) {
        FriendFragment fragment = new FriendFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.activity_chatbox, container, false);

        btn_search=(ImageButton)view.findViewById(R.id.button_search);
        btn_invitation=(ImageButton)view.findViewById(R.id.button_invitation);
        lv_friends=(ListView)view.findViewById(R.id.friends_list);

        //Search button
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        //Invitation button
        btn_invitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), InvitationActivity.class);
                startActivity(intent);
            }
        });

        //get My Email
        //Lấy thông tin user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
        {
            myEmail=user.getEmail().replace('.','-');
        }

        //Set friends list
        friends_list=new ArrayList<UserBasic>();

        //Lấy danh sách người dùng từ firebase
        final DatabaseReference myRef= FirebaseDatabase.getInstance().getReference().child("User");
        // Read from the database
        myRef.child(myEmail).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                size=(int)dataSnapshot.getChildrenCount();
                //Log.e("test123", String.valueOf(size));
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    String friend=item.getValue().toString();
                    friends_email.add(friend);
                    //Log.e("test123", friend);
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

                            UserBasic userBasic=new UserBasic(avatar,online,nickName);

                            friends_list.add(userBasic);

                            //Sau khi dữ liệu đã được thêm vào đầy đủ ta tiến hành setAdapter
                            if (friends_list.size()==size)
                            {
                                //Log.e("test123", "OK");
                                lv_friends.setAdapter(new CustomListAdapterUser(view.getContext(),friends_list));
                            }

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

        lv_friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra("email",friends_email.get(position));
                startActivity(intent);
            }
        });

        return view;
    }
}
