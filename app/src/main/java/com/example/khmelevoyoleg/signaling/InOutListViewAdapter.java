package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

class InOutListViewAdapter extends SimpleAdapter {

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу
    private final String LOG_TAG = "myLogs";
    private int idTag = 0;      // идентификатор строчки в ListView

    InOutListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
    }

    @Override
    public void setViewImage(ImageView v, int value) {
        super.setViewImage(v, value);
        v.setTag(idTag);
        idTag++;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "itemClick: position" + view.getTag());
            }
        });
    }
}