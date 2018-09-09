package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

class DigInListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener{

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

    DigInListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // получаем SwitchCompat
        SwitchCompat swActive = (SwitchCompat) buttonView;
        // получаем номер данного SwitchCompat
        int swNumber = (int) swActive.getTag(R.id.swDigInState);
        // запоминаем новое значение переключателя (входа)
        activity.digInState.set(swNumber, isChecked);
        // передаем в BT команду на включение/ отключение входа
        if (isChecked) {
            if (activity.checkAbilityTxBT())
                activity.sendDataBT(String.format("%s%d\r", activity.IN_ON, (swNumber + 1)), 0);
            else
                // выдаем текстовое оповещение что соединение отсутствует
                Toast.makeText(activity.getApplicationContext(),
                        R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                        Toast.LENGTH_SHORT).show();
        } else {
            if (activity.checkAbilityTxBT())
                activity.sendDataBT(String.format("%s%d\r", activity.IN_OFF, (swNumber + 1)), 0);
            else
                // выдаем текстовое оповещение что соединение отсутствует
                Toast.makeText(activity.getApplicationContext(),
                        R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                        Toast.LENGTH_SHORT).show();
        }
    }

    private static class ViewHolder {
        TextView tvDigInNumber; // номер входа в списке
        EditText etDigInName; // имя входа в списке
        ImageView ivDigInStatus; // состояние входа в списке
        SwitchCompat swDigInState; // состояние входа - вкл/выкл
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // получаем View
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            viewHolder = new ViewHolder();
            viewHolder.tvDigInNumber = (TextView) convertView.findViewById(R.id.tvDigInNumber);
            viewHolder.etDigInName = (EditText) convertView.findViewById(R.id.etDigInName);
            viewHolder.ivDigInStatus = (ImageView) convertView.findViewById(R.id.ivDigInStatus);
            viewHolder.swDigInState = (SwitchCompat) convertView.findViewById(R.id.swDigInState);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvDigInNumber.setTag(R.id.tvDigInNumber, position);
            viewHolder.etDigInName.setTag(R.id.etDigInName, position);
            viewHolder.ivDigInStatus.setTag(R.id.ivDigInStatus, position);
            viewHolder.swDigInState.setTag(R.id.swDigInState, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etDigInName.setOnFocusChangeListener(this);
            viewHolder.swDigInState.setOnCheckedChangeListener(this);
        } else {
            convertView = super.getView(position, convertView, parent);
            // задаем Tag для EditText
            viewHolder = (ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvDigInNumber.setTag(R.id.tvDigInNumber, position);
            viewHolder.etDigInName.setTag(R.id.etDigInName, position);
            viewHolder.ivDigInStatus.setTag(R.id.ivDigInStatus, position);
            viewHolder.swDigInState.setTag(R.id.swDigInState, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvDigInNumber.setText(activity.digInNumber.get(position));
            viewHolder.etDigInName.setText(activity.digInName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivDigInStatus.setImageResource(activity.getDigInImageViewValue(position));
            // устанавливаем значение переключателя
            viewHolder.swDigInState.setChecked(activity.digInState.get(position));
        }
        return convertView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // получаем EditText
        EditText etActive = (EditText) v;
        if (hasFocus) {
            // фокус появился нужно вернуть фокус на данный EditText при обновлении окна
            etActive.requestFocusFromTouch();
        } else {
            // фокус был потерян, нужно сохранить новое значение EditText в digInName
            int etActiveNumber = (int) etActive.getTag(R.id.etDigInName);
            activity.digInName.set(etActiveNumber, etActive.getText().toString());
        }
    }
}