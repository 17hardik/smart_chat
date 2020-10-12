package com.example.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class Intro extends AppCompatActivity {
    private static int time = 1500;
    int i;
    String S, loginStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(S,i);
        loginStatus = preferences.getString("Status","No");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        getSupportActionBar().hide();
        new Handler().postDelayed((new Runnable() {
            @Override
            public void run() {
                if(loginStatus.equals("No")) {
                    Intent intro = new Intent(Intro.this, Login.class);
                    startActivity(intro);
                }
                else{
                    Intent intent = new Intent(Intro.this,MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }),time);
    }
}
