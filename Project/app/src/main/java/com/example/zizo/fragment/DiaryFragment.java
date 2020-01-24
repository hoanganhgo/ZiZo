package com.example.zizo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zizo.PostActivity;
import com.example.zizo.R;
import com.example.zizo.adapter.CustomListAdapterStatus;
import com.example.zizo.object.Status;

import java.util.ArrayList;
import java.util.Date;

public class DiaryFragment extends Fragment {

    private ListView lv_status;
    private Button btn_post;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //do nothing
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.activity_diary, container, false);

        //Set ListView Status
        lv_status=(ListView)view.findViewById(R.id.diary);

        ArrayList<Status> status_list=new ArrayList<Status>();
        Log.e("test","start");

        String email="hoang@zizo-com";
        String content="Happy new year!";
        String image="https://www.gannett-cdn.com/presto/2019/12/18/PGRE/4fa79f9a-8d51-4736-9035-c62452c34e4e-new_years.jpg?width=540&height=&fit=bounds&auto=webp";
        long time=(new Date()).getTime();
        Status status=new Status(email,content,image,time,null,null);

        status_list.add(status);
        status_list.add(status);
        status_list.add(status);

        lv_status.setAdapter(new CustomListAdapterStatus(view.getContext(),status_list));
        Log.e("test","finish");


        //set post button
        btn_post=view.findViewById(R.id.btn_post);
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), PostActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
