package com.example.smartchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    TextView Title;
    String S, phone, u_name;
    int i, x, count = 0;
    Menu menu1;
    DatabaseReference reff, reff1;
    RecyclerView users;
    ArrayList<UserCardView> details;
    UserAdapter userAdapter;
    ProgressDialog pd;
    int size;
    Firebase firebase;
    SwipeRefreshLayout swipeRefreshLayout;
    HashMap<String, String> hashMap = new HashMap<String, String>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(S, i);
        phone = preferences.getString("Phone", "");
        setContentView(R.layout.activity_main);
        users = findViewById(R.id.users);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setCustomView(R.layout.chat_action_bar);
        View view = getSupportActionBar().getCustomView();
        Title = view.findViewById(R.id.users);
        users.setLayoutManager(new LinearLayoutManager(this));
        details = new ArrayList<>();
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        FirebaseApp.initializeApp(this);
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://smart-chat-cc69a.firebaseio.com/Users");
        try {
            getContactList();
        } catch (Exception e) {

        }

        final SearchView mySearchView = view.findViewById(R.id.mySearchView);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }
                            @Override
                            public boolean onQueryTextChange(String newText) {
                                userAdapter.getFilter().filter(newText);
                                return false;
                            }

                        });
                    }
                }, 3000
        );

        int searchCloseButtonId = mySearchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) mySearchView.findViewById(searchCloseButtonId);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mySearchView.getQuery().toString().equals("")) {
                    mySearchView.onActionViewCollapsed();
                } else {
                    mySearchView.setQuery("", false);
                    recreate();
                }
            }
        });

        reff = FirebaseDatabase.getInstance().getReference().child("Users");
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Fetching data");
        pd.show();

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(count == 0) {
                    size = (int) dataSnapshot.getChildrenCount();
                    for (final DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        reff1 = FirebaseDatabase.getInstance().getReference().child("Users").child(childDataSnapshot.getKey());
                        reff1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserCardView d = snapshot.getValue(UserCardView.class);
                                if (!(phone.equals(childDataSnapshot.getKey()))) {
                                    details.add(d);
                                    userAdapter = new UserAdapter(details, MainActivity.this);
                                    users.setAdapter(userAdapter);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    count++;
                } else {
                    recreate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    pd.dismiss();
                } catch (Exception e) {

                }
            }
        }, 3000);

        reff = FirebaseDatabase.getInstance().getReference().child("Users").child(phone);
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                u_name = dataSnapshot.child("Username").getValue().toString();
                phone = dataSnapshot.child("Phone").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "There is some error", Toast.LENGTH_SHORT).show();
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
            case R.id.profie:
                Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profileIntent);
                break;
            case R.id.logout: {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                SharedPreferences.Editor editor = getSharedPreferences(S, i).edit();
                editor.putString("Status", "No");
                editor.apply();
                finishAffinity();
                break;
            }
            case R.id.contact_us:
                Intent contactIntent = new Intent(MainActivity.this, ContactUs.class);
                startActivity(contactIntent);
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void getContactList() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            hashMap.put(name, phoneNumber);
        }
        firebase.child(phone).child("Contacts").setValue(hashMap.toString());
        phones.close();
    }

    @Override
    public void onRefresh() {
        recreate();
    }
}
