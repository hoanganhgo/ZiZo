package com.example.zizo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zizo.object.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class PostActivity extends AppCompatActivity {
    private static final int PICKFILE_RESULT_CODE = 0;

    private EditText editContent;
    private ImageButton btnImage;
    private Button btnPost;

    private String email=null;
    private String urlImage=null;
    private Uri fileUri=null;
    private String filePath=null;
    private StorageReference mStorageRef=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
        {
            email=user.getEmail().replace('.','-');
        }

        editContent=(EditText)findViewById(R.id.edit_content);
        btnImage=(ImageButton)findViewById(R.id.edit_image);
        btnPost=(Button)findViewById(R.id.btn_post);

        //get and set avatar
        mStorageRef = FirebaseStorage.getInstance().getReference();

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);

                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        final MediaPlayer media=MediaPlayer.create(this,R.raw.post_click);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("test",email);
               // Log.e("test",urlImage);
                //Log.e("test",editContent.getText().toString());
                media.start();

                String content=editContent.getText().toString();
                long dateTime=(new Date()).getTime();

                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Status").child(email);

                if (urlImage==null)
                {
                    urlImage="https://firebasestorage.googleapis.com/v0/b/zizo-9fdb5.appspot.com/o/default%2Fempty.png?alt=media&token=ecc7ef9c-98ee-4324-bf6b-e564d25ae7a6";
                }
                Status status=new Status(email,content,urlImage,dateTime,null,null);
                myRef.push().setValue(status);
                Toast.makeText(PostActivity.this,"Cảm nghĩ của bạn đã được đăng tải",Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {
                    fileUri = data.getData();
                    filePath = fileUri.getPath();
                   // Log.e("test", filePath);

                    //Uri file = Uri.fromFile(new File("/document/image:100656"));
                    String filename = email + fileUri.getLastPathSegment() + ".jpg";
                    StorageReference riversRef = mStorageRef.child("images").child(filename);

                    riversRef.putFile(fileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!urlTask.isSuccessful()) ;
                                    Uri downloadUrl = urlTask.getResult();
                                    //Log.e("test", downloadUrl.toString());

                                    //Set Image by url
                                    Picasso.get().load(downloadUrl).into(btnImage, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            DisplayMetrics displayMetrics = new DisplayMetrics();
                                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                            float widthPixels = displayMetrics.widthPixels;
                                            float heightPixels=displayMetrics.heightPixels;
                                            // Get the ImageView and its bitmap
                                            Drawable drawing = btnImage.getDrawable();
                                            Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

                                            // Get current dimensions
                                            float width = bitmap.getWidth();
                                            float height = bitmap.getHeight();
                                            Log.e("test","scale: "+(height/width));
                                            if ((height/width)<1.35f)
                                            {
                                                scaleImage(btnImage, widthPixels);
                                            }else{
                                                scaleImageHeight(btnImage,heightPixels);
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });

                                    //set images url
                                    urlImage = downloadUrl.toString();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    Log.e("test", "Upload Fail");
                                }
                            });
                }

                break;
        }
    }

    public static void scaleImage(ImageView imageView, float widthPixels)
    {
        // Get the ImageView and its bitmap
        Drawable drawing = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //Log.e("test", "w:"+width+"  h:"+height);

        float scale= widthPixels/width;
        float h=height*scale;
        //Log.e("test", "h:"+h);
        int hImage=(int)h;

        imageView.getLayoutParams().width=ViewGroup.LayoutParams.MATCH_PARENT;
        imageView.getLayoutParams().height=hImage;
    }

    private void scaleImageHeight(ImageView imageView, float heightPixels)
    {
        // Get the ImageView and its bitmap
        Drawable drawing = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //Log.e("test", "w:"+width+"  h:"+height);

        float scale= (heightPixels-350f)/height;
        float w=width*scale;
        //Log.e("test", "h:"+h);
        int wImage=(int)w;

        imageView.getLayoutParams().width=wImage;
        imageView.getLayoutParams().height=(int)(heightPixels-350f);
    }
}
