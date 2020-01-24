package com.example.educher_child;

import android.annotation.TargetApi;
import android.app.Activity;
//import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class MainActivity extends Activity {

    public final static int REQUEST_CODE = 101;
    private static final String TAG = "OverlayPermission";
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Settings.canDrawOverlays(MainActivity.this)) {
            startActivity(new Intent(getApplicationContext(),ActiveAdministrator.class));
            finish();
        }
        findViewById(R.id.next_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Settings.canDrawOverlays(MainActivity.this)) {
                    startActivity(new Intent(getApplicationContext(), ActiveAdministrator.class));
                    finish();
                }else{
                    Toast.makeText(MainActivity.this, "Please Give Permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.grant_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDrawOverlayPermission();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {

        // check if we already  have permission to draw over other apps
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Log.v(TAG, "We already have permission for it.");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "OnActivity Result.");
        //check if received result code
        //  is equal our requested code for draw permission
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
