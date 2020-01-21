package com.example.zizo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    final ArrayList<String> users=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final AutoCompleteTextView search=findViewById(R.id.search_friends);

        //Lấy danh sách người dùng từ firebase
        DatabaseReference myRef= FirebaseDatabase.getInstance().getReference().child("User");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    // Log.e("test123", item.getKey());
                    users.add(item.getKey().replace('-','.'));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Test123", "Failed to read value.", error.toException());
            }
        });

        ArrayAdapter<String> adapterUsers=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,users);
        search.setAdapter(adapterUsers);

        //Cài đặt sự kiện Click autocomplete TextView
        search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String user=parent.getItemAtPosition(position).toString();
                //String temp=search.get
                //Log.e("test123", user+"  "+position);
                Intent intent=new Intent(view.getContext(), UserActivity.class);
                intent.putExtra("email",user);
                startActivity(intent);
            }
        });
    }
}
