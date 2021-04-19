package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu;
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
        View.OnClickListener, View.OnCreateContextMenuListener {

    private final static int TIMER_ON_BUTTON = 1000;  // время отжатия кнопкок
    private Handler timerHandler;
    private ImageView[] pressedButton;  // список нажатых кнопок
    private boolean[] scOutState;     // список состояния переключателей включения выходов
    private final short BUTTON_COUNT = 10; // количество одновременно нажатых кнопок за 1 с.
    private String toastText;   // текст который будет выводиться в Toast
    private short toastActive;
    boolean editableName = false;

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

    /**
     * анализ состояний программы
     */
    private Runnable runCheckStatus = new Runnable() {
        @Override
        public void run() {
            //setButtonImage();
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
        // увеличиваем время между запросами, чтобы исключить ложные срабатывания
        activity.numberPass = 0;
        // получаем SwitchCompat
        ImageView ivActive = (ImageView) v;
        // получаем номер данного SwitchCompat
        int ivNumber = (int) ivActive.getTag(R.id.ivOutTimeSwitch);
        View parent = (View) ivActive.getParent();
        ViewHolder vh = (ViewHolder) parent.getTag();
        // обрабатываем нажатие только в случае выключенного swOutState
        if (!vh.swOutState.isChecked()) {
            // передаем данные если возможна передача
            if (activity.checkAbilityTxBT()) {
                // activity.sendDataBT(activity.OUT_ON + Integer.toString(swNumber + 1), 0);
                activity.sendDataBT(String.format("%s%d\r", Utils.OUT_ON_TIME, (ivNumber + 1)), 0);
                // вызываем runCheckStatus с задержкой 100 мс.
                //timerBTHandler.postDelayed(runCheckStatus, TIMER_ON_BUTTON);
                ivActive.setImageResource(R.drawable.circle_grey32_dark);
                int i = 0;
                do {
                    if (pressedButton[i] == null) {
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
                toast.setGravity(Gravity.TOP, 0, 20);
                toast.show();
            } else {
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
            if (editableName)
                viewHolder.etOutName.setFocusableInTouchMode(true);
            else {
                viewHolder.etOutName.setFocusableInTouchMode(false);
                viewHolder.etOutName.setFocusable(false);
            }
            viewHolder.etOutName.setOnCreateContextMenuListener(this);
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
            if (editableName)
                viewHolder.etOutName.setFocusableInTouchMode(true);
            else {
                viewHolder.etOutName.setFocusableInTouchMode(false);
                viewHolder.etOutName.setFocusable(false);
            }
        }
        // устанавливаем значение переключателя и текстовых полей времени
        if (activity.checkAbilityTxBT()) {
            // если подключение по BT есть
            viewHolder.swOutState.setEnabled(true);
            // устанавливаем значение переключателя
            viewHolder.swOutState.setChecked(activity.mOutState.get(position));
            // проверяем находится ли выход в режиме сработал по времени
            if (activity.mOutTimeOnState.get(position)) {
                // сработал, нужно отобразить его темным цветом
                viewHolder.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32_aver);
            }
            else {
                if (activity.mOutState.get(position)) {
                    // не сработал, нужно отобразить его светлым цветом
                    viewHolder.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32_aver);
                }
                else {
                    // не сработал, нужно отобразить его светлым цветом
                    viewHolder.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32);
                }
            }
        } else {
            // если подключения по BT нет
            viewHolder.swOutState.setEnabled(false);
            viewHolder.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32);
        }
        return convertView;
    }

    private void setTagToItem(ViewHolder viewHolder, int position) {
        viewHolder.tvOutNumber.setTag(R.id.tvOutNumber, position);
        viewHolder.etOutName.setTag(R.id.etOutName, position);
        viewHolder.swOutState.setTag(R.id.swOutState, position);
        viewHolder.ivOutTimeSwitch.setTag(R.id.ivOutTimeSwitch, position);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        switch (v.getId()) {
            case R.id.etOutName:
                menu.add(0, activity.EDIT_NAME_DIG_OUT, 0, "Редактировать");
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
        if (isChecked) {
            // передаем данные если возможна передача
            if (activity.checkAbilityTxBT()) {
                // выдаем текстовое оповещение о включении с номером вкюченного выхода
                // только в том случае когда изменения были сделаны руками путем переключения
                if (scOutState[swNumber] == activity.mOutState.get(swNumber))
                    // выдаем сообщение только для выключенного выхода
                    if ( ! scOutState[swNumber]) {
                        addToast(swNumber, activity.getString(R.string.outOnTimeBegin));
                        // отправляем команду по BT
                        activity.sendDataBT(String.format("%s%d\r", Utils.OUT_ON, (swNumber + 1)), 0);
                        // меняем индикацию выхода
                        View parent = (View) swActive.getParent();
                        ViewHolder vh = (ViewHolder) parent.getTag();
                        vh.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32_aver);
                    }
                // устанавливаем переключатель
                scOutState[swNumber] = true;
                // изменяем статус для данного входа
                activity.mOutState.set(swNumber, true);
            }
            else {
                // устанавливаем переключатель
                scOutState[swNumber] = false;
                // изменяем статус для данного входа
                activity.mOutState.set(swNumber, false);
                // выдаем текстовое оповещение что соединение отсутствует
                Toast.makeText(activity.getApplicationContext(),
                        R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            // передаем данные если возможна передача
            if (activity.checkAbilityTxBT()) {
                // выдаем текстовое оповещение с номером вкюченного выхода
                // только в том случае когда изменения были сделаны руками путем переключения
                if (scOutState[swNumber] == activity.mOutState.get(swNumber))
                    // выдаем сообщение только для включенного выхода
                    if (scOutState[swNumber]) {
                        addToast(swNumber, activity.getString(R.string.outOff));
                        // отправляем команду по BT
                        activity.sendDataBT(String.format("%s%d\r", Utils.OUT_OFF, (swNumber + 1)), 0);
                        // меняем индикацию выхода
                        View parent = (View) swActive.getParent();
                        ViewHolder vh = (ViewHolder) parent.getTag();
                        vh.ivOutTimeSwitch.setImageResource(R.drawable.circle_grey32);
                    }
                // устанавливаем переключатель
                scOutState[swNumber] = false;
                // изменяем статус для данного входа
                activity.mOutState.set(swNumber, false);


            }
            else {
                // выдаем текстовое оповещение что соединение отсутствует
                Toast.makeText(activity.getApplicationContext(),
                        R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    public boolean[] getSwithCompactOutState() {
        return scOutState;
    }

    private void setButtonImage() {
        for (short i = 0; i < BUTTON_COUNT; i++) {
            if (pressedButton[i] != null) {
                pressedButton[i].setImageResource(R.drawable.circle_grey32);
                pressedButton[i] = null;
            }
        }
    }


    /**
     * запуск отображения Toast
     */
    private void runToast() {
        if (toastActive > 1) {
            Toast toast = Toast.makeText(activity.getApplicationContext(), toastText, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 20);
            toast.show();
        }
        toastText = null;
        toastActive = 0;
    }

    /**
     * формирование текста для Toast
     */
    private void addToast(int number, String startText) {
        if (toastText != null)
            toastText = toastText + '\n' + startText + Integer.toString(number + 1);
        else
            toastText = startText + Integer.toString(number + 1);
        if (toastActive == 0) {
            Toast toast = Toast.makeText(activity.getApplicationContext(), toastText, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP,0,20);
            toast.show();
            timerHandler.postDelayed(runToast, Utils.TIMER_TOAST);
            toastActive ++;
            toastText = null;
        }
        else {
            toastActive ++;
        }
    }
    /**
     * запуск отображения Toast
     */
    Runnable runToast = new Runnable() {
        @Override
        public void run() {
            runToast();
        }
    };
}
