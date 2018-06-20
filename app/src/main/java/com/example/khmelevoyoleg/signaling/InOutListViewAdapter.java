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

class InOutListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener{

    static final String STATUS_OFF = "STATUS_OFF"; // состояние входа - выключен
    static final String STATUS_ON = "STATUS_ON"; // состояние входа - включен
    static final String STATUS_START_ACTIVE = "STATUS_START_ACTIVE"; // состояние входа - активен на момент включения
    static final String STATUS_ALARM = "STATUS_ALARM"; // состояние входа - сработал

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

    InOutListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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
        int swNumber = (int) swActive.getTag(R.id.swInOutState);
        // усанавливаем новое значение переключателя
        activity.inOutState.set(swNumber, isChecked);
    }

    private static class ViewHolder {
        TextView tvInOutNumber; // номер входа в списке
        EditText etInOutName; // имя входа в списке
        ImageView ivInOutStatus; // состояние входа в списке
        SwitchCompat swInOutState; // состояние входа - вкл/выкл
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // получаем View
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            viewHolder = new ViewHolder();
            viewHolder.tvInOutNumber = (TextView) convertView.findViewById(R.id.tvInOutNumber);
            viewHolder.etInOutName = (EditText) convertView.findViewById(R.id.etInOutName);
            viewHolder.ivInOutStatus = (ImageView) convertView.findViewById(R.id.ivInOutStatus);
            viewHolder.swInOutState = (SwitchCompat) convertView.findViewById(R.id.swInOutState);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvInOutNumber.setTag(R.id.tvInOutNumber, position);
            viewHolder.etInOutName.setTag(R.id.etInOutName, position);
            viewHolder.ivInOutStatus.setTag(R.id.ivInOutStatus, position);
            viewHolder.swInOutState.setTag(R.id.swInOutState, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etInOutName.setOnFocusChangeListener(this);
            viewHolder.swInOutState.setOnCheckedChangeListener(this);
            // устанавливаем значение картинки состояния входа
            viewHolder.ivInOutStatus.setImageResource(getImageViewValue(position));
        } else {
            // задаем Tag для EditText
            viewHolder = (ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvInOutNumber.setTag(R.id.tvInOutNumber, position);
            viewHolder.etInOutName.setTag(R.id.etInOutName, position);
            viewHolder.ivInOutStatus.setTag(R.id.ivInOutStatus, position);
            viewHolder.swInOutState.setTag(R.id.swInOutState, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvInOutNumber.setText(activity.inOutNumber.get(position));
            viewHolder.etInOutName.setText(activity.inOutName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivInOutStatus.setImageResource(getImageViewValue(position));
            // устанавливаем значение переключателя
            viewHolder.swInOutState.setChecked(activity.inOutState.get(position));
        }
        return convertView;
    }

    /**
     * получение нужной картинки для вывода в ivInOutStatus
     * @param position - позиция элемента в списке
     * @return - номер ресурса
     */
    private int getImageViewValue(int position) {
        if (activity.inOutStatus.get(position).equals(STATUS_OFF)) {
            return R.drawable.circle_grey48;
        } else if (activity.inOutStatus.get(position).equals(STATUS_ON)) {
            return R.drawable.circle_green48;
        } else if (activity.inOutStatus.get(position).equals(STATUS_START_ACTIVE)) {
            return R.drawable.circle_blue48;
        } else if (activity.inOutStatus.get(position).equals(STATUS_ALARM)) {
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
            // фокус был потерян, нужно сохранить новое значение EditText в inOutName
            int etActiveNumber = (int) etActive.getTag(R.id.etInOutName);
            activity.inOutName.set(etActiveNumber, etActive.getText().toString());
        }
    }
}