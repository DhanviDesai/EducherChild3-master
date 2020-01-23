package com.example.educher_child;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Database.PrefManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;
import static com.example.educher_child.AppConfiguration.SUGGESTION;

public class Suggestion extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<SuggestModel> models;
    private DatabaseReference reference;
   // private IOSDialog iosDialog;
    private AppLockDatabase db;
    private String parent_key,childiD;
    private int count;
    private PrefManager prefManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        count = 0;
        //change status bar color
//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary));


//        iosDialog = new IOSDialog.Builder(this)
//                .setCancelable(false)
//                .setSpinnerClockwise(false)
//                .setMessageContentGravity(Gravity.END)
//                .build();
//        iosDialog.show();

        prefManager = new PrefManager(getApplicationContext());

        recyclerView = findViewById(R.id.sug_recycler_single);
        models = new ArrayList<>();
        db = new AppLockDatabase(getApplicationContext());
        Cursor cursor = db.searchInAppDataTable(PARENT_KEY);
        cursor.moveToFirst();
        parent_key = cursor.getString(2);
        cursor.close();
        Cursor c = db.searchInAppDataTable(CHILD_ID);
        c.moveToFirst();
        childiD = c.getString(2);
        c.close();

        reference = FirebaseDatabase.getInstance().getReference(parent_key).child(childiD).child(SUGGESTION);

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //iosDialog.cancel();
                count++;
                prefManager.setSugCount(count);
                String ffrom = dataSnapshot.child("name").getValue().toString();
                String link = dataSnapshot.child("link").getValue().toString();
                SuggestModel model = new SuggestModel(ffrom,link);
                models.add(model);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                SuggestionAdapter adapter = new SuggestionAdapter(models,parent_key,childiD,getApplicationContext());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
