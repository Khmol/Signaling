package com.example.khmelevoyoleg.signaling;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;


public class InOut extends Activity implements View.OnClickListener {

    // имена атрибутов для Map
    final String ATTRIBUTE_NAME_TEXT = "text";
    final String ATTRIBUTE_NAME_CHECKED = "checked";
    final String ATTRIBUTE_NAME_IMAGE = "image";

    final String LOG_TAG = "myLogs";
    ListView lvInOut;
    SimpleAdapter adapter;

    MediaPlayer mediaPlayer;
    final String DATA_SD = Environment.getExternalStorageDirectory().toString();
    final String DATA_SD1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).toString();
    int soundIdAlarm;
    int soundIdPreAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pbInOutCancel:
                break;
            case R.id.pbInOutSave:
                break;
        }
    }
}
