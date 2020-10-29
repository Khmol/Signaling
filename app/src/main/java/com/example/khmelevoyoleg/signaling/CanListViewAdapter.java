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

class CanListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, SeekBar.OnSeekBarChangeListener{

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу
    private ArrayList<CanListViewAdapter.ViewHolder> viewHolderList;
    private ArrayList<String> oldCanStatus;
    private static int _minute;
    private static int _hour;

    CanListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // создаем список если его нет
        viewHolderList = new ArrayList<>();
        oldCanStatus = new ArrayList<>();
    }

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
    }

    /**
     * изменение картинки для входов если это нужно
     */
    void checkStatusPictureCan(ArrayList<String> canStatus) {
        int len = canStatus.size();
        if (oldCanStatus.size() != len) {
            for (String curStatus : canStatus) {
                oldCanStatus.add(curStatus);
            }
        }
        for (int i = 0; i < len; i++) {
            // проверяем изменился ли статус входа
            if ( !canStatus.get(i).equals(oldCanStatus.get(i))) {
                // изменился
                for(CanListViewAdapter.ViewHolder viewHolder: viewHolderList) {
                    int pos = (int) viewHolder.ivCanStatus.getTag(R.id.ivCanStatus);
                    if (pos == i) {
                        // устанавливаем значение картинки
                        viewHolder.ivCanStatus.setImageResource(Utils.getImageViewValue(activity.mCanStatus, i));
                    }
                }
                oldCanStatus.set(i, canStatus.get(i));
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int swNumber = (int) seekBar.getTag(R.id.sbCanState);
        int progress = seekBar.getProgress();
        // запоминаем новое значение переключателя (входа)
        // проверяем изменилось ли состояние переключателя относительно сохраненного значения
        switch (progress) {
            // выключено
            case 0:
                if ( activity.mCanState.get(swNumber) ||
                        activity.mCanTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_CAN_OFF, (swNumber + 1)), 0);
                    activity.mCanTimeOff.set(swNumber, 0);
                    activity.mCanState.set(swNumber, false);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32_dark, null);
                    seekBar.setThumb(draw);
                }
                break;
            case 1: {
                if (!activity.mCanState.get(swNumber) ||
                        activity.mCanTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_CAN_ON, (swNumber + 1)), 0);
                    activity.mCanTimeOff.set(swNumber, 0);
                    activity.mCanState.set(swNumber, true);
                }
                Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                seekBar.setThumb(draw);
                break;
            }
            case 2:
                if ( !activity.mCanState.get(swNumber) ||
                        activity.mCanTimeOff.get(swNumber) == 0) {
                    // запрашиваем установку времени выключения входа
                    activity.showDialogIn(swNumber, activity.setCanOffTime, Utils.CAN);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_blue32, null);
                    seekBar.setThumb(draw);
                }
                break;
        }
    }

    static void setTime(int hour, int minute) {
        _hour = hour;
        _minute = minute;
    }

    private static class ViewHolder {
        TextView tvCanNumber; // номер входа в списке
        EditText etCanName; // имя входа в списке
        ImageView ivCanStatus; // состояние входа в списке
        SeekBar sbCanState; // состояние входа - вкл/выкл/
        TextView tvCanTimeOff; // время выключения входа
        TextView tvCanDelayTime; // время задерэки обработки при постановке на охрану
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // получаем View
        CanListViewAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            // создаем ViewHolder
            viewHolder = new CanListViewAdapter.ViewHolder();
            viewHolder.tvCanNumber = (TextView) convertView.findViewById(R.id.tvCanNumber);
            viewHolder.etCanName = (EditText) convertView.findViewById(R.id.etCanName);
            viewHolder.ivCanStatus = (ImageView) convertView.findViewById(R.id.ivCanStatus);
            viewHolder.sbCanState = (SeekBar) convertView.findViewById(R.id.sbCanState);
            viewHolder.tvCanTimeOff = (TextView) convertView.findViewById(R.id.tvCanTimeOff);
            viewHolder.tvCanDelayTime = (TextView) convertView.findViewById(R.id.tvCanDelayTime);

            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvCanNumber.setTag(R.id.tvCanNumber, position);
            viewHolder.etCanName.setTag(R.id.etCanName, position);
            viewHolder.ivCanStatus.setTag(R.id.ivCanStatus, position);
            viewHolder.sbCanState.setTag(R.id.sbCanState, position);
            viewHolder.tvCanTimeOff.setTag(R.id.tvCanTimeOff, position);
            viewHolder.tvCanDelayTime.setTag(R.id.tvCanDelayTime, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etCanName.setOnFocusChangeListener(this);
            viewHolder.sbCanState.setOnSeekBarChangeListener(this);
            viewHolderList.add(viewHolder);
        }
        else {
            // задаем Tag для EditText
            viewHolder = (CanListViewAdapter.ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvCanNumber.setTag(R.id.tvCanNumber, position);
            viewHolder.etCanName.setTag(R.id.etCanName, position);
            viewHolder.ivCanStatus.setTag(R.id.ivCanStatus, position);
            viewHolder.sbCanState.setTag(R.id.sbCanState, position);
            viewHolder.tvCanTimeOff.setTag(R.id.tvCanTimeOff, position);
            viewHolder.tvCanDelayTime.setTag(R.id.tvCanDelayTime, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvCanNumber.setText(activity.mCanNumber.get(position));
            viewHolder.etCanName.setText(activity.mCanName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivCanStatus.setImageResource(Utils.getImageViewValue(activity.mCanStatus, position));
        }

        // устанавливаем значение переключателя и текстовых полей времени
        if (activity.checkAbilityTxBT()){
            // если подключение по BT есть
            viewHolder.sbCanState.setEnabled(true);
            if (activity.mCanState.get(position)) {
                if (activity.mCanTimeOff.get(position) > 0) {
                    viewHolder.sbCanState.setProgress(Utils.SB_IN_TIME_OFF);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_blue32, null);
                    viewHolder.sbCanState.setThumb(draw);
                    // устанавливаем значение времени хх:хх
                    int _time = activity.mCanTimeOff.get(position);
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
                    viewHolder.tvCanTimeOff.setText(String.format("%s:%s", _textHour, _textMinute));
                }
                else {
                    viewHolder.sbCanState.setProgress(Utils.SB_IN_ON);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                    viewHolder.sbCanState.setThumb(draw);
                    // значение текста для времени - пусто
                    viewHolder.tvCanTimeOff.setText("");
                }
            }
            else {
                viewHolder.sbCanState.setProgress(Utils.SB_IN_OFF);
                Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32_dark, null);
                viewHolder.sbCanState.setThumb(draw);
                // значение текста для времени - пусто
                viewHolder.tvCanTimeOff.setText("");
            }
            // значение текста для времени - по значению в mDigInDelayTime
            if (activity.mCanDelayTime.get(position) > 0) {
                String textTime;
                int _time = activity.mCanDelayTime.get(position);
                if (_time <= 60) {
                    textTime = String.format("%dc",_time);
                }
                else {
                    textTime = String.format("%dм",(_time / 60));
                }
                viewHolder.tvCanDelayTime.setText(textTime);
            }
            else
                viewHolder.tvCanDelayTime.setText(""); // или пусто если значение 0
        }
        else {
            // подключения нет, все переключатели не активны
            Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32, null);
            viewHolder.sbCanState.setThumb(draw);
            viewHolder.sbCanState.setEnabled(false);
            // значение текста для времени - пусто
            viewHolder.tvCanTimeOff.setText("");
            viewHolder.tvCanDelayTime.setText("");
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
            int etActiveNumber = (int) etActive.getTag(R.id.etCanName);
            activity.mCanName.set(etActiveNumber, etActive.getText().toString());
        }
    }
}
