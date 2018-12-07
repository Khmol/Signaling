package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AnalogInListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener{

    private ArrayList<ViewHolder> viewHolderList;
    private ArrayList<String> oldAnalogInStatus;

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

    AnalogInListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        viewHolderList = new ArrayList<>();
        oldAnalogInStatus = new ArrayList<>();
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
        int swNumber = (int) swActive.getTag(R.id.swAnalogInState);
        // запоминаем новое значение переключателя (входа)
        // проверяем изменилось ли состояние переключателя относительно сохраненного значения
        if (activity.mAnalogInState.get(swNumber) != isChecked) {
            // изменяем сохраненное значене
            activity.mAnalogInState.set(swNumber, isChecked);
            // передаем в BT команду на включение/ отключение входа
            if (isChecked) {
                if (activity.checkAbilityTxBT())
                    activity.sendDataBT(String.format("%s%d\r", Utils.ADC_IN_ON, (swNumber + 1)), 0);
                else
                    // выдаем текстовое оповещение что соединение отсутствует
                    Toast.makeText(activity.getApplicationContext(),
                            R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                            Toast.LENGTH_SHORT).show();
            } else {
                if (activity.checkAbilityTxBT())
                    activity.sendDataBT(String.format("%s%d\r", Utils.ADC_IN_OFF, (swNumber + 1)), 0);
                else
                    // выдаем текстовое оповещение что соединение отсутствует
                    Toast.makeText(activity.getApplicationContext(),
                            R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class ViewHolder {
        TextView tvAnalogInNumber; // номер входа в списке
        EditText etAnalogInName; // имя входа в списке
        ImageView ivAnalogInStatus; // состояние входа в списке
        SwitchCompat swAnalogInState; // состояние входа - вкл/выкл
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // получаем View
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            viewHolder = new AnalogInListViewAdapter.ViewHolder();
            viewHolder.tvAnalogInNumber = (TextView) convertView.findViewById(R.id.tvAnalogInNumber);
            viewHolder.etAnalogInName = (EditText) convertView.findViewById(R.id.etAnalogInName);
            viewHolder.ivAnalogInStatus = (ImageView) convertView.findViewById(R.id.ivAnalogInStatus);
            viewHolder.swAnalogInState = (SwitchCompat) convertView.findViewById(R.id.swAnalogInState);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvAnalogInNumber.setTag(R.id.tvAnalogInNumber, position);
            viewHolder.etAnalogInName.setTag(R.id.etAnalogInName, position);
            viewHolder.ivAnalogInStatus.setTag(R.id.ivAnalogInStatus, position);
            viewHolder.swAnalogInState.setTag(R.id.swAnalogInState, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etAnalogInName.setOnFocusChangeListener(this);
            viewHolder.swAnalogInState.setOnCheckedChangeListener(this);
            // устанавливаем значение картинки состояния входа
            viewHolder.ivAnalogInStatus.setImageResource(Utils.getImageViewValue(activity.mAnalogInStatus, position));
            viewHolderList.add(viewHolder);
        } else {
            // задаем Tag для EditText
            viewHolder = (AnalogInListViewAdapter.ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvAnalogInNumber.setTag(R.id.tvAnalogInNumber, position);
            viewHolder.etAnalogInName.setTag(R.id.etAnalogInName, position);
            viewHolder.ivAnalogInStatus.setTag(R.id.ivAnalogInStatus, position);
            viewHolder.swAnalogInState.setTag(R.id.swAnalogInState, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvAnalogInNumber.setText(activity.mAnalogInNumber.get(position));
            viewHolder.etAnalogInName.setText(activity.mAnalogInName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivAnalogInStatus.setImageResource(Utils.getImageViewValue(activity.mAnalogInStatus, position));
            // устанавливаем значение переключателя
            viewHolder.swAnalogInState.setChecked(activity.mAnalogInState.get(position));
        }
        return convertView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // получаем EditText
        EditText etActive = (EditText) v;
        if (hasFocus){
            // фокус появился нужно вернуть фокус на данный EditText при обновлении окна
            etActive.requestFocusFromTouch();
        } else {
            // фокус был потерян, нужно сохранить новое значение EditText в mDigInName
            int etActiveNumber = (int) etActive.getTag(R.id.etAnalogInName);
            activity.mAnalogInName.set(etActiveNumber, etActive.getText().toString());
        }
    }

    /**
     * изменение картинки для входов если это нужно
     */
    void checkStatusPictureAnalogIn(ArrayList<String> analogInStatus) {
        int len = analogInStatus.size();
        if (oldAnalogInStatus.size() != len) {
            for (String curStatus : analogInStatus) {
                oldAnalogInStatus.add(curStatus);
            }
        }
        for (int i = 0; i < len; i++) {
            // проверяем изменился ли статус входа
            if ( ! analogInStatus.get(i).equals(oldAnalogInStatus.get(i))) {
                // изменился
                for(ViewHolder viewHolder: viewHolderList) {
                    int position = (int) viewHolder.ivAnalogInStatus.getTag(R.id.ivAnalogInStatus);
                    if (position == i) {
                        // устанавливаем значение картинки
                        viewHolder.ivAnalogInStatus.setImageResource(Utils.getImageViewValue(activity.mAnalogInStatus, i));
                    }
                }
                oldAnalogInStatus.set(i, analogInStatus.get(i));
            }
        }
    }
}
