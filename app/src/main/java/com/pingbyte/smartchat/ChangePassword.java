
package com.pingbyte.smartchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.math.BigInteger;

/** Through this activity, a user can change his/her password by providing their current password
 * After verification, new password will be assigned to the user
 */

public class ChangePassword extends AppCompatActivity {
    EditText ETOld, ETNew, ETConfirm;
    Button BTPassword;
    String phone, pass, M, S, Cipher, password, new_pass, conf_pass, New_Cipher, lang;
    int i, j, count = 1, count2 = 1;
    Firebase firebase;
    DatabaseReference reff;
    ImageView Eye, EyeNew;
    Boolean isVerified = false, English = true;
    Menu menu1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Change Password");
        actionBar.setDisplayHomeAsUpEnabled(true);
        SharedPreferences preferences = getSharedPreferences(S,i);
        phone = preferences.getString("Phone","");
        setContentView(R.layout.activity_change_password);
        ETOld = findViewById(R.id.old_password);
        ETNew = findViewById(R.id.new_password);
        Eye = findViewById(R.id.eye);
        EyeNew = findViewById(R.id.eyeNew);
        ETConfirm = findViewById(R.id.confirm_password);
        BTPassword = findViewById(R.id.passwordButton);
        ETNew.setEnabled(false);
        ETConfirm.setEnabled(false);
        Firebase.setAndroidContext(this);

        Eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count%2!=0) {
                    ETOld.setInputType(InputType.TYPE_CLASS_TEXT);
                    ETOld.setSelection(ETOld.getText().length());
                    Eye.setImageResource(R.drawable.closed_eye);
                }
                else{
                    ETOld.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ETOld.setTypeface(Typeface.SANS_SERIF);
                    ETOld.setSelection(ETOld.getText().length());
                    Eye.setImageResource(R.drawable.open_eye);
                }
                count++;
            }
        });

        EyeNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count2%2!=0) {
                    ETNew.setInputType(InputType.TYPE_CLASS_TEXT);
                    ETNew.setSelection(ETNew.getText().length());
                    ETConfirm.setInputType(InputType.TYPE_CLASS_TEXT);
                    ETConfirm.setSelection(ETConfirm.getText().length());
                    EyeNew.setImageResource(R.drawable.closed_eye);
                }
                else{
                    ETNew.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ETNew.setTypeface(Typeface.SANS_SERIF);
                    ETNew.setSelection(ETNew.getText().length());
                    ETConfirm.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ETConfirm.setTypeface(Typeface.SANS_SERIF);
                    ETConfirm.setSelection(ETConfirm.getText().length());
                    EyeNew.setImageResource(R.drawable.open_eye);
                }
                count2++;
            }
        });

        firebase = new Firebase("https://smart-chat-cc69a.firebaseio.com/Users");
        reff = FirebaseDatabase.getInstance().getReference().child("Users").child(phone);
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                password = dataSnapshot.child("Password").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChangePassword.this, "There is some error", Toast.LENGTH_SHORT).show();
            }
        });
        BTPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVerified) {
                    pass = ETOld.getText().toString().trim();
                    if (pass.equals("")) {
                        ETOld.setError("Must be filled");
                    } else {
                        BigInteger hash = BigInteger.valueOf((phone.charAt(0) - '0') + (phone.charAt(2) - '0') + (phone.charAt(4) - '0') + (phone.charAt(6) - '0') + (phone.charAt(8) - '0'));
                        StringBuilder sb = new StringBuilder();
                        char[] letters = pass.toCharArray();
                        for (char ch : letters) {
                            sb.append((byte) ch);
                        }
                        String a = sb.toString();
                        BigInteger i = new BigInteger(a);
                        hash = i.multiply(hash);
                        Cipher = String.valueOf(hash);
                        if (Cipher.equals(password)) {
                            ETNew.setEnabled(true);
                            ETConfirm.setEnabled(true);
                            BTPassword.setText("Change Password");
                            isVerified = true;
                        } else {
                            Toast.makeText(ChangePassword.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    new_pass = ETNew.getText().toString().trim();
                    conf_pass = ETConfirm.getText().toString().trim();
                    if (!(new_pass.equals(conf_pass))) {
                        ETConfirm.setError("Passwords are not matching");
                        ETConfirm.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                    } else if (new_pass.length() < 5) {
                        ETNew.setError("At least 5 characters must be there");
                        ETNew.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                    } else{
                        BigInteger hash = BigInteger.valueOf((phone.charAt(0) - '0') + (phone.charAt(2) - '0') + (phone.charAt(4) - '0') + (phone.charAt(6) - '0') + (phone.charAt(8) - '0'));
                        StringBuilder sb = new StringBuilder();
                        char[] letters = new_pass.toCharArray();
                        for (char ch : letters) {
                            sb.append((byte) ch);
                        }
                        String a = sb.toString();
                        BigInteger i = new BigInteger(a);
                        hash = i.multiply(hash);
                        New_Cipher = String.valueOf(hash);
                        firebase.child(phone).child("Password").setValue(New_Cipher);
                        Intent intent = new Intent(ChangePassword.this, Profile.class);
                        startActivity(intent);
                        Toast.makeText(ChangePassword.this, "Password updated successfully" , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu1 = menu;
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.logout: {
                Intent intent = new Intent(ChangePassword.this, Login.class);
                startActivity(intent);
                SharedPreferences.Editor editor = getSharedPreferences(S, i).edit();
                editor.putString("Status", "No");
                editor.apply();
                finishAffinity();
                break;
            }

            case R.id.contact_us:
                Intent intent = new Intent(ChangePassword.this, ContactUs.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}