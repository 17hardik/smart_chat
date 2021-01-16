package com.pingbyte.smartchat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteAccount extends Dialog implements View.OnClickListener{
    Activity activity;
    Button BTYes, BTNo;
    int i, j;
    String phone, check, S, M;
    TextView deleteText;
    DatabaseReference reff;
    StorageReference mStorageReference;

    public DeleteAccount(Activity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = activity.getSharedPreferences(S,i);
        phone = preferences.getString("Phone","");
        SharedPreferences preferences1 = activity.getSharedPreferences(M,j);
        check = preferences1.getString("Lang","Eng");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_delete_account);
        BTYes =  findViewById(R.id.yes_button);
        BTNo = findViewById(R.id.no_button);
        deleteText = findViewById(R.id.delete_text);
        mStorageReference = FirebaseStorage.getInstance().getReference();
        BTYes.setOnClickListener(this);
        BTNo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes_button:
                reff = FirebaseDatabase.getInstance().getReference().child("Users").child(phone);
                StorageReference sRef4 = mStorageReference.child(phone).child("Profile Picture");
                reff.removeValue();
                sRef4.delete();
                Intent intent = new Intent(activity, Login.class);
                activity.startActivity(intent);
                activity.finishAffinity();
                break;
            case R.id.no_button:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
