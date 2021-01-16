package com.pingbyte.smartchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DecryptMessage extends AppCompatActivity {

    EditText encryptedMessage;
    Button decryptButton;
    TextView decryptedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt_message);

        encryptedMessage = findViewById(R.id.encrypted_message);
        decryptButton = findViewById(R.id.decrypt_button);
        decryptedMessage = findViewById(R.id.decrypted_message);

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(encryptedMessage.getText().toString().equals(""))) {
                    try {
                        decryptedMessage.setText(decryptMessage(encryptedMessage.getText().toString()));
                    } catch(Exception e) {
                        encryptedMessage.setError("Please enter a valid message");
                    }
                } else {
                    encryptedMessage.setError("Must be filled");
                }
            }
        });
    }
    public StringBuilder decryptMessage(String message) {
        int pllen;
        StringBuilder sb = new StringBuilder();
        int ciplen = message.length();
        String temp = Character.toString(message.charAt(ciplen - 2));
        if (temp.matches("[a-z]+")) {
            pllen = Character.getNumericValue(message.charAt(ciplen - 1));
            pllen -= 2;
        } else {
            String templen = message.charAt(ciplen - 2) + Character.toString(message.charAt(ciplen - 1));
            pllen = Integer.parseInt(templen);
            pllen -= 2;
        }
        String[] separated = message.split("[a-zA-Z]");
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