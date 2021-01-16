package com.pingbyte.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Intro extends AppCompatActivity {
    private static int time = 1500;
    int i;
    String S, loginStatus, phone;
    DatabaseReference reff;
    boolean isUserExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(S,i);
        loginStatus = preferences.getString("Status","No");
        phone = preferences.getString("Phone", "");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        getSupportActionBar().hide();
        FirebaseApp.initializeApp(this);
        Firebase.setAndroidContext(this);
        reff = FirebaseDatabase.getInstance().getReference().child("Users").child(phone);
        try {
            reff.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child("Phone") != null){
                        isUserExist = true;
                        new Handler().postDelayed((new Runnable() {
                            @Override
                            public void run() {
                                if(loginStatus.equals("No") || !isUserExist) {
                                    Intent intro = new Intent(Intro.this, Login.class);
                                    startActivity(intro);
                                }
                                else{
                                    Intent intent = new Intent(Intro.this, MainActivity.class);
                                    startActivity(intent);
                                }
                                finish();
                            }
                        }),time);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {

        }
    }
}
