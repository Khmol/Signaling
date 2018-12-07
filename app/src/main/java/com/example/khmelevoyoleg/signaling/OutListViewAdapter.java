package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

class OutListViewAdapter extends SimpleAdapter
        implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {

    private final static int TIMER_ON_BUTTON = 1000;  // время отжатия кнопкок
    private Handler timerHandler;
    private ImageView[] pressedButton;  // список нажатых кнопок
    private boolean[] scOutState;     // список состояния переключателей включения выходов
    private final short BUTTON_COUNT = 10; // количество одновременно нажатых кнопок за 1 с.

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

    /**
     * анализ состояний программы
     */
    private Runnable runCheckStatus = new Runnable() {
        @Override
        public void run() {
            setButtonImage();
        }
    };

    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    OutListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // запускаем таймер просмотра состояний программы
        timerHandler = new Handler();
        pressedButton = new ImageView[BUTTON_COUNT];
    }

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
    }

    @Override
    public void onClick(View v) {
        // получаем SwitchCompat
        ImageView ivActive = (ImageView) v;
        // получаем номер данного SwitchCompat
        int ivNumber = (int) ivActive.getTag(R.id.ivOutTimeSwitch);
        View parent = (View) ivActive.getParent();
        ViewHolder vh = (ViewHolder) parent.getTag();
        // обрабатываем нажатие только в случае выключенного swOutState
        if ( ! vh.swOutState.isChecked()) {
            // передаем данные если возможна передача
            if (activity.checkAbilityTxBT()) {
                // activity.sendDataBT(activity.OUT_ON + Integer.toString(swNumber + 1), 0);
                activity.sendDataBT(String.format("%s%d\r", Utils.OUT_ON_TIME, (ivNumber + 1)), 0);
                ivActive.setImageResource(R.drawable.circle_grey32_dark);
                // вызываем runCheckStatus с задержкой 100 мс.
                timerHandler.postDelayed(runCheckStatus, TIMER_ON_BUTTON);
                ivActive.setImageResource(R.drawable.circle_grey32_dark);
                int i = 0;
                do {
                    if (pressedButton[i] == null){
                        pressedButton[i] = ivActive;
                        break;
                    }
                    i++;
                } while (i < BUTTON_COUNT);
                String text = activity.getString(R.string.outOnTimeBegin) +
                        Integer.toString(ivNumber + 1) +
                        activity.getString(R.string.outOnTimeEnd);
                // выдаем текстовое оповещение с номером вкюченного выхода
                Toast toast = Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP,0,20);
                toast.show();
            }
            else {
                // выдаем текстовое оповещение что соединение отсутствует
                Toast.makeText(activity.getApplicationContext(),
                        R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class ViewHolder {
        TextView tvOutNumber; // номер выхода в списке
        EditText etOutName; // имя выхода в списке
        SwitchCompat swOutState; // состояние выхода - вкл/выкл
        ImageView ivOutTimeSwitch;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // получаем View
        ViewHolder viewHolder;
        // mAlOutStatus
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            viewHolder = new ViewHolder();
            viewHolder.tvOutNumber = (TextView) convertView.findViewById(R.id.tvOutNumber);
            viewHolder.etOutName = (EditText) convertView.findViewById(R.id.etOutName);
            viewHolder.swOutState = (SwitchCompat) convertView.findViewById(R.id.swOutState);
            viewHolder.ivOutTimeSwitch = (ImageView) convertView.findViewById(R.id.ivOutTimeSwitch);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            setTagToItem(viewHolder, position);
            // для EditText задаем обработчик изменения фокуса
            viewHolder.etOutName.setOnFocusChangeListener(this);
            viewHolder.swOutState.setOnCheckedChangeListener(this);
            viewHolder.ivOutTimeSwitch.setOnClickListener(this);
        } else {
            // задаем Tag для EditText
            viewHolder = (ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            setTagToItem(viewHolder, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvOutNumber.setText(activity.mOutNumber.get(position));
            viewHolder.etOutName.setText(activity.mOutName.get(position));
            // устанавливаем значение переключателя
            viewHolder.swOutState.setChecked(activity.mOutState.get(position));
        }
        return convertView;
    }

    private void setTagToItem (ViewHolder viewHolder, int position) {
        viewHolder.tvOutNumber.setTag(R.id.tvOutNumber, position);
        viewHolder.etOutName.setTag(R.id.etOutName, position);
        viewHolder.swOutState.setTag(R.id.swOutState, position);
        viewHolder.ivOutTimeSwitch.setTag(R.id.ivOutTimeSwitch, position);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // получаем EditText
        EditText etActive = (EditText) v;
        if (hasFocus){
            // фокус появился нужно вернуть фокус на данный EditText при обновлении окна
            etActive.requestFocusFromTouch();
        } else {
            // фокус был потерян, нужно сохранить новое значение EditText в etOutName
            int etActiveNumber = (int) etActive.getTag(R.id.etOutName);
            activity.mOutName.set(etActiveNumber, etActive.getText().toString());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // определяем список при первом влючении длиной равной mOutNumber
        if (scOutState == null) {
            scOutState = new boolean[activity.mOutNumber.size()];
        }
        // получаем SwitchCompat
        SwitchCompat swActive = (SwitchCompat) buttonView;
        // получаем номер данного SwitchCompat
        int swNumber = (int) swActive.getTag(R.id.swOutState);
        // отправляем команду на включение/ выключение реле
        // посылаем команду установить на охрану в 1-м режиме
        if (isChecked) {
            // передаем данные если возможна передача
            if (activity.checkAbilityTxBT()) {
                scOutState[swNumber] = true;
                activity.sendDataBT(String.format("%s%d\r", Utils.OUT_ON, (swNumber + 1)), 0);
                View parent = (View) swActive.getParent();
                ViewHolder vh = (ViewHolder) parent.getTag();
                vh.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32_off);
                // выдаем текстовое оповещение с номером вкюченного выхода
                String text = activity.getString(R.string.outOnTimeBegin) +
                        Integer.toString(swNumber + 1);
                Toast toast = Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP,0,20);
                toast.show();
            }
            else {
                scOutState[swNumber] = false;
                // выдаем текстовое оповещение что соединение отсутствует
                Toast.makeText(activity.getApplicationContext(),
                        R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            // передаем данные если возможна передача
            if (activity.checkAbilityTxBT()) {
                //activity.sendDataBT(activity.OUT_OFF + Integer.toString(swNumber + 1), 0);
                activity.sendDataBT(String.format("%s%d\r", Utils.OUT_OFF, (swNumber + 1)), 0);
                View parent = (View) swActive.getParent();
                ViewHolder vh = (ViewHolder) parent.getTag();
                vh.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32);
                // выдаем текстовое оповещение с номером вкюченного выхода
                String text = activity.getString(R.string.outOff) +
                        Integer.toString(swNumber + 1);
                Toast toast = Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP,0,20);
                toast.show();
            }
            else {
                // выдаем текстовое оповещение что соединение отсутствует
                Toast.makeText(activity.getApplicationContext(),
                        R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean[] getSwithCompactOutState () {
        return scOutState;
    }

    private void setButtonImage() {
        for(short i = 0; i < BUTTON_COUNT; i++){
            if (pressedButton[i] != null) {
                pressedButton[i].setImageResource(R.drawable.circle_grey32);
                pressedButton[i] = null;
            }
        }
    }
}
