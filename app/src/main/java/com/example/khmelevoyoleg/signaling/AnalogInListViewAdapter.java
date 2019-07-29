package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AnalogInListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, SeekBar.OnSeekBarChangeListener{

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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int swNumber = (int) seekBar.getTag(R.id.sbAnalogInState);
        int progress = seekBar.getProgress();
        // запоминаем новое значение переключателя (входа)
        // проверяем изменилось ли состояние переключателя относительно сохраненного значения
        switch (progress) {
            // выключено
            case 0:
                if ( activity.mAnalogInState.get(swNumber) ||
                        activity.mAnalogInTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.ADC_IN_OFF, (swNumber + 1)), 0);
                    activity.mAnalogInTimeOff.set(swNumber, 0);
                    activity.mAnalogInState.set(swNumber, false);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32, null);
                    seekBar.setThumb(draw);
                }
                break;
            case 1: {
                if (!activity.mAnalogInState.get(swNumber) ||
                        activity.mAnalogInTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.ADC_IN_ON, (swNumber + 1)), 0);
                    activity.mAnalogInTimeOff.set(swNumber, 0);
                    activity.mAnalogInState.set(swNumber, true);
                }
                Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                seekBar.setThumb(draw);
                break;
            }
            case 2:
                if ( !activity.mAnalogInState.get(swNumber) ||
                        activity.mAnalogInTimeOff.get(swNumber) == 0) {
                    // запрашиваем установку времени выключения входа
                    activity.showDialogIn(swNumber, activity.setAnalogInOffTime, true);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_blue32, null);
                    seekBar.setThumb(draw);
                }
                break;
        }
    }

    private static class ViewHolder {
        TextView tvAnalogInNumber; // номер входа в списке
        TextView tvAnalogInTimeOff; // время на которое выключен вход
        TextView tvAnalogInDelayTime; // время на которое выключен вход
        EditText etAnalogInName; // имя входа в списке
        ImageView ivAnalogInStatus; // состояние входа в списке
        SeekBar sbAnalogInState; // состояние входа - вкл/выкл/выкл по времени
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
            viewHolder.sbAnalogInState = (SeekBar) convertView.findViewById(R.id.sbAnalogInState);
            viewHolder.tvAnalogInTimeOff = (TextView) convertView.findViewById(R.id.tvAnalogInTimeOff);
            viewHolder.tvAnalogInDelayTime = (TextView) convertView.findViewById(R.id.tvAnalogInDelayTime);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvAnalogInNumber.setTag(R.id.tvAnalogInNumber, position);
            viewHolder.etAnalogInName.setTag(R.id.etAnalogInName, position);
            viewHolder.ivAnalogInStatus.setTag(R.id.ivAnalogInStatus, position);
            viewHolder.sbAnalogInState.setTag(R.id.sbAnalogInState, position);
            viewHolder.tvAnalogInTimeOff.setTag(R.id.tvAnalogInTimeOff, position);
            viewHolder.tvAnalogInDelayTime.setTag(R.id.tvAnalogInDelayTime, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etAnalogInName.setOnFocusChangeListener(this);
            viewHolder.sbAnalogInState.setOnSeekBarChangeListener(this);
            // устанавливаем значение картинки состояния входа
            viewHolder.ivAnalogInStatus.setImageResource(Utils.getImageViewValue(activity.mAnalogInStatus, position));
            viewHolderList.add(viewHolder);
        }
        else {
            // задаем Tag для EditText
            viewHolder = (AnalogInListViewAdapter.ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvAnalogInNumber.setTag(R.id.tvAnalogInNumber, position);
            viewHolder.etAnalogInName.setTag(R.id.etAnalogInName, position);
            viewHolder.ivAnalogInStatus.setTag(R.id.ivAnalogInStatus, position);
            viewHolder.sbAnalogInState.setTag(R.id.sbAnalogInState, position);
            viewHolder.tvAnalogInTimeOff.setTag(R.id.tvAnalogInTimeOff, position);
            viewHolder.tvAnalogInDelayTime.setTag(R.id.tvAnalogInDelayTime, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvAnalogInNumber.setText(activity.mAnalogInNumber.get(position));
            viewHolder.etAnalogInName.setText(activity.mAnalogInName.get(position));
        }

        // устанавливаем значение переключателя и текстовых полей времени
        if (activity.checkAbilityTxBT()){
            // если подключение по BT есть
            viewHolder.sbAnalogInState.setEnabled(true);
            if (activity.mAnalogInState.get(position)) {
                if (activity.mAnalogInTimeOff.get(position) > 0) {
                    viewHolder.sbAnalogInState.setProgress(Utils.SB_IN_TIME_OFF);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_blue32, null);
                    viewHolder.sbAnalogInState.setThumb(draw);
                    // устанавливаем значение времени хх:хх
                    int _time = activity.mAnalogInTimeOff.get(position);
                    int _hour = _time / 60;
                    int _minute = _time % 60;
                    String _textHour;
                    if (_hour <= 9 )
                        _textHour = "0" + Integer.toString(_hour);
                    else
                        _textHour = Integer.toString(_hour);
                    String _textMinute;
                    if (_minute <= 9 )
                        _textMinute = "0" + Integer.toString(_minute);
                    else
                        _textMinute = Integer.toString(_minute);
                    viewHolder.tvAnalogInTimeOff.setText(String.format("%s:%s", _textHour, _textMinute));
                }
                else {
                    viewHolder.sbAnalogInState.setProgress(Utils.SB_IN_ON);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                    viewHolder.sbAnalogInState.setThumb(draw);
                    // значение текста для времени - пусто
                    viewHolder.tvAnalogInTimeOff.setText("");
                }
            }
            else {
                viewHolder.sbAnalogInState.setProgress(Utils.SB_IN_OFF);
                Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32, null);
                viewHolder.sbAnalogInState.setThumb(draw);
                // значение текста для времени - пусто
                viewHolder.tvAnalogInTimeOff.setText("");
            }
            // значение текста для времени - по значению в mAnalogInDelayTime
            if (activity.mAnalogInDelayTime.get(position) > 0) {
                String textTime;
                int _time = activity.mAnalogInDelayTime.get(position);
                if (_time <= 60) {
                    textTime = String.format("%dc", _time);
                } else {
                    textTime = String.format("%dм", (_time / 60));
                }
                viewHolder.tvAnalogInDelayTime.setText(textTime);
            }
            else
                viewHolder.tvAnalogInDelayTime.setText(""); // или пусто если значение 0
        }
        else {
            // подключения нет, все переключатели не активны
            Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32, null);
            viewHolder.sbAnalogInState.setThumb(draw);
            viewHolder.sbAnalogInState.setEnabled(false);
            // значение текста для времени - пусто
            viewHolder.tvAnalogInTimeOff.setText("");
            viewHolder.tvAnalogInDelayTime.setText("");
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
