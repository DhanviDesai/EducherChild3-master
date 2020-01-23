package com.example.educher_child;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.educher_child.Database.AppLockDatabase;
import com.google.firebase.database.DatabaseReference;

import static com.example.educher_child.AppConfiguration.CONNECT;

public class Login extends AppCompatActivity {
    private EditText passwordwrapper;
    private EditText phone;
    private Spinner spinner;
    private String[] countrtyCode= {"+91","+92","+93"};
    private DatabaseReference reference;
    private String ph,pass,ch;
    private AppLockDatabase dataBaseHelper;
    //private IOSDialog iosDialog;
    private AppLockDatabase database;
    private static final String TAG = "Login";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = new AppLockDatabase(getApplicationContext());
        Cursor cursor = dataBaseHelper.searchInAppDataTable(CONNECT);
        if (cursor.getCount() > 0){
            startActivity(new Intent(getApplicationContext(),Home.class));
            finish();
        }
        setContentView(R.layout.activity_login);

        //change status bar color
//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.color1));


        init();

        setupSpinnerData();

//        iosDialog = new IOSDialog.Builder(this)
//                .setCancelable(false)
//                .setSpinnerClockwise(false)
//                .setMessageContentGravity(Gravity.END)
//                .build();

        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ph = spinner.getSelectedItem()+phone.getText().toString().trim();
                if (ph.isEmpty()){
                    Toast.makeText(Login.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                //iosDialog.show();
                verify();
            }
        });

        findViewById(R.id.textView9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

    }

    private void init(){

        passwordwrapper = findViewById(R.id.login_pass);
        phone = findViewById(R.id.login_phone);
        spinner = findViewById(R.id.login_spinner);
        database = new AppLockDatabase(getApplicationContext());
    }

    private void setupSpinnerData(){
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countrtyCode);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void verify(){
        ch = ph.replace("+","");
        Toast.makeText(this, ""+ph, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(),PhoneVerification.class);
        intent.putExtra("phone",ph);
        startActivity(intent);
    }
}
