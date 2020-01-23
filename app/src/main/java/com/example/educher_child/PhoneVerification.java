package com.example.educher_child;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.educher_child.Database.AppLockDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.TimeUnit;

import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.CHILD_TOKEN;

public class PhoneVerification extends AppCompatActivity {

    //phone no of the child
    private String mobile;

    //verification id sent by firebase which is used with the code sent to authenticate the user in the database
    private String mVerificationId;

    //Login_text
    private TextView msg;

    //received code to the given mobile no
    private EditText code;

    //Firebase User authentication object
    private FirebaseAuth mAuth;

    //Firebase Database Reference
    // private DatabaseReference reference;


   // private IOSDialog iosDialog;

    //Private offline Database
    private AppLockDatabase database;

    //Unique Identifier given to the child by firebase
    private String token;


    private static final String TAG = "phoneverification";

    //Callbacks given to phoneverification
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    Log.d(TAG, "onVerificationCompleted: ");
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(PhoneVerification.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onVerificationFailed: "+e.getMessage());
                    //iosDialog.cancel();
                }

                @Override
                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    //iosDialog.cancel();
                    mVerificationId = s;
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        //change status bar color
//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.color1));


        mAuth = FirebaseAuth.getInstance();
        code = findViewById(R.id.code);
        msg = findViewById(R.id.login_text);
        database = new AppLockDatabase(getApplicationContext());


//        iosDialog = new IOSDialog.Builder(this)
//                .setCancelable(false)
//                .setSpinnerClockwise(false)
//                .setMessageContentGravity(Gravity.END)
//                .build();

        //Points to 'childregistration' node
        //reference = FirebaseDatabase.getInstance().getReference().child(CHILD_REGISTRATION);

        //Get Intent values from previous activity
        Bundle bundle = getIntent().getExtras();
        if (bundle !=null) {
            mobile = bundle.getString("phone");
        }

        saveToken();

        msg.setText("Please Type the verification code sent \n to +"+mobile);

        code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(code.getText().toString().trim().length() ==6){
                    String c = code.getText().toString().trim();
                    Log.d(TAG, "onTextChanged: "+code);
                   // iosDialog.show();
                    verifyVerificationCode(c);
                }
            }
        });
    }

    private void sendVerificationCode(String mobile) {
        //iosDialog.show();
        Log.d(TAG, "sendVerificationCode: "+mobile);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+"+mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void verifyVerificationCode(String code) {
        //creating the credential which takes in the verification id given by firebase and the code sent to the user
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(PhoneVerification.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                            //verification successful we will start the profile activity

                            //iosDialog.cancel();

                            //value inserted in the local database with child_id being the childs phone number
                            database.insertDataInAppDataTable(CHILD_ID,mobile);
                            nextActivity();

                        } else {
                            //iosDialog.cancel();

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }
                            Toast.makeText(PhoneVerification.this, ""+message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void nextActivity(){
        startActivity(new Intent(getApplicationContext(),ParentKey.class));
        finish();
    }

    //saves the token for the registered child in the database
    private void saveToken(){
        FirebaseApp.initializeApp(this);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener
                ( PhoneVerification.this,  new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        token = instanceIdResult.getToken();

                        //child_token is the unique token given by firebase
                        database.insertDataInAppDataTable(CHILD_TOKEN,token);
                        sendVerificationCode(mobile);
                    }
                });
    }
}
