package com.example.educher_child;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class UsagePermission extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_permission);

//        //change status bar color
//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.color1));


        if (hasUsageStatsPermission(this)){
            startActivity(new Intent(getApplicationContext(),BatteryPermission.class));
            finish();
        }

        findViewById(R.id.next_usage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasUsageStatsPermission(UsagePermission.this)) {
                    startActivity(new Intent(getApplicationContext(), BatteryPermission.class));
                    finish();
                }else{
                    Toast.makeText(UsagePermission.this, "Please Give Permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.grant_usage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUsageStatsPermission();
            }
        });
    }

    void requestUsageStatsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !hasUsageStatsPermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }
}
