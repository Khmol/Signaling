package com.example.khmelevoyoleg.signaling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get extra data included in the Intent
        String message = intent.getStringExtra("message");
        Log.d("receiver", "Got message: " + message);
    }
}
