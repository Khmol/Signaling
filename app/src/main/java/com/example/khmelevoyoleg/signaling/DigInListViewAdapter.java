package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DigInListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, SeekBar.OnSeekBarChangeListener{

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
/*
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
*/

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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        /*
        // получаем SeekBar
        // SeekBar sbActive = seekBar;
        // получаем номер данного SwitchCompat
        int swNumber = (int) seekBar.getTag(R.id.sbDigInState);
        // запоминаем новое значение переключателя (входа)
        // проверяем изменилось ли состояние переключателя относительно сохраненного значения
        switch (progress) {
            // выключено
            case 0:
                if ( activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_OFF, (swNumber + 1)), 0);
                    activity.mDigInTimeOff.set(swNumber, 0);
                    activity.mDigInState.set(swNumber, false);
                }
                break;
            case 1:
                if ( !activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_ON, (swNumber + 1)), 0);
                    activity.mDigInTimeOff.set(swNumber, 0);
                    activity.mDigInState.set(swNumber, true);
                }
                break;
            case 2:
                int time_off = 10;
                if ( !activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) == 0) {
                    activity.sendDataBT(String.format("%s%d,%d\r", Utils.IN_OFF_TIME, (swNumber + 1), time_off), 0);
                    activity.mDigInTimeOff.set(swNumber, time_off);
                    activity.mDigInState.set(swNumber, true);
                }
                break;
        }
        */
        /*
        if (activity.mDigInState.get(swNumber) == progress) {
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
        */
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int swNumber = (int) seekBar.getTag(R.id.sbDigInState);
        int progress = seekBar.getProgress();
        // запоминаем новое значение переключателя (входа)
        // проверяем изменилось ли состояние переключателя относительно сохраненного значения
        switch (progress) {
            // выключено
            case 0:
                if ( activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_OFF, (swNumber + 1)), 0);
                    activity.mDigInTimeOff.set(swNumber, 0);
                    activity.mDigInState.set(swNumber, false);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32, null);
                    seekBar.setThumb(draw);
                }
                break;
            case 1:
                if ( !activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_ON, (swNumber + 1)), 0);
                    activity.mDigInTimeOff.set(swNumber, 0);
                    activity.mDigInState.set(swNumber, true);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                    seekBar.setThumb(draw);
                }
                break;
            case 2:
                int time_off = 10;
                if ( !activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) == 0) {
                    activity.sendDataBT(String.format("%s%d,%d\r", Utils.IN_OFF_TIME, (swNumber + 1), time_off), 0);
                    activity.mDigInTimeOff.set(swNumber, time_off);
                    activity.mDigInState.set(swNumber, true);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_blue32, null);
                    seekBar.setThumb(draw);
                }
                break;
        }
    }

    private static class ViewHolder {
        TextView tvDigInNumber; // номер входа в списке
        EditText etDigInName; // имя входа в списке
        ImageView ivDigInStatus; // состояние входа в списке
        SeekBar sbDigInState; // состояние входа - вкл/выкл/
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
            viewHolder.sbDigInState = (SeekBar) convertView.findViewById(R.id.sbDigInState);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvDigInNumber.setTag(R.id.tvDigInNumber, position);
            viewHolder.etDigInName.setTag(R.id.etDigInName, position);
            viewHolder.ivDigInStatus.setTag(R.id.ivDigInStatus, position);
            viewHolder.sbDigInState.setTag(R.id.sbDigInState, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etDigInName.setOnFocusChangeListener(this);
            viewHolder.sbDigInState.setOnSeekBarChangeListener(this);
            viewHolderList.add(viewHolder);
        } else {
            // задаем Tag для EditText
            viewHolder = (ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvDigInNumber.setTag(R.id.tvDigInNumber, position);
            viewHolder.etDigInName.setTag(R.id.etDigInName, position);
            viewHolder.ivDigInStatus.setTag(R.id.ivDigInStatus, position);
            viewHolder.sbDigInState.setTag(R.id.sbDigInState, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvDigInNumber.setText(activity.mDigInNumber.get(position));
            viewHolder.etDigInName.setText(activity.mDigInName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivDigInStatus.setImageResource(Utils.getImageViewValue(activity.mDigInStatus, position));
            // устанавливаем значение переключателя
            if (activity.mDigInState.get(position)) {
                if (activity.mDigInTimeOff.get(position) > 0) {
                    viewHolder.sbDigInState.setProgress(Utils.SB_DIG_IN_TIME_OFF);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_blue32, null);
                    viewHolder.sbDigInState.setThumb(draw);
                }
                else {
                    viewHolder.sbDigInState.setProgress(Utils.SB_DIG_IN_ON);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                    viewHolder.sbDigInState.setThumb(draw);
                }
            }
            else {
                viewHolder.sbDigInState.setProgress(Utils.SB_DIG_IN_OFF);
                Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32, null);
                viewHolder.sbDigInState.setThumb(draw);
            }

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
