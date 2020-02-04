package com.example.educher_child;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ChildHidden extends AppCompatActivity {

    private boolean isHiddenPresent = false;
    private PackageManager packageManager;
    List<PackageInfo> apps;
    private Button install;
    private ImageView nextSecure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_hidden);

        init();

        packageManager = this.getPackageManager();
        apps = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
        if(apps != null && !apps.isEmpty()){
            for (int i=0;i<apps.size();i++){
                PackageInfo app = apps.get(i);
                   if(app.packageName.equals("com.example.childhidden")){
                       isHiddenPresent = true;
                       nextActivity();
                   }

            }
        }

        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(""));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("")));
                }
            }
        });

        nextSecure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isHiddenPresent){
                    nextActivity();
                }
                else{
                    Toast.makeText(ChildHidden.this, "Please Install the app for more security", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void init(){
        install = findViewById(R.id.install);
        nextSecure = findViewById(R.id.secureNext);
    }

    public void nextActivity(){

        Intent i = new Intent(ChildHidden.this,Login.class);
        startActivity(i);
        finish();
    }
}
