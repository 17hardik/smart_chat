package com.example.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Chat extends AppCompatActivity {

    LinearLayout layout;
    ImageView sendButton, encryptButton, infoButton;
    EditText messageArea;
    ScrollView scrollView;
    TextView Suggestion1, Suggestion2, Suggestion3;
    String S, name, chatwith, phone, username;
    int i, count = 0;
    Intent intent;
    List<TextMessage> conversation;
    Firebase reference1, reference2;
    boolean isEncryptionSet = false, isChatLayoutOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(S,i);
        phone = preferences.getString("Phone","");
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setCustomView(R.layout.chat_action_bar);
        intent = getIntent();
        chatwith = intent.getStringExtra("Phone");
        username = intent.getStringExtra("Username");
        layout = findViewById(R.id.layout1);
        sendButton = findViewById(R.id.sendButton);
        encryptButton = findViewById(R.id.encrypt_button);
        infoButton = findViewById(R.id.info_button);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);
        Suggestion1 = findViewById(R.id.suggestion1);
        Suggestion2 = findViewById(R.id.suggestion2);
        Suggestion3 = findViewById(R.id.suggestion3);
        name = phone;
        Firebase.setAndroidContext(this);
        conversation = new ArrayList<>();
        reference1 = new Firebase("https://smart-chat-cc69a.firebaseio.com/messages/" + name + "_" + chatwith);
        reference2 = new Firebase("https://smart-chat-cc69a.firebaseio.com/messages/" + chatwith + "_" + name);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.encryption_info);
                getSupportActionBar().hide();
                isChatLayoutOpened = true;
            }
        });

        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEncryptionSet) {
                    Toast.makeText(Chat.this, "Enabled Encrypted Chat", Toast.LENGTH_SHORT).show();
                    encryptButton.setImageResource(R.drawable.ic_baseline_lock_open_24);
                    messageArea.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    messageArea.setTypeface(Typeface.SANS_SERIF);
                    messageArea.setSelection(messageArea.getText().length());
                    isEncryptionSet = true;
                } else {
                    Toast.makeText(Chat.this, "Disabled Encrypted Chat", Toast.LENGTH_SHORT).show();
                    encryptButton.setImageResource(R.drawable.ic_baseline_lock_24);
                    messageArea.setInputType(InputType.TYPE_CLASS_TEXT);
                    messageArea.setSelection(messageArea.getText().length());
                    isEncryptionSet = false;
                }
            }
        });

        encryptButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent decryptIntent = new Intent(Chat.this, DecryptMessage.class);
                startActivity(decryptIntent);
                return true;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();
                InputMethodManager imm = (InputMethodManager)Chat.this.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageArea.getWindowToken(), 0);
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    if(isEncryptionSet) {
                        map.put("message", encryptMessage(messageText).toString());
                    } else {
                        map.put("message", messageText);
                    }
                    map.put("user", name);
                    map.put("time", currentTime);
                    map.put("date", currentDate);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                } else {
                    Toast.makeText(Chat.this, "Please write something", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Suggestion1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageArea.setText(Suggestion1.getText().toString());
                Suggestion1.setVisibility(View.GONE);
                Suggestion2.setVisibility(View.GONE);
                Suggestion3.setVisibility(View.GONE);
            }
        });

        Suggestion2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageArea.setText(Suggestion2.getText().toString());
                Suggestion1.setVisibility(View.GONE);
                Suggestion2.setVisibility(View.GONE);
                Suggestion3.setVisibility(View.GONE);
            }
        });

        Suggestion3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageArea.setText(Suggestion3.getText().toString());
                Suggestion1.setVisibility(View.GONE);
                Suggestion2.setVisibility(View.GONE);
                Suggestion3.setVisibility(View.GONE);
            }
        });

        getSupportActionBar().setTitle(username);
        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();
                String time = map.get("time").toString();
                conversation.add(TextMessage.createForLocalUser(message, System.currentTimeMillis()));
                smartReply();
                messageArea.setText("");

                if(userName.equals(name)){
                    addMessageBox(time + "\n\n" + message, 1);
                }
                else{
                    addMessageBox(time + "\n\n" + message, 2);
                }
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);
        textView.setTextIsSelectable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(lp);

        if(type == 1) {
            textView.setBackgroundResource(R.drawable.userchatbox);
            lp.setMargins(600,0,0,10);
        }
        else{
            textView.setBackgroundResource(R.drawable.chatwithchatbox);
            lp.setMargins(0,0,600,10);
        }

        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
    public void smartReply(){
        SmartReplyGenerator smartReply = SmartReply.getClient();
        smartReply.suggestReplies(conversation)
                .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onSuccess(SmartReplySuggestionResult result) {
                        if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            // The conversation's language isn't supported, so
                            // the result doesn't contain any suggestions.

                        } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            // Task completed successfully
                            // ...
                            for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                                String replyText = suggestion.getText();
                                if(count == 0) {
                                    Suggestion1.setVisibility(View.VISIBLE);
                                    Suggestion1.setText(replyText);
                                    count++;
                                } else if(count == 1) {
                                    Suggestion2.setVisibility(View.VISIBLE);
                                    Suggestion2.setText(replyText);
                                    count++;
                                } else {
                                    Suggestion3.setVisibility(View.VISIBLE);
                                    Suggestion3.setText(replyText);
                                    count = 0;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Toast.makeText(Chat.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public StringBuilder encryptMessage(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        Random r = new Random();
        int len = message.length();
        for (int i = 0; i < len; i++) {
            char a = message.charAt(i);
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
        if(!isChatLayoutOpened) {
            super.onBackPressed();
        } else {
            recreate();
            isChatLayoutOpened = false;
        }
    }
}