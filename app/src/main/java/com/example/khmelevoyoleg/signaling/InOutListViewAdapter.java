package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import java.util.List;
import java.util.Map;

class InOutListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener{

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу
    private int etActiveNumber;

    InOutListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
    }

    private static class ViewHolder {
        EditText etItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // получаем View
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            viewHolder = new ViewHolder();
            viewHolder.etItem = (EditText) convertView.findViewById(R.id.etlvInOutName);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для EditText
            viewHolder.etItem.setTag(position);
            viewHolder.etItem.setOnFocusChangeListener(this);
        } else {
            // задаем Tag для EditText
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.etItem.setTag(position);
            convertView = super.getView(position, convertView, parent);
            viewHolder.etItem.setText(activity.inOutName.get(position));
        }
        return convertView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // получаем EditText
        EditText etActive = (EditText) v;
        if (hasFocus){
            // фокус появился, нужно запомнить номер данного EditText
            etActiveNumber = (int) etActive.getTag();;
        } else {
            // фокус был потерян, нужно сохранить новое значение EditText в inOutName
            activity.inOutName.set(etActiveNumber, etActive.getText().toString());
        }
    }
}