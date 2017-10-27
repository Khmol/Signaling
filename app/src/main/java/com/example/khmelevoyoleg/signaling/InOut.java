package com.example.khmelevoyoleg.signaling;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.IOException;


public class InOut extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, View.OnClickListener {

    final String LOG_TAG = "myLogs";

    MediaPlayer mediaPlayer;
    final String DATA_SD = Environment.getExternalStorageDirectory().toString();
    final String DATA_SD1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).toString();
    int soundIdAlarm;
    int soundIdPreAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_out);

        mediaPlayer = new MediaPlayer();
        String path = getApplicationInfo().dataDir;
        Log.d("LOG_TAG", DATA_SD);
        Log.d("LOG_TAG", DATA_SD1);
        Log.d("LOG_TAG", path);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                break;
            case R.id.pause:
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
