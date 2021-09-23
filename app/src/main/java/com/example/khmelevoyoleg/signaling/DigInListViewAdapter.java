package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.ContextMenu;
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

class DigInListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, SeekBar.OnSeekBarChangeListener, View.OnCreateContextMenuListener{

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу
    private ArrayList<ViewHolder> viewHolderList;
    private ArrayList<String> oldDigInStatus;
    boolean editableName = false;
    private static int _minute;
    private static int _hour;

    DigInListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // создаем список если его нет
        viewHolderList = new ArrayList<>();
        oldDigInStatus = new ArrayList<>();

    }

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
        int len = activity.mDigInStatus.size();
        if (oldDigInStatus.size() != len) {
            oldDigInStatus.addAll(activity.mDigInStatus);
        }
    }

    /**
     * изменение картинки для входов если это нужно
     */
    boolean checkStatusPictureDigIn(ArrayList<String> digInStatus) {
        int len = digInStatus.size();
        if (viewHolderList.size() == 0)
            return false;
        else
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
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32_dark, null);
                    seekBar.setThumb(draw);
                }
                break;
            case 1: {
                if (!activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) != 0) {
                    activity.sendDataBT(String.format("%s%d\r", Utils.IN_ON, (swNumber + 1)), 0);
                    activity.mDigInTimeOff.set(swNumber, 0);
                    activity.mDigInState.set(swNumber, true);
                }
                Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                seekBar.setThumb(draw);
                break;
            }
            case 2:
                if ( !activity.mDigInState.get(swNumber) ||
                        activity.mDigInTimeOff.get(swNumber) == 0) {
                    // запрашиваем установку времени выключения входа
                    activity.showDialogIn(swNumber, activity.setDigInOffTime, Utils.DIG_IN);
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
        TextView tvDigInNumber; // номер входа в списке
        EditText etDigInName; // имя входа в списке
        ImageView ivDigInStatus; // состояние входа в списке
        SeekBar sbDigInState; // состояние входа - вкл/выкл/
        TextView tvDigInTimeOff; // время выключения входа
        TextView tvDigInDelayTime; // время задерэки обработки при постановке на охрану
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
            viewHolder.etDigInName.setOnCreateContextMenuListener(this);
            if (editableName)
                viewHolder.etDigInName.setFocusableInTouchMode(true);
            else {
                viewHolder.etDigInName.setFocusableInTouchMode(false);
                viewHolder.etDigInName.setFocusable(false);
            }
            viewHolder.ivDigInStatus = (ImageView) convertView.findViewById(R.id.ivDigInStatus);
            viewHolder.sbDigInState = (SeekBar) convertView.findViewById(R.id.sbDigInState);
            viewHolder.tvDigInTimeOff = (TextView) convertView.findViewById(R.id.tvDigInTimeOff);
            viewHolder.tvDigInDelayTime = (TextView) convertView.findViewById(R.id.tvDigInDelayTime);

            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvDigInNumber.setTag(R.id.tvDigInNumber, position);
            viewHolder.etDigInName.setTag(R.id.etDigInName, position);
            viewHolder.ivDigInStatus.setTag(R.id.ivDigInStatus, position);
            viewHolder.sbDigInState.setTag(R.id.sbDigInState, position);
            viewHolder.tvDigInTimeOff.setTag(R.id.tvDigInTimeOff, position);
            viewHolder.tvDigInDelayTime.setTag(R.id.tvDigInDelayTime, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etDigInName.setOnFocusChangeListener(this);
            viewHolder.sbDigInState.setOnSeekBarChangeListener(this);
            viewHolderList.add(viewHolder);
        }
        else {
            // задаем Tag для EditText
            viewHolder = (ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvDigInNumber.setTag(R.id.tvDigInNumber, position);
            viewHolder.etDigInName.setTag(R.id.etDigInName, position);
            if (editableName)
                viewHolder.etDigInName.setFocusableInTouchMode(true);
            else {
                viewHolder.etDigInName.setFocusableInTouchMode(false);
                viewHolder.etDigInName.setFocusable(false);
            }
            viewHolder.ivDigInStatus.setTag(R.id.ivDigInStatus, position);
            viewHolder.sbDigInState.setTag(R.id.sbDigInState, position);
            viewHolder.tvDigInTimeOff.setTag(R.id.tvDigInTimeOff, position);
            viewHolder.tvDigInDelayTime.setTag(R.id.tvDigInDelayTime, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvDigInNumber.setText(activity.mDigInNumber.get(position));
            viewHolder.etDigInName.setText(activity.mDigInName.get(position));
            // устанавливаем значение картинки
            viewHolder.ivDigInStatus.setImageResource(Utils.getImageViewValue(activity.mDigInStatus, position));
        }

        // устанавливаем значение переключателя и текстовых полей времени
        if (activity.checkAbilityTxBT()){
            // если подключение по BT есть
            viewHolder.sbDigInState.setEnabled(true);
            if (activity.mDigInState.get(position)) {
                if (activity.mDigInTimeOff.get(position) > 0) {
                    viewHolder.sbDigInState.setProgress(Utils.SB_IN_TIME_OFF);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_blue32, null);
                    viewHolder.sbDigInState.setThumb(draw);
                    // устанавливаем значение времени хх:хх
                    int _time = activity.mDigInTimeOff.get(position);
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
                    viewHolder.tvDigInTimeOff.setText(String.format("%s:%s", _textHour, _textMinute));
                }
                else {
                    viewHolder.sbDigInState.setProgress(Utils.SB_IN_ON);
                    Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_green32, null);
                    viewHolder.sbDigInState.setThumb(draw);
                    // значение текста для времени - пусто
                    viewHolder.tvDigInTimeOff.setText("");
                }
            }
            else {
                viewHolder.sbDigInState.setProgress(Utils.SB_IN_OFF);
                Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32_dark, null);
                viewHolder.sbDigInState.setThumb(draw);
                // значение текста для времени - пусто
                viewHolder.tvDigInTimeOff.setText("");
            }
            // значение текста для времени - по значению в mDigInDelayTime
            if (activity.mDigInDelayTime.get(position) > 0) {
                String textTime;
                int _time = activity.mDigInDelayTime.get(position);
                if (_time <= 60) {
                    textTime = String.format("%dc",_time);
                }
                else {
                    textTime = String.format("%dм",(_time / 60));
                }
                viewHolder.tvDigInDelayTime.setText(textTime);
            }
            else
                viewHolder.tvDigInDelayTime.setText(""); // или пусто если значение 0
        }
        else {
            // подключения нет, все переключатели не активны
            Drawable draw = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle_grey32, null);
            viewHolder.sbDigInState.setThumb(draw);
            viewHolder.sbDigInState.setEnabled(false);
            // значение текста для времени - пусто
            viewHolder.tvDigInTimeOff.setText("");
            viewHolder.tvDigInDelayTime.setText("");
        }
        return convertView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        switch (v.getId()) {
            case R.id.etDigInName:
                menu.add(0, activity.EDIT_NAME_DIG_IN, 0, "Редактировать");
                break;
        }
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
