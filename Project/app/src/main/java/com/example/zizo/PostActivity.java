package com.example.zizo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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

import static com.example.zizo.HomeActivity.heightPixels;
import static com.example.zizo.HomeActivity.widthPixels;

public class PostActivity extends AppCompatActivity {
    private static final int PICKFILE_RESULT_CODE = 0;

    private EditText editContent;
    private ImageButton btnImage;
    private ProgressBar progressBar;

    private String email=null;
    private StorageReference mStorageRef=null;
    private Uri fileUri=null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable drawable= getDrawable(R.drawable.background_title);
        actionBar.setBackgroundDrawable(drawable);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
        {
            email=user.getEmail().replace('.','-');
        }

        editContent=(EditText)findViewById(R.id.edit_content);
        btnImage=(ImageButton)findViewById(R.id.edit_image);
        Button btnPost = (Button) findViewById(R.id.btn_post);
        progressBar=(ProgressBar)findViewById(R.id.progressBar_Post);

        //get and set avatar
        mStorageRef = FirebaseStorage.getInstance().getReference();

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);

                chooseFile = Intent.createChooser(chooseFile, "Chọn hình ảnh");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        final MediaPlayer media=MediaPlayer.create(this,R.raw.post_click);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.startProgressBar(progressBar);
                media.start();

                final String content=editContent.getText().toString();
                final long dateTime=(new Date()).getTime();

                if (fileUri!=null)
                {
                    //Upload image
                    String filename = email + fileUri.getLastPathSegment() + ".jpg";
                    StorageReference riversRef = mStorageRef.child("images").child(filename);

                    riversRef.putFile(fileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!urlTask.isSuccessful()){
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    };
                                    Uri downloadUrl = urlTask.getResult();
                                    //Log.e("test", downloadUrl.toString());

                                    //Set Image by url
                                    Picasso.get().load(downloadUrl).resize((int)widthPixels,0).into(btnImage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });


                                    //set images url
                                    String urlImage = downloadUrl.toString();

                                    // Write a message to the database
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("Status").child(email);
                                    Status status=new Status(email,content,urlImage,dateTime,null,null);
                                    myRef.child(Long.toString(dateTime)).setValue(status);

                                    MainActivity.finishProgressBar(progressBar);

                                    Toast.makeText(PostActivity.this,"Cảm nghĩ của bạn đã được đăng tải",Toast.LENGTH_LONG).show();
                                    finish();
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
                else
                {
                    // Write a message to the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Status").child(email);
                    Status status=new Status(email,content,null,dateTime,null,null);
                    myRef.child(Long.toString(dateTime)).setValue(status);

                    MainActivity.finishProgressBar(progressBar);

                    Toast.makeText(PostActivity.this,"Cảm nghĩ của bạn đã được đăng tải",Toast.LENGTH_LONG).show();
                    finish();
                }
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
                    btnImage.setImageURI(fileUri);
                    Drawable drawing = btnImage.getDrawable();
                    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

                    float width = bitmap.getWidth();
                    float height = bitmap.getHeight();
                    //Log.e("test","scale: "+(height/width));
                    if ((height/width)<1.3f)
                    {
                        scaleImage(btnImage, widthPixels);
                    }else{
                        scaleImageHeight(btnImage,heightPixels);
                    }
                }

                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        HomeActivity.resetDiary=false;
        finish();
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

        imageView.getLayoutParams().width= (int)w;
        imageView.getLayoutParams().height=(int)(heightPixels-350f);
    }
}
