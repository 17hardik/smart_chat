package com.pingbyte.smartchat;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ChatRecording extends RecyclerView.ViewHolder  {
    TextView leftText,rightText;

    public ChatRecording(View itemView){
        super(itemView);
        leftText = itemView.findViewById(R.id.leftText);
        rightText = itemView.findViewById(R.id.rightText);
    }
}