package com.example.zizo;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zizo.object.Comment;
import com.example.zizo.object.MessageModel;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class CommentActivity extends AppCompatActivity {

    private TextView likes;
    private TextView comments;
    private EditText editComment;
    private ImageButton btnPost;

    private String myEmail=null;
    private int sumLikes=0;
    private int sumComments=0;
    private String refStatus=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        likes=(TextView)findViewById(R.id.likes);
        comments=(TextView)findViewById(R.id.comments);
        editComment=(EditText)findViewById(R.id.input_comment);
        btnPost=(ImageButton) findViewById(R.id.btn_comment);

        Intent intent=getIntent();
        myEmail=intent.getStringExtra("myEmail");
        sumLikes=intent.getIntExtra("likes", 0);
        sumComments=intent.getIntExtra("comments",0);
        refStatus=intent.getStringExtra("refStatus");

        likes.setText(Integer.toString(sumLikes));
        comments.setText(Integer.toString(sumComments));

        displayChatMessages();

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference(refStatus);
                long time=(new Date()).getTime();
                Comment comment=new Comment(myEmail,time,editComment.getText().toString());
                ref.child("comments").push().setValue(comment);
                editComment.setText("");
            }
        });
    }

    // hiển thị tin nhắn
    private void displayChatMessages(){
        FirebaseListAdapter<Comment> adapter = new FirebaseListAdapter<Comment>(this, Comment.class,
                R.layout.adapter_comment, FirebaseDatabase.getInstance().getReference(refStatus).child("comments")
        ) {
            @Override
            protected void populateView(View v, Comment model, int position) {
                // Get references to the views of message.xml
                final de.hdodenhof.circleimageview.CircleImageView avatar=v.findViewById(R.id.avatar_commentator);
                final TextView nickName=v.findViewById(R.id.commentator);
                TextView content=v.findViewById(R.id.comment_content);
                TextView time=v.findViewById(R.id.message_time);

                time.setText(DateFormat.format("dd-MM-yyyy - HH:mm",
                        model.getTime()));
                content.setText(model.getContent());

                FirebaseDatabase.getInstance().getReference("User").child(model.getEmail())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                nickName.setText(dataSnapshot.child("nickName").getValue().toString());
                                Picasso.get().load(dataSnapshot.child("avatar").getValue().toString()).into(avatar);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        };

        ListView listOfMessages = (ListView)findViewById(R.id.comments_list);
        listOfMessages.setAdapter(adapter);
    }
}
