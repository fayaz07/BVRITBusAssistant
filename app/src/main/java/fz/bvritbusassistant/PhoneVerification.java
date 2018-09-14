package fz.bvritbusassistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class PhoneVerification extends AppCompatActivity {

    private static final String TAG = "PhoneLogin";
    private static boolean userExists = false;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private ContentLoadingProgressBar js;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private String phones[],routeCodes[];


    private FirebaseAuth mAuth;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private TextView mStatusText;
    private TextView mDetailText;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;

    private Button mStartButton,mVerifyButton,mResendButton;
    private Spinner countryCode;
    private ProgressDialog progressDialog;

    private NetworkInfo info;
    private ConnectivityManager connectivityManager;
    boolean isNetworkactive;
    private int indexOfNumber;
    private String route;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        actionBar.setTitle("Verify your identity");
        actionBar.setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Clients");

        connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        info = connectivityManager.getActiveNetworkInfo();
        isNetworkactive = info!=null && info.isConnectedOrConnecting();
        if (!isNetworkactive){
            Toast.makeText(getApplicationContext(),"Check your internet",Toast.LENGTH_SHORT).show();
        }

        Intent intent = getIntent();
        indexOfNumber = Integer.parseInt(intent.getStringExtra("i"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        mAuth = FirebaseAuth.getInstance();
        countryCode = (Spinner)findViewById(R.id.countryCode);
        mDetailText = (TextView)findViewById(R.id.detail);
        mStatusText = (TextView)findViewById(R.id.statusText);

        mPhoneNumberField = (EditText) findViewById(R.id.field_phone_number);
        mVerificationField = (EditText) findViewById(R.id.field_verification_code);

        mStartButton = (Button) findViewById(R.id.button_start_verification);
        mVerifyButton = (Button)findViewById(R.id.button_verify_phone);
        mResendButton = (Button)findViewById(R.id.button_resend);
        mVerifyButton.setEnabled(false);
        mResendButton.setEnabled(false);
        mVerificationField.setEnabled(false);

        phones = getResources().getStringArray(R.array.numbers);
        mPhoneNumberField.setText(phones[indexOfNumber]);
        countryCode.setEnabled(false);
        mPhoneNumberField.setEnabled(false);

        routeCodes = getResources().getStringArray(R.array.codes);
        route = routeCodes[indexOfNumber];

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(final PhoneAuthCredential credential) {

                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
                mStatusText.setText("Verification Completed");
                Toast.makeText(getApplicationContext(),"Auto verification completed",Toast.LENGTH_LONG).show();
                //String phoneNumber = countryCode.getSelectedItem() + mPhoneNumberField.getText().toString();

      //          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                mDetailText.setText("Firebase ID: " + user.getUid());

                progressDialog.show();
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        onVerificationFinished();
                    }
                };
                handler.postDelayed(runnable,2000);
//                Log.d("UID Firebase",FirebaseAuth.getInstance().getCurrentUser().getUid());
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                String mess="";
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    mess = "Invalid Phone Number";
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    mess = "Too many requests, please try again";
                }else if (e instanceof FirebaseAuthUserCollisionException){
                    mess = "Oops, looks someone is already signed in";
                }else if (e instanceof FirebaseNetworkException){
                    mess = "Check your internet";
                }else if (e instanceof FirebaseAuthException){
                    mess = ((FirebaseAuthException) e).getErrorCode();
                }
                progressDialog.dismiss();
                mStatusText.setText("Verification failed, "+ mess);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mStatusText.setText("Enter the code sent you mobile or wait for auto-verification");
                progressDialog.dismiss();
                mVerificationId = verificationId;
                mResendToken = token;
                mVerifyButton.setEnabled(true);
            }
            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                progressDialog.dismiss();
                mStatusText.setText("Auto-verification timed out, please enter the code");
            }
        };

        mResendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkactive){
                    Toast.makeText(getApplicationContext(),"Check your internet",Toast.LENGTH_SHORT).show();
                }
                progressDialog.show();
                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);
            }
        });
        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkactive){
                    Toast.makeText(getApplicationContext(),"Check your internet",Toast.LENGTH_SHORT).show();
                }
                progressDialog.show();
                if (!validateCode()){
                    mVerificationField.setError("Invalid code");
                    return;
                }
                String code = mVerificationField.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVerifictaionProcess();
            }
        });
        startVerifictaionProcess();
    }

    private void startVerifictaionProcess(){
        if (!isNetworkactive){
            Toast.makeText(getApplicationContext(),"Check your internet",Toast.LENGTH_SHORT).show();
        }
        if (!validatePhoneNumber()){
            mPhoneNumberField.setError("Invalid phone");
            return;
        }
        progressDialog.show();
        String phoneNumber = countryCode.getSelectedItem() + mPhoneNumberField.getText().toString();
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, PhoneVerification.this, mCallbacks);
        mVerifyButton.setEnabled(true);
        mResendButton.setEnabled(true);
        mVerificationField.setEnabled(true);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = task.getResult().getUser();
                            //mDetailText.setText("Firebase ID: " + user.getUid());

                            progressDialog.show();
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    onVerificationFinished();
                                }
                            };
                            handler.postDelayed(runnable,2000);
                            //Log.d("UID Firebase",task.getResult().getUser().getUid());

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                mStatusText.setText("Invalid otp");
                            }

                        }
                    }
                });
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneVerification.this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
        mStatusText.setText("Verification code resent");
        progressDialog.dismiss();
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone");
            return false;
        }
        return true;
    }


    private boolean validateCode() {
        String co = mVerificationField.getText().toString();
        if (co.isEmpty() || (co.length()>6)){
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return false;
    }

    private void onVerificationFinished(){
        ClientDataModel cm = new ClientDataModel();
        cm.setIndex(String.valueOf(indexOfNumber));
        cm.setPhone(phones[indexOfNumber]);
        cm.setRouteCode(routeCodes[indexOfNumber]);
        databaseReference.child(routeCodes[indexOfNumber]).setValue(cm);

        Intent intent = new Intent(PhoneVerification.this, StartService.class);
        intent.putExtra("i",indexOfNumber);
        startActivity(intent);
        progressDialog.dismiss();
        finish();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PhoneVerification.this,MainActivity.class));
        finish();
    }

}