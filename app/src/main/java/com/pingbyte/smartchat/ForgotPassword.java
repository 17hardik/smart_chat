package com.pingbyte.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Activity for account recovery in case if a user forgets his/her password
 * Users can change their password by verifying their DOB and Username
 */

public class ForgotPassword extends AppCompatActivity {
    private EditText Phone, Username, DOB;
    private Calendar myCalendar;
    private String dob, username, storedDOB, storedUsername, phone, check;
    String M, K;
    private Button Verify;
    int j, l;

    DatabaseReference reff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences1 = getSharedPreferences(M,j);
        check = preferences1.getString("Lang","Eng");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_forgot_password);
        Phone = findViewById(R.id.phone);
        Username = findViewById(R.id.username);
        DOB = findViewById(R.id.dob);
        Verify = findViewById(R.id.verifyButton);
        Verify.setBackgroundResource(R.drawable.button);

        SharedPreferences.Editor editor = getSharedPreferences(K,l).edit();
        editor.putString("Clicked", "No");
        editor.apply();

        FirebaseApp.initializeApp(this);
        Firebase.setAndroidContext(this);
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
        DOB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(ForgotPassword.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog pd = new ProgressDialog(ForgotPassword.this);

                pd.setMessage("Verifying...");
                pd.show();
                phone = Phone.getText().toString().trim();
                dob = DOB.getText().toString();
                username = Username.getText().toString().trim();
                if(phone.equals("")){
                    Phone.setError("Must be filled");
                    Phone.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                    pd.dismiss();
                }
                else if(username.equals("")){
                    Username.setError("Must be filled");
                    Username.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                    pd.dismiss();
                }
                else if(dob.equals("")){
                    DOB.setError("Must be filled");
                    DOB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
                    pd.dismiss();
                }

                else {

                    // matching entered credentials with our database records
                    try {
                        reff = FirebaseDatabase.getInstance().getReference().child("Users").child(phone);
                    } catch (Exception e) {

                    }
                    reff.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                storedUsername = dataSnapshot.child("Username").getValue().toString();
                                storedDOB = dataSnapshot.child("DOB").getValue().toString();
                            } catch (Exception e){

                            }
                            try {
                                if (storedDOB.equals(dob) && decryptUsername(storedUsername).toString().equals(username)) {
                                    Intent intent = new Intent(ForgotPassword.this, ForgotSecond.class);
                                    intent.putExtra("phone", phone);
                                    startActivity(intent);
                                    pd.dismiss();

                                } else {
                                    Toast.makeText(ForgotPassword.this, storedUsername, Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                        Toast.makeText(ForgotPassword.this, "Username or DOB is incorrect", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e){
                                    pd.dismiss();
                                    Toast.makeText(ForgotPassword.this, "User not found", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                pd.dismiss();
                                Toast.makeText(ForgotPassword.this, "There is some error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }
    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        DOB.setText(sdf.format(myCalendar.getTime()));
    }

    public StringBuilder decryptUsername(String uname) {
        int pllen;
        StringBuilder sb = new StringBuilder();
        int ciplen = uname.length();
        String temp = Character.toString(uname.charAt(ciplen - 2));
        if (temp.matches("[a-z]+")) {
            pllen = Character.getNumericValue(uname.charAt(ciplen - 1));
        } else {
            String templen = uname.charAt(ciplen - 2) + Character.toString(uname.charAt(ciplen - 1));
            pllen = Integer.parseInt(templen);
        }
        pllen -= 2;
        String[] separated = uname.split("[a-zA-Z]");
        for (int i = 0; i < pllen; i++) {
            String splitted = separated[i];
            int split = Integer.parseInt(splitted);
            split = split + pllen + (2 * i);
            char pln = (char) split;
            sb.append(pln);
        }
        return sb;
    }

}
