package com.example.zizo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zizo.R;
import com.example.zizo.adapter.CustomListAdapterStatus;
import com.example.zizo.object.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

public class ProfileFragment extends Fragment {

    private static final int PICKFILE_RESULT_CODE = 0;

    private String mParam1;
    private String mParam2;
    private DatabaseReference myRef;
    private DatabaseReference refStatus;
    private StorageReference mStorageRef;
    private Uri fileUri;
    private String filePath;
    private TextView nickName;
    private Button btn_avatar;
    private TextView sumLikes;
    private ImageView avatar;
    private ListView lv_status;
    private int widthPixels;

    public ProfileFragment(int widthPixels)
    {
        this.widthPixels=widthPixels;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //do nothing
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        nickName=view.findViewById(R.id.nickName);
        btn_avatar=view.findViewById(R.id.btn_avatar);
        avatar=view.findViewById(R.id.avatar);
        sumLikes=view.findViewById(R.id.sumLikes);

        //Lấy thông tin user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        String temp=null;
        if (user!=null)
        {
            temp=user.getEmail();
        }
        assert temp != null;
        final String myEmail=temp.replace('.','-');

        //Lấy nickname
        myRef= FirebaseDatabase.getInstance().getReference("User").child(myEmail);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nickname=dataSnapshot.child("nickName").getValue().toString();
                nickName.setText(nickname);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("test", "Failed to read value.", error.toException());
            }
        });

        //set avatar
        myRef.child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String url = dataSnapshot.getValue(String.class);

                //Set avatar by Url
                Picasso.get().load(url).into(avatar);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        //get and set avatar
        mStorageRef = FirebaseStorage.getInstance().getReference();

        btn_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);

                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        //Set ListView Status
        lv_status=(ListView)view.findViewById(R.id.my_status_list);

        final ArrayList<Status> status_list=new ArrayList<Status>();
        //add my status
        refStatus=FirebaseDatabase.getInstance().getReference("Status");
        refStatus.child(myEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    int sum=0;
                    Stack<Status> stack =new Stack<Status>();
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        String email=myEmail;
                        String content=item.child("content").getValue().toString();
                        String image = item.child("image").getValue().toString();
                        ArrayList<String> likes=new ArrayList<>();
                        for (DataSnapshot like: item.child("likes").getChildren())
                        {
                            likes.add(like.getValue().toString());
                            //Log.e("test", like.getValue().toString());
                        }
                        long time=Long.parseLong(item.child("dateTime").getValue().toString());
                        Status status=new Status(email,content,image,time,likes,null);

                        stack.push(status);
                        sum+=likes.size();
                    }

                    while (!stack.isEmpty())
                    {
                        status_list.add(stack.pop());
                    }

                    //hiển thị tổng số like
                    sumLikes.setText(Integer.toString(sum));

                    //Gán 1 status rỗng
                    String email="";
                    String content="";
                    String image ="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/images%2FtheEnd.png?alt=media&token=de8146f9-b3ef-4f18-8b77-a5d4b481f5a1";
                    long time=0;
                    Status status=new Status(email,content,image,time,null,null);

                    status_list.add(status);
                    lv_status.setAdapter(new CustomListAdapterStatus(getContext(),status_list, widthPixels, myEmail));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {
                    fileUri = data.getData();
                    filePath = fileUri.getPath();
                    Log.e("test", filePath);

                    //Uri file = Uri.fromFile(new File("/document/image:100656"));
                    String filename=fileUri.getLastPathSegment()+".jpg";
                    StorageReference riversRef = mStorageRef.child("avatars").child(filename);

                    riversRef.putFile(fileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!urlTask.isSuccessful());
                                    Uri downloadUrl = urlTask.getResult();
                                    Log.e("test",downloadUrl.toString());

                                    //Set avatar by Url
                                    Picasso.get().load(downloadUrl).into(avatar);

                                    //set avatar Url on databases
                                    myRef.child("avatar").setValue(downloadUrl.toString());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    Log.e("test","Upload Fail");
                                }
                            });
                }

                break;
        }
    }
}
