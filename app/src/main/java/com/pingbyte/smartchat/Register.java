package com.pingbyte.smartchat;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity implements View.OnClickListener {

    EditText username, password, TPhone, TName, TDate, TConfirm;
    Button registerButton;
    String user, pass, user_phone, user_name, date,confirm, mVerificationId, url, M;
    TextView login, already;
    ProgressDialog pd;
    int j, count = 1;
    ImageView Eye;
    Calendar myCalendar;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences1 = getSharedPreferences(M,j);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);
        login = findViewById(R.id.login2);
        login.setOnClickListener(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        already = findViewById(R.id.already);
        Eye = findViewById(R.id.eye);
        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(this);
        TDate= findViewById(R.id.dob);
        TPhone = findViewById(R.id.phone);
        TConfirm=findViewById(R.id.confirm);
        TName=findViewById(R.id.name);
        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        TDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(Register.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count%2!=0) {
                    password.setInputType(InputType.TYPE_CLASS_TEXT);
                    password.setSelection(password.getText().length());
                    TConfirm.setInputType(InputType.TYPE_CLASS_TEXT);
                    TConfirm.setSelection(TConfirm.getText().length());
                    Eye.setImageResource(R.drawable.closed_eye);
                }
                else{
                    password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password.setTypeface(Typeface.SANS_SERIF);
                    password.setSelection(password.getText().length());
                    TConfirm.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    TConfirm.setTypeface(Typeface.SANS_SERIF);
                    TConfirm.setSelection(TConfirm.getText().length());
                    Eye.setImageResource(R.drawable.open_eye);
                }
                count++;
            }
        });

        Firebase.setAndroidContext(this);
        pd = new ProgressDialog(Register.this);
        initFireBaseCallbacks();
        url = "https://smart-chat-cc69a.firebaseio.com/Users.json";
    }
    void initFireBaseCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                    TPhone.setError("Error: "+e.getMessage());
                    pd.dismiss();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                pd.dismiss();
                Toast.makeText(Register.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                mVerificationId = verificationId;
                mResendToken = token;
                Intent intent = new Intent(Register.this, PhoneVerification.class);
                intent.putExtra("username", user);
                intent.putExtra("phone", user_phone);
                intent.putExtra("password", pass);
                intent.putExtra("name", user_name);
                intent.putExtra("dob", date);
                intent.putExtra("verificationId", mVerificationId);
                intent.putExtra("resendToken", mResendToken);
                startActivity(intent);
            }
        };
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button:
                user = username.getText().toString().trim();
                pass = password.getText().toString().trim();
                user_phone = TPhone.getText().toString();
                user_name = TName.getText().toString().trim();
                confirm = TConfirm.getText().toString().trim();
                date = TDate.getText().toString();
                pd.setMessage("Registering...");
                pd.show();
                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONObject obj = new JSONObject(s);
                            if (obj.has(user_phone)) {
                                pd.dismiss();
                                Toast.makeText(Register.this, "User already exists", Toast.LENGTH_SHORT).show();
                            }
                            else if (user_name.equals("")) {
                                TName.setError("Must be filled");
                                TName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            }
                            else if (user.equals("")) {
                                username.setError("Must be filled");
                                username.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            } else if (pass.equals("")) {
                                password.setError("Must be filled");
                                password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            } else if (user.contains(" ")) {
                                username.setError("Space is not allowed");
                                username.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            } else if (user.length() < 5) {
                                username.setError("At least 5 characters must be there");
                                username.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            } else if (user_phone.equals("")) {
                                TPhone.setError("Must be filled");
                                TPhone.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            } else if (user_phone.length() != 10) {
                                TPhone.setError("Please enter a valid number");
                                TPhone.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            }else if (pass.length() < 5) {
                                password.setError("At least 5 characters must be there");
                                password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                                pd.dismiss();
                            } else if (date.length() != 10 || (!date.contains("/"))) {
                                pd.dismiss();
                                TDate.setError("Incorrect DOB Format");
                                TDate.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                            } else if (!(pass.equals(confirm))) {
                                pd.dismiss();
                                TConfirm.setError("Passwords are not matching");
                                TConfirm.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                            } else {
                                pd.setMessage("Sending OTP...");
                                pd.show();
                                //Sending OTP to verify provided phone number
                                send_data();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }


                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        pd.dismiss();
                    }
                });
                RequestQueue rQueue = Volley.newRequestQueue(Register.this);
                rQueue.add(request);

                break;
            case R.id.login2:
                Intent loginIntent = new Intent(Register.this, Login.class);
                startActivity(loginIntent);
                break;
        }
    }
    public void send_data(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + user_phone,
                1,
                TimeUnit.MINUTES,
                this,
                mCallbacks);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        TDate.setText(sdf.format(myCalendar.getTime()));
    }
}