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

import java.util.List;
import java.util.Map;

class AnalogInListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener{

        private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

        AnalogInListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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
        int swNumber = (int) swActive.getTag(R.id.swAnalogInState);
        // усанавливаем новое значение переключателя
        activity.analogInState.set(swNumber, isChecked);
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
        AnalogInListViewAdapter.ViewHolder viewHolder;
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
            viewHolder.ivAnalogInStatus.setImageResource(getImageViewValue(position));
        } else {
            // задаем Tag для EditText
            viewHolder = (AnalogInListViewAdapter.ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvAnalogInNumber.setTag(R.id.tvAnalogInNumber, position);
            viewHolder.etAnalogInName.setTag(R.id.etAnalogInName, position);
            viewHolder.ivAnalogInStatus.setTag(R.id.ivAnalogInStatus, position);
            viewHolder.swAnalogInState.setTag(R.id.swAnalogInState, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvAnalogInNumber.setText(activity.analogInNumber.get(position));
            viewHolder.etAnalogInName.setText(activity.analogInName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivAnalogInStatus.setImageResource(getImageViewValue(position));
            // устанавливаем значение переключателя
            viewHolder.swAnalogInState.setChecked(activity.analogInState.get(position));
        }
        return convertView;
    }

    /**
     * получение нужной картинки для вывода в ivAnalogInStatus
     * @param position - позиция элемента в списке
     * @return - номер ресурса
     */
    private int getImageViewValue(int position) {
        if (activity.digInStatus.get(position).equals(MainActivity.STATUS_OFF)) {
            return R.drawable.circle_grey48;
        } else if (activity.digInStatus.get(position).equals(MainActivity.STATUS_ON)) {
            return R.drawable.circle_green48;
        } else if (activity.digInStatus.get(position).equals(MainActivity.STATUS_START_ACTIVE)) {
            return R.drawable.circle_blue48;
        } else if (activity.digInStatus.get(position).equals(MainActivity.STATUS_ALARM)) {
            return R.drawable.circle_red48;
        }
        return 0;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // получаем EditText
        EditText etActive = (EditText) v;
        if (hasFocus){
            // фокус появился нужно вернуть фокус на данный EditText при обновлении окна
            etActive.requestFocusFromTouch();
        } else {
            // фокус был потерян, нужно сохранить новое значение EditText в digInName
            int etActiveNumber = (int) etActive.getTag(R.id.etAnalogInName);
            activity.analogInName.set(etActiveNumber, etActive.getText().toString());
        }
    }
}