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
        // усанавливаем новое значение переключателя
        activity.digInState.set(swNumber, isChecked);
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
            // устанавливаем значение картинки состояния входа
            viewHolder.ivDigInStatus.setImageResource(getImageViewValue(position));
        } else {
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
            viewHolder.ivDigInStatus.setImageResource(getImageViewValue(position));
            // устанавливаем значение переключателя
            viewHolder.swDigInState.setChecked(activity.digInState.get(position));
        }
        return convertView;
    }

    /**
     * получение нужной картинки для вывода в ivDigInStatus
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
            int etActiveNumber = (int) etActive.getTag(R.id.etDigInName);
            activity.digInName.set(etActiveNumber, etActive.getText().toString());
        }
    }
}