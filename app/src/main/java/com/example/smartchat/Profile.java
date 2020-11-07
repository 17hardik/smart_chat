package com.example.smartchat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Random;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/** Activity through which users can view their profile and can make edit in it
 * Both database reading and writing functionality are working here
 */

public class Profile extends AppCompatActivity{
    EditText ETUsername, ETName;
    Button BTUsername, BTName, BTPassword, BTDelete;
    DatabaseReference reff;
    ConstraintLayout Layout;
    ImageView camera, profile, fullProfile, premiumProfile;
    boolean isFull = false;
    Firebase firebase;
    StorageReference mStorageReference;
    String user_name, name, phone, S, user_phone;
    int i;
    final static int PICK_IMAGE_REQUEST = 2342;
    Menu menu1;
    ProgressDialog pd;
    String username;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(S,i);
        phone = preferences.getString("Phone","");
        path = preferences.getString("path","");
        setContentView(R.layout.activity_profile);
        Layout = findViewById(R.id.layout);
        ETUsername = findViewById(R.id.usernameET);
        ETName = findViewById(R.id.nameET);
        camera = findViewById(R.id.camera);
        profile = findViewById(R.id.profile_image);
        fullProfile = findViewById(R.id.profile_image_full);
        BTUsername = findViewById(R.id.usernameBT);
        BTName = findViewById(R.id.nameBT);
        BTPassword = findViewById(R.id.password);
        BTDelete = findViewById(R.id.delete);
        FirebaseApp.initializeApp(this);
        Firebase.setAndroidContext(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(Profile.this);
        pd.setMessage("Updating Image...");

        firebase = new Firebase("https://smart-chat-cc69a.firebaseio.com/Users");
        reff = FirebaseDatabase.getInstance().getReference().child("Users").child(phone);
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    user_name = dataSnapshot.child("Username").getValue().toString();
                    name = dataSnapshot.child("Name").getValue().toString();
                    user_phone = dataSnapshot.child("Phone").getValue().toString();
                    username  = decryptUsername(user_name).toString();
                    mStorageReference = FirebaseStorage.getInstance().getReference().child(user_phone).child("Profile Picture");
                    ETUsername.setText(username);
                    ETName.setText(name);
                } catch(Exception e){
                    Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                try {
                    final long ONE_MEGABYTE = 1024 * 1024;
                    mStorageReference.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    DisplayMetrics dm = new DisplayMetrics();
                                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                                    profile.setMinimumHeight(dm.heightPixels);
                                    profile.setMinimumWidth(dm.widthPixels);
                                    profile.setImageBitmap(bm);
                                    fullProfile.setImageBitmap(bm);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    });
                } catch(Exception e){
                    Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Profile.this, "There is some error", Toast.LENGTH_SHORT).show();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullProfile.setVisibility(View.VISIBLE);
                Layout.setVisibility(View.INVISIBLE);
                getSupportActionBar().hide();
                isFull = true;
            }
        });

        BTUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String encryptedUsername = encryptUsername(ETUsername.getText().toString().trim()).toString();
                firebase.child(phone).child("Username").setValue(encryptedUsername);
                Toast.makeText(Profile.this, "Username Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        BTName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebase.child(phone).child("Name").setValue(ETName.getText().toString().trim());
                Toast.makeText(Profile.this, "Name Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        BTPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, ChangePassword.class);
                startActivity(intent);
            }
        });

        BTDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAccount del=new DeleteAccount(Profile.this);
                del.show();
            }
        });
        fullProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullProfile.setVisibility(View.GONE);
                Layout.setVisibility(View.VISIBLE);
                getSupportActionBar().show();
                isFull = false;
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
                Intent intent = new Intent(Profile.this, Login.class);
                startActivity(intent);
                SharedPreferences.Editor editor = getSharedPreferences(S, i).edit();
                editor.putString("Status", "No");
                editor.apply();
                finishAffinity();
                break;

            }

            case R.id.contact_us:
                Intent intent = new Intent(Profile.this, ContactUs.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void checkPermission() {

        //Checking Storage read permission for fetching internal documents
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 1);
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                if (img != null) {
                    pd.show();
                    profile.setImageBitmap(img);
                    fullProfile.setImageBitmap(img);
                    uploadFile(data.getData());
                    SharedPreferences.Editor editor1 = getSharedPreferences(S,i).edit();
                    editor1.putString("path", path);
                    editor1.apply();
                } else{

                }
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        }
    }

    private void uploadFile(Uri data) {
        mStorageReference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Toast.makeText(Profile.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        });
    }

    public StringBuilder decryptUsername(String uname) {
        int pllen;
        StringBuilder sb = new StringBuilder();
        int ciplen = uname.length();
        String temp = Character.toString(uname.charAt(ciplen - 2));
        if (temp.matches("[a-z]+")) {
            pllen = Character.getNumericValue(uname.charAt(ciplen - 1));
            pllen -= 2;
        } else {
            String templen = uname.charAt(ciplen - 2) + Character.toString(uname.charAt(ciplen - 1));
            pllen = Integer.parseInt(templen);
            pllen -= 2;
        }
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
    public StringBuilder encryptUsername(String uname) {
        StringBuilder stringBuilder = new StringBuilder();
        Random r = new Random();
        int len = uname.length();
        for (int i = 0; i < len; i++) {
            char a = uname.charAt(i);
            char c = (char) (r.nextInt(26) + 'a');
            stringBuilder.append((a - len) - (2 * i));
            stringBuilder.append(c);
        }
        String strlen = Integer.toString(len + 2);
        stringBuilder.append(strlen);
        return stringBuilder;
    }

    @Override
    public void onBackPressed() {
        if(!isFull) {
            super.onBackPressed();
        } else {
            fullProfile.setVisibility(View.GONE);
            Layout.setVisibility(View.VISIBLE);
            getSupportActionBar().show();
            isFull = false;
        }
    }
}
