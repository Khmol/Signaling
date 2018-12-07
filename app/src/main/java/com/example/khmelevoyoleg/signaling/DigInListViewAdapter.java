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

class DigInListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener{

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу
    private ArrayList<ViewHolder> viewHolderList;
    private ArrayList<String> oldDigInStatus;

    DigInListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // создаем список если его нет
        viewHolderList = new ArrayList<>();
        oldDigInStatus = new ArrayList<>();
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
        // проверяем изменилось ли состояние переключателя относительно сохраненного значения
        if (activity.mDigInState.get(swNumber) != isChecked) {
            // изменяем сохраненное значене
            activity.mDigInState.set(swNumber, isChecked);
            // передаем в BT команду на включение/ отключение входа
            if (isChecked) {
                if (activity.checkAbilityTxBT())
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_ON, (swNumber + 1)), 0);
                else
                    // выдаем текстовое оповещение что соединение отсутствует
                    Toast.makeText(activity.getApplicationContext(),
                            R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                            Toast.LENGTH_SHORT).show();
            } else {
                if (activity.checkAbilityTxBT())
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_OFF, (swNumber + 1)), 0);
                else
                    // выдаем текстовое оповещение что соединение отсутствует
                    Toast.makeText(activity.getApplicationContext(),
                            R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * изменение картинки для входов если это нужно
     */
    void checkStatusPictureDigIn(ArrayList<String> digInStatus) {
        int len = digInStatus.size();
        if (oldDigInStatus.size() != len) {
            for (String curStatus : digInStatus) {
                oldDigInStatus.add(curStatus);
            }
        }
        for (int i = 0; i < len; i++) {
            // проверяем изменился ли статус входа
            if ( !digInStatus.get(i).equals(oldDigInStatus.get(i))) {
                // изменился
                for(ViewHolder viewHolder: viewHolderList) {
                    int pos = (int) viewHolder.ivDigInStatus.getTag(R.id.ivDigInStatus);
                    if (pos == i) {
                        // устанавливаем значение картинки
                        viewHolder.ivDigInStatus.setImageResource(Utils.getImageViewValue(activity.mDigInStatus, i));
                    }
                }
                oldDigInStatus.set(i, digInStatus.get(i));
            }
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
            // создаем ViewHolder
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
            viewHolderList.add(viewHolder);
        } else {
            // задаем Tag для EditText
            viewHolder = (ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvDigInNumber.setTag(R.id.tvDigInNumber, position);
            viewHolder.etDigInName.setTag(R.id.etDigInName, position);
            viewHolder.ivDigInStatus.setTag(R.id.ivDigInStatus, position);
            viewHolder.swDigInState.setTag(R.id.swDigInState, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvDigInNumber.setText(activity.mDigInNumber.get(position));
            viewHolder.etDigInName.setText(activity.mDigInName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivDigInStatus.setImageResource(Utils.getImageViewValue(activity.mDigInStatus, position));
            // устанавливаем значение переключателя
            viewHolder.swDigInState.setChecked(activity.mDigInState.get(position));
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
            // фокус был потерян, нужно сохранить новое значение EditText в mDigInName
            int etActiveNumber = (int) etActive.getTag(R.id.etDigInName);
            activity.mDigInName.set(etActiveNumber, etActive.getText().toString());
        }
    }
}
