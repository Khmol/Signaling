package com.example.khmelevoyoleg.signaling;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements View.OnTouchListener, View.OnClickListener,
        MediaPlayer.OnCompletionListener {

    // инициализация переменных
    //region InitVar
    // тип перечисления состояний по Bluetooth
    private enum ConnectionStatusBT {
        CONNECTING,
        NO_CONNECT,
        CONNECTED
    }

    private enum MainStatus {
        CLOSE,
        IDLE,
        CONNECTING,
        CONNECTED,
    }
    private enum SoundStatus {
        ALARM_ACTIVE,
        PREALARM_ACTIVE,
        IDLE,
        AFTER_ALARM,
        AFTER_PREALARM,
    }

    final String LOG_TAG = "myLogs";
    // определяем строковые константы

    private static final String RX_ERROR = "RX_ERROR" ;
    static final String SETTINGS_FILENAME = "Signaling";   // имя файла для хранения настроек
    static final String SELECTED_BOUNDED_DEV = "SELECTED_BOUNDED_DEV";   // выбранное спаренное устройство
    static final String PENDING = "PENDING";        // задача ожидает запуска
    static final String FINISHED = "FINISHED";      // задача завершена
    static final String BT_INIT_MESSAGE = "SIMCOMSPPFORAPP\r"; //SIMCOMSPPFORAPP посылка для инициализации SIM
    static final String SET_ALARM = "SET ALARM,1\r"; // посылка для установки на охрану
    static final String CLEAR_ALARM = "CLEAR ALARM,1\r"; // посылка для снятия с охраны
    static final String CLEAR_ALARM_TRIGGERED = "CLEAR ALARM TRIGGERED,1\r"; // посылка для снятия аварии с SIM
    static final String OUT_1_ON = "OUT ON,1\r"; // посылка для открытия 1-го выхода
    static final String RX_INIT_OK = "SPP APP OK\r"; // ответ на BT_INIT_MESSAGE
    static final String TYPE_INPUT = "INPUT,"; // тип команды в ответе от SIM
    private static final String AUTO_CONNECT = "AUTO_CONNECT";   // вкл/выкл автоматическое соединение

    // определяем числовые константы
    private static final int MAX_CONNECTION_ATTEMPTS = 3;   // максимальное количество попыток установления соединения
    private static final int REQUEST_ENABLE_BT = 1;     // запрос включения Bluetooth
    private static final int SET_SETTINGS = 2;          // редактирование настроек
    private static final long UUID_SERIAL = 0x1101;     // нужный UUID
    private static final long UUID_MASK = 0xFFFFFFFF00000000L;  // маска для извлечения нужных битов UUID
    private static final short POSITION_STATUS_SIM = 6; // начальная позиция флагов статуса в общем пакете
    private static final short POSITION_LATCH_IN = 11;  // начальная позиция флагов статуса защелки входов в общем пакете
    private static final short POSITION_CURRENT_IN = 37;  // начальная позиция флагов статуса текущего состояния входов в общем пакете
    private static final short POSITION_TYPE_DATA = 0; // начальная позиция описания типа команды
    private static final short MASK_GUARD = 1;  // маска для извлечения флага нахождения на охране
    private static final short MASK_ALARM = 512;  // маска для извлечения флага сработала авария
    private static final short MASK_ALARM_TRIGERED = 256;  // маска для извлечения флага сработала предварительная авария
    private static final int DELAY_TX_INIT_MESSAGE = 3; // задержка перед повторной передачей  INIT_MESSAGE
    private static final int TIMER_CHECK_STATUS = 400;  // периодичность вызова runCheckStatus
    private static final int DELAY_CONNECTING = 12;     // задержка перед повторной попыткой соединения по BT
    private static final int MAX_PROGRESS_VALUE = 3;    // количество ступеней в ProgressBar
    private static final int AUTO_CONNECT_TIMEOUT = 300;  // время между запуском поиска SIM 2 мин = TIMER_CHECK_STATUS * AUTO_CONNECT_TIMEOUT
    private static final long VIBRATE_TIME = 200;      //  длительность вибрации при нажатии кнопки

    // определяем стринговые переменные
    protected String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    public BluetoothSocket mClientSocket;
    ConnectionStatusBT mConnectionStatusBT; // состояние подключения по Bluetooth
    Handler timerHandler;       // обработчик таймера
    BTRx bluetooth_Rx;        // поток приема
    BTConnect bluetooth_Connect;   // задача установления соединения
    BTTx bluetooth_Tx;        // поток передачи

    int mConnectionAttemptsCnt = 0;         // счетчик попыток подключения по Bluetooth

    MainStatus mMainStatus; // состояние подключения по Bluetooth
    SoundStatus mSoundStatus; // состояние звукового оповещения
    OutputStream mOutStream;                 // поток по передаче bluetooth
    InputStream mInStream;                   // поток по приему bluetooth
    private FloatingActionButton fabConnect;        // кнопка поиска "Базового блока"
    private int fabConnectPicture = 1;
    private ViewFlipper flipper;                    // определяем flipper для перелистываний экрана
    private float fromPosition;                     // позиция касания при перелистывании экранов
    private TextView tvBtRxData;                    // текст с принятыми данными
    private Set<BluetoothDevice> pairedDevices;     // множество спаренных устройств
    private SharedPreferences sPref;                // настройки приложения
    int btRxCnt;                                    // счетчик принятых пакетов
    ImageButton ibOpen;                     // кнопка снятия с охраны
    boolean ibOpenPress;
    ImageButton ibClose;                    // кнопка установки на охрану
    boolean ibClosePress;
    ImageButton ibMute;                     // кнопка снятие активной аварии
    boolean ibMutePress;
    ImageButton ibBagage;                   // кнопка открытие 1-го выхода
    boolean ibBagagePress;
    ImageView ivSigState;                   // рисунок состояния сигнализации (на крыше)
    ProgressBar pbIBPress;                  // Индикация длятельности нажатия кнопок
    private int pbProgress;                 // счетчик длительности нажатия кнопок
    private int autoConnectCnt = AUTO_CONNECT_TIMEOUT; // счетчик времени между вызовами AutoConnect
    private boolean autoConnectFlag;        // флаг активности AutoConnect

    MediaPlayer mediaPlayer;
    AudioManager am;

    //endregion

    // приемник широковещательных событий
    // region BroadcastReceiver
    BroadcastReceiver connectionStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    // изменяем состояние BT - CONNECTED
                    mConnectionStatusBT = ConnectionStatusBT.CONNECTED;
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    if (mMainStatus != MainStatus.CLOSE  && mMainStatus != MainStatus.IDLE){
                        // если программа не в закрытии и не в исходном состоянии
                        // состояние - CONNECTING
                        mConnectionStatusBT = ConnectionStatusBT.CONNECTING;
                        // запускаем процесс установления соединения
                        mMainStatus = MainStatus.CONNECTING;
                    }
                    else {
                        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    if (mMainStatus != MainStatus.CLOSE) {
                        // переходим в исходное состояние
                        returnIdleState();
                    }
                    break;
            }
        }
    };
    // endregion

    /** обработка результатов намерений */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // проверяем результат какого намерения вернулся
        if (requestCode == REQUEST_ENABLE_BT) {
            returnIdleState();
        }
        else if(requestCode == SET_SETTINGS){
            if (data != null){
                // читаем значение флага AutoConnect
                autoConnectFlag = Boolean.parseBoolean(sPref.getString(AUTO_CONNECT, ""));
                //Toast.makeText(getApplicationContext(), data.getStringExtra("address"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flip);
        // определяем объект для работы с настройками
        sPref = getSharedPreferences(SETTINGS_FILENAME, MODE_PRIVATE);
        // определяем объекты для flipper
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        // Создаем View и добавляем их в уже готовый flipper
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layouts[] = new int[]{ R.layout.activity_main, R.layout.activity_in_out};
        for (int layout : layouts)
            flipper.addView(inflater.inflate(layout, null));

        // Устанавливаем listener касаний, для последующего перехвата жестов
        ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.main_layout);
        mainLayout.setOnTouchListener(this);
        // определяем toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // инициализация переменных
        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;    // вначале состояние NO_CONNECT
        mMainStatus = MainStatus.IDLE;  // вначале состояние IDLE
        // инициализация объектов для View
        fabConnect = (FloatingActionButton) findViewById(R.id.fabConnect);
        fabConnect.setOnClickListener(this);
        ibOpen = (ImageButton) findViewById(R.id.ibOpen);
        ibOpen.setOnTouchListener(this);
        ibClose = (ImageButton) findViewById(R.id.ibClose);
        ibClose.setOnTouchListener(this);
        ibMute = (ImageButton) findViewById(R.id.ibMute);
        ibMute.setOnTouchListener(this);
        ibBagage = (ImageButton) findViewById(R.id.ibBagage);
        ibBagage.setOnTouchListener(this);
        ivSigState = (ImageView) findViewById(R.id.ivSigState);
        tvBtRxData = (TextView) findViewById(R.id.tvBtRxData);
        pbIBPress = (ProgressBar) findViewById(R.id.pbIBPress);
        pbIBPress.setMax(MAX_PROGRESS_VALUE);
        pbIBPress.setProgress(0);

        // читаем значение флага AutoConnect
        autoConnectFlag = Boolean.parseBoolean(sPref.getString(AUTO_CONNECT, ""));

        // регистрация активностей
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));

        // определяем AudioManager
        am = (AudioManager) getSystemService(AUDIO_SERVICE);

        // определяем адаптер
        //region BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter != null)
        {
            // С Bluetooth все в порядке.
            if (!mBluetoothAdapter.isEnabled())
            {
                // Bluetooth выключен. Предложим пользователю включить его.
                startActivityForResult(new Intent(actionRequestEnable), REQUEST_ENABLE_BT);
            }
        }
        // создаем задачу соединения BTConnect
        bluetooth_Connect = new BTConnect();
        // передаем ссылку на основную activity
        bluetooth_Connect.link(this);
        // запускаем таймер просмотра состояний программы
        timerHandler = new Handler();
        timerHandler.postDelayed(runCheckStatus, TIMER_CHECK_STATUS);
        //endregion
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // инициализация меню
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabConnect:
                if (mMainStatus == MainStatus.IDLE){
                    // выводим сообщение "Запущен поиск сигнализации"
                    Toast.makeText(getApplicationContext(), R.string.connectionStart, Toast.LENGTH_SHORT).show();
                    mMainStatus = MainStatus.CONNECTING;   // переходим в установку соединения
                    mConnectionStatusBT = ConnectionStatusBT.CONNECTING;    // переходим в режим CONNECTING
                    setFabConnectColorBlue();   // синий цвет для кнопки FabConnect
                    // создаем асинхронную задачу соединения если прошлая уже отработала
                    if (bluetooth_Connect.getStatus().toString().equals(FINISHED)) {
                        bluetooth_Connect = new BTConnect();
                        // передаем ссылку на основную activity
                        bluetooth_Connect.link(this);
                    }
                    pbConnectHeader();     // запускаем установление соединения
                }
                else {
                    if (mMainStatus == MainStatus.CONNECTING){
                        // выводим сообщение "Поиск сигнализации остановлен"
                        Toast.makeText(getApplicationContext(), R.string.сonnectionStoped, Toast.LENGTH_SHORT).show();
                    }
                    else
                    if (mMainStatus == MainStatus.CONNECTED){
                        // выводим сообщение "Соединение разорвано"
                        Toast.makeText(getApplicationContext(), R.string.connectionInterrupted, Toast.LENGTH_SHORT).show();
                    }
                    // выключаем поиск сигнализации и переходим в исходное состояние
                    returnIdleState();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получаем код выбранного пункта меню
        int id = item.getItemId();
        // если выбран пункт меню "Настройки"
        if (id == R.id.action_settings) {
            // получаем список спаренных устройств
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            // новые списки имен и адресов спаренных устройств
            ArrayList<String> namePairedDevices = new ArrayList<>();
            ArrayList<String> adressPairedDevices = new ArrayList<>();
            // запускаем намерение
            Intent intent = new Intent(MainActivity.this, SigSettings.class);
            // передаем данные для активности IntentActivity
            if (pairedDevices.size() > 0) {
                int i = 0;
                for (BluetoothDevice device : pairedDevices) {
                    // читаем имена спаренных устройств
                    String name = device.getName();
                    namePairedDevices.add(i, name);
                    // читаем адреса спаренных устройств
                    String mac_adress = device.getAddress();
                    adressPairedDevices.add(i, mac_adress);
                    i++;    // переход к следующему устройству
                }
            }
            intent.putExtra("paired_names", namePairedDevices);
            intent.putExtra("paired_adresses", adressPairedDevices);
            // запускаем активность
            startActivityForResult(intent, SET_SETTINGS);
            return true;
        }
        // по умолчанию возвращаем обработчик родителя
        return super.onOptionsItemSelected(item);
    }

    /**
     * обработка нажатия кнопки fabConnect
     */
    private void pbConnectHeader(){
        fabConnect.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        // получаем список спаренных устройств
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        mConnectionAttemptsCnt++;       // увеличиваем счетчик попыток установления соединения
        try {
            UUID serialUUID;
            // проверяем включен ли Bluetooth
            if (mBluetoothAdapter.isEnabled()) {
                String selectedBoundedDevice = sPref.getString(SELECTED_BOUNDED_DEV, "");
                // проверим определено ли устройство для связи
                if (selectedBoundedDevice.equals("")) {
                    // переходим в исходное состояние
                    returnIdleState();
                    // выдаем сообщение "Устройство для связи не выбрано."
                    Toast.makeText(getApplicationContext(), R.string.noBoundedDevice, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Bluetooth включен, проверяем устанавливалось ли ранее соединение
                if (mClientSocket == null) {
                    // попытка установить соединение не производилась
                    // производим поиск нужного нам устройства, получаем его
                    mBluetoothDevice = findBTDevice(selectedBoundedDevice);
                    if (mBluetoothDevice != null) {
                        ParcelUuid[] uuids = mBluetoothDevice.getUuids();// получаем перечень доступных UUID данного устройства
                        if (uuids != null) {
                            serialUUID = new UUID(0, 0);
                            for (ParcelUuid uuid : uuids) {
                                long profileUUID = uuid.getUuid().getMostSignificantBits();
                                profileUUID = (profileUUID & UUID_MASK) >> 32;
                                if (profileUUID == UUID_SERIAL)
                                    serialUUID = uuid.getUuid();
                            }
                            // проверяем установлен ли UUID
                            if (serialUUID.getMostSignificantBits() != 0) {
                                // устанавливаем связь, создаем Socket
                                mClientSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(serialUUID);
                            } else {
                                // переходим в исходное состояние
                                returnIdleState();
                                // если получено NO_BOUNDED_DEVICE, выдать предупреждение
                                Toast.makeText(getApplicationContext(), R.string.noBoundedDevice, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                // запускаем задачу bluetooth_Connect если она не запущена
                if (bluetooth_Connect.getStatus().toString().equals(PENDING))
                    bluetooth_Connect.execute(0);
            }
            else {
                // Bluetooth выключен. Предложим пользователю включить его.
                startActivityForResult(new Intent(actionRequestEnable), REQUEST_ENABLE_BT);
            }
        } catch (IOException e) {
            if (mMainStatus == MainStatus.CLOSE) {
                // завершение работы программы
                // выводим сообщение "Соединение разорвано"
                Toast.makeText(getApplicationContext(),
                        R.string.connectionInterrupted, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     *  поиск устройства Bluetooth по имени
     *  params: String addressDev - адрес, который будет искаться в спаренных усройствах
     */
    @Nullable
    private BluetoothDevice findBTDevice (String addressDev) {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String address = device.getAddress();
                if (address.equals(addressDev)) {
                    return device;
                }
            }
        }
        return null;
    }

    // установка цвета для кнопки FloatingActionButton
    private void setFloatingActionButtonColors(FloatingActionButton fab, int primaryColor, int rippleColor) {
        int[][] states = {
                {android.R.attr.state_enabled},
                {android.R.attr.state_pressed},
        };

        int[] colors = {
                primaryColor,
                rippleColor,
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        fab.setBackgroundTintList(colorStateList);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        switch (view.getId()) {
            case R.id.ibOpen:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // Пользователь нажал на экран, т.е. начало движения
                        ibOpenPress = true;
                        ibOpen.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP: // Пользователь отпустил экран, т.е. окончание движения
                        ibOpenPress = false;
                        ibOpen.setPressed(false);
                        pbProgress = 0;
                        pbIBPress.setProgress(pbProgress);
                        break;
                }
                break;
            case R.id.ibClose:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // Пользователь нажал на экран, т.е. начало движения
                        ibClosePress = true;
                        ibClose.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP: // Пользователь отпустил экран, т.е. окончание движения
                        ibClosePress = false;
                        ibClose.setPressed(false);
                        pbProgress = 0;
                        pbIBPress.setProgress(pbProgress);
                        break;
                }
                break;
            case R.id.ibMute:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // Пользователь нажал на экран, т.е. начало движения
                        ibMutePress = true;
                        ibMute.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP: // Пользователь отпустил экран, т.е. окончание движения
                        ibMutePress = false;
                        ibMute.setPressed(false);
                        pbProgress = 0;
                        pbIBPress.setProgress(pbProgress);
                        break;
                }
                break;
            case R.id.ibBagage:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // Пользователь нажал на экран, т.е. начало движения
                        ibBagagePress = true;
                        ibBagage.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP: // Пользователь отпустил экран, т.е. окончание движения
                        ibBagagePress = false;
                        ibBagage.setPressed(false);
                        pbProgress = 0;
                        pbIBPress.setProgress(pbProgress);
                        break;
                }
                break;
            case R.id.main_layout:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // Пользователь нажал на экран, т.е. начало движения
                        // fromPosition - координата по оси X начала выполнения операции
                        fromPosition = event.getX();
                        break;
                    case MotionEvent.ACTION_UP: // Пользователь отпустил экран, т.е. окончание движения
                        float toPosition = event.getX();
                        if (fromPosition > toPosition) {
                            if ((fromPosition - toPosition) > 100) {
                                flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_next_in));
                                flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_next_out));
                                flipper.showNext();
                            }
                        } else if (fromPosition < toPosition) {
                            if ((toPosition - fromPosition) > 100) {
                                flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_in));
                                flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_out));
                                flipper.showPrevious();
                            }
                        }
                    default:
                        break;
                }
                break;
        }
        return true;
    }

    /**
     * анализ состояний программы
     */
    Runnable runCheckStatus = new Runnable() {
        @Override
        public void run() {
            checkStatus();
        }
    };

    private void checkStatus(){
        if (mMainStatus == MainStatus.IDLE & autoConnectFlag) {
            // включен автоматический поиск SIM - AutoConnect
            if (autoConnectCnt <= AUTO_CONNECT_TIMEOUT)
                autoConnectCnt++;
            else {
                autoConnectCnt = 0;
                // запускаем поиск SIM, нажимаем кнопку поиска
                onClick(fabConnect);
            }
        }
        else
            if (mMainStatus == MainStatus.CONNECTING) {
                // показываем что идет поиск сети путем изменения антенны на кнопке
                changePictureFabConnect();
                // проверка установлено ли соединение
                if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
                    // проверяем была ли уже передача ранее
                    if (bluetooth_Tx != null)
                        if ( bluetooth_Tx.getStatus().toString().equals(FINISHED)) {
                            // передаем повторно пакет BT_INIT_MESSAGE, если прошлый уже передан
                            sendDataBT(BT_INIT_MESSAGE, DELAY_TX_INIT_MESSAGE);
                            // вызываем runCheckStatus с задержкой 100 мс.
                            timerHandler.postDelayed(runCheckStatus, TIMER_CHECK_STATUS);
                            // снова запускаем прием данных
                            listenMessageBT();
                            return;
                        }
                }
                else {
                    // проверка завершилась ли прошлая задача установки соединения
                    if (bluetooth_Connect.getStatus().toString().equals(FINISHED)) {
                        // повторяем попытку соединения если количество попыток меньше MAX_CONNECTION_ATTEMPTS
                        if (mConnectionAttemptsCnt <= MAX_CONNECTION_ATTEMPTS) {
                            // запускаем задачу bluetooth_Connect
                            bluetooth_Connect = new BTConnect();
                            // передаем ссылку на основную activity
                            bluetooth_Connect.link(this);
                            // запускаем задачу с задержкой 12 с
                            bluetooth_Connect.execute(DELAY_CONNECTING);
                            // увеличиваем счетчик попыток установления соединения
                            mConnectionAttemptsCnt++;
                        } else {
                            // все попытки установки соединения закончились неудачей
                            returnIdleState();
                            // выводим сообщение "Ошибка установления связи"
                            Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            else
                if (mMainStatus != MainStatus.CLOSE){
                    if (ibOpenPress) {
                        pbIBPress.setProgress(++pbProgress);
                        if (pbProgress == MAX_PROGRESS_VALUE) {
                            ibOpenHeader();
                            Vibrate(VIBRATE_TIME);
                        }
                    }
                    else if (ibClosePress) {
                        pbIBPress.setProgress(++pbProgress);
                        if (pbProgress == MAX_PROGRESS_VALUE) {
                            ibCloseHeader();
                            Vibrate(VIBRATE_TIME);
                        }
                    }
                    else if (ibMutePress) {
                        pbIBPress.setProgress(++pbProgress);
                        if (pbProgress == MAX_PROGRESS_VALUE) {
                            ibMuteHeader();
                            Vibrate(VIBRATE_TIME);
                        }
                    }
                    else if (ibBagagePress) {
                        pbIBPress.setProgress(++pbProgress);
                        if (pbProgress == MAX_PROGRESS_VALUE) {
                            ibBaggageHeader();
                            Vibrate(VIBRATE_TIME);
                        }
                    }
                    // снова запускаем прием данных
                    listenMessageBT();
                    // вызываем runCheckStatus с задержкой 100 мс.
                    timerHandler.postDelayed(runCheckStatus, TIMER_CHECK_STATUS);
                    return;
                }
        // вызываем runCheckStatus с задержкой 100 мс.
        timerHandler.postDelayed(runCheckStatus, TIMER_CHECK_STATUS);
    }

    /**
     * Изменение рисунка на кнопке fabConnect во время поиска базы
     */
    private void changePictureFabConnect() {
        switch (fabConnectPicture) {
            case 1:
                fabConnect.setImageResource(R.drawable.antenna1);
                break;
            case 2:
                fabConnect.setImageResource(R.drawable.antenna2);
                break;
            case 3:
                fabConnect.setImageResource(R.drawable.antenna3);
                break;
        }
        if (fabConnectPicture == 3)
            fabConnectPicture = 1;
        else fabConnectPicture++;
    }

    private void Vibrate(Long time) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
    }

    /**
     * передача данных через Bluetooth
     * @param data - данные для передачи
     * @param delay - задержка перед передачей данных в секундах
     */
    private boolean sendDataBT(String data, int delay){
        // проверка завершилась ли прошлая задача передачи
        if ( bluetooth_Tx == null || bluetooth_Tx.getStatus().toString().equals(FINISHED)) {
            // передача завершилась, создаем новую задачу передачи
            bluetooth_Tx = new BTTx(data);
            // передаем ссылку на основную activity
            bluetooth_Tx.link(this);
            bluetooth_Tx.execute(delay);
            Log.d("MY_LOG", "Send: " + data);
            return true;
        }
        else {
            return false;
        }
    }

    // прием данных из Bluetooth
    private void listenMessageBT() {
        try {
            if (mClientSocket != null) {
                if (mInStream == null)
                    mInStream = mClientSocket.getInputStream();
                if (bluetooth_Rx == null || bluetooth_Rx.getStatus().toString().equals(FINISHED)) {
                    // создаем задачу BTRx
                    bluetooth_Rx = new BTRx();
                    // передаем ссылку на основную activity
                    bluetooth_Rx.link(this);
                    // запускаем задачу с задержкой 0 с
                    bluetooth_Rx.execute(0);
                    Log.d("MY_LOG", "Start recieve");
                }
            } else Log.d("MY_LOG", "Rx Error");
        }
        catch (IOException e) {
            returnIdleState();
            Log.d("MY_LOG", "BT_RX_INTERRUPT");
            Toast.makeText(getApplicationContext(),
                    "Прерывание приема", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * анализ принятых данных по BT
     * @param data - данные для анализа
     * @return - true/false
     */
    private boolean analiseRxData(String data){
        if (data.length() > POSITION_LATCH_IN){
            int startIndexStatusSIM = (data.indexOf(',') + 1);
            int endIndexStatusSIM = data.indexOf(',', startIndexStatusSIM);
            String strStatusSIM = data.substring(startIndexStatusSIM, endIndexStatusSIM);
            String strTypeData = data.substring(POSITION_TYPE_DATA, startIndexStatusSIM);
            try{
                if (strTypeData.equals(TYPE_INPUT)) {
                    // получаем значение статуса SIM модуля
                    int statusSIM = Integer.parseInt(strStatusSIM, 16);
                    // устанавливаем нужный рисунок на крыше машины
                    if ((statusSIM & MASK_GUARD) > 0) {
                        // устанавливаем рисунок - close_small "закрыто"
                        ivSigState.setImageResource(R.drawable.close_small);
                    } else {
                        // устанавливаем рисунок - close_small "открыто"
                        ivSigState.setImageResource(R.drawable.open_small);
                    }
                    // включаем/ выключаем звук аварии (предварительной аварии)
                    if ((statusSIM & MASK_ALARM) > 0) {
                        // сработала авария, включаем звук
                        Log.d(LOG_TAG, "start alarm");
                        if (mediaPlayer == null & mSoundStatus != SoundStatus.AFTER_ALARM) {
                            mediaPlayer = MediaPlayer.create(this, R.raw.alarm1);
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.start();
                            mSoundStatus = SoundStatus.AFTER_ALARM;
                        }
                    } else if ((statusSIM & MASK_ALARM_TRIGERED) > 0) {
                        // сработала предварительная авария, включаем звук
                        Log.d(LOG_TAG, "start pre_alarm");
                        if (mediaPlayer == null & mSoundStatus != SoundStatus.AFTER_PREALARM) {
                            mediaPlayer = MediaPlayer.create(this, R.raw.prealarm);
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.start();
                            mSoundStatus = SoundStatus.AFTER_PREALARM;
                        }
                    } else {
                        // авария не сработала либо была выключена, выключаем звук
                        Log.d(LOG_TAG, "stop media player");
                        mSoundStatus = SoundStatus.IDLE;
                        if (mediaPlayer == null)
                            return true;
                        mediaPlayer.stop();
                        mediaPlayer = null;
                    }
                    return true;
                }
            }
            catch (NumberFormatException e){
                Log.d("MY_LOG", "NO_DIGIT");
                return false;
            }
        }
        // выводим сообщение, "Соединение отсутствует"
        //Toast.makeText(getApplicationContext(), statusSIM, Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * обработка нажатия кнопки ibClose
     */
    private void ibCloseHeader(){
        if (mMainStatus == MainStatus.CONNECTED){
            // посылаем команду установить на охрану в 1-м режиме
            sendDataBT(SET_ALARM, 0);
        }
        else
            // выводим сообщение, "Соединение отсутствует"
            Toast.makeText(getApplicationContext(), R.string.connectionFailed, Toast.LENGTH_SHORT).show();
    }

    /**
     *  обработка нажатия кнопки ibOpen
     */
    private void ibOpenHeader(){
        if (mMainStatus == MainStatus.CONNECTED){
            // посылаем команду снять с охраны в 1-м режиме
            sendDataBT(CLEAR_ALARM, 0);
        }
        else
            // выводим сообщение, "Соединение отсутствует"
            Toast.makeText(getApplicationContext(), R.string.connectionFailed, Toast.LENGTH_SHORT).show();
    }

    /**
     *  обработка нажатия кнопки ibMute
     */
    private void ibMuteHeader(){
        if (mMainStatus == MainStatus.CONNECTED){
            // посылаем команду снять с охраны d 1-м режиме
            sendDataBT(CLEAR_ALARM_TRIGGERED, 0);
        }
        else
            // выводим сообщение, "Соединение отсутствует"
            Toast.makeText(getApplicationContext(), R.string.connectionFailed, Toast.LENGTH_SHORT).show();
    }

    /**
     *     обработка нажатия кнопки ibBagage
     */
    private void ibBaggageHeader(){
        if (mMainStatus == MainStatus.CONNECTED){
            // посылаем команду открыть 1-й выход
            sendDataBT(OUT_1_ON, 0);
        }
        else
            // выводим сообщение, "Соединение отсутствует"
            Toast.makeText(getApplicationContext(), R.string.connectionFailed, Toast.LENGTH_SHORT).show();
    }

    /**
     * возврат в исходное состояние
     */
    private void returnIdleState(){
        mMainStatus = MainStatus.IDLE;  // состояние IDLE
        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;    // состояние BT = NO_CONNECT
        setFabConnectColorRed();    // кнопка соединеия - красная
        mConnectionAttemptsCnt = 0; // счетчик попыток соединения в 0
        // устанавливаем рисунок - no_connect_small
        ivSigState.setImageResource(R.drawable.no_connect_small);
        closeBtStreams();   // закрываем все потоки
    }

    /**
     * установка зеленого цвета кнопки fabConnect
     */
    private void setFabConnectColorGreen(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setFloatingActionButtonColors(fabConnect,
                    getResources().getColor(R.color.colorGreen, null),
                    getResources().getColor(R.color.colorCarSame, null));
        }
        else {
            setFloatingActionButtonColors(fabConnect,
                    getResources().getColor(R.color.colorGreen),
                    getResources().getColor(R.color.colorCarSame));
        }
        fabConnect.setImageResource(R.drawable.antenna3);
    }

    /**
     * установка синего цвета кнопки fabConnect
     */
    private void setFabConnectColorBlue(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setFloatingActionButtonColors(fabConnect,
                    getResources().getColor(R.color.colorCarSame, null),
                    getResources().getColor(R.color.colorCarSame, null));
        }
        else {
            setFloatingActionButtonColors(fabConnect,
                    getResources().getColor(R.color.colorCarSame),
                    getResources().getColor(R.color.colorCarSame));
        }
    }

    /**
     * установка красного цвета кнопки fabConnect
     */
    private void setFabConnectColorRed(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setFloatingActionButtonColors(fabConnect,
                    getResources().getColor(R.color.colorRed, null),
                    getResources().getColor(R.color.colorCarSame, null));
        }
        else {
            setFloatingActionButtonColors(fabConnect,
                    getResources().getColor(R.color.colorRed),
                    getResources().getColor(R.color.colorCarSame));
        }
        fabConnect.setImageResource(R.drawable.antenna3);
    }

    /**
     * метод вызываемый в конце выполнения задачи bluetooth_Connect
     */
    void onPostExecuteBTConnect(String result){
        // если соединение активно
        if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
            // проверяем результат работы задачи bluetooth_Connect
            if (result.equals(BTConnect.CONNECTION_OK)) {
                // обнуляем счетчик попыток установления связи
                mConnectionAttemptsCnt = 0;
                // переходим к отправке посылки инициаизации, состояние CONNECTED
                sendDataBT(BT_INIT_MESSAGE, DELAY_TX_INIT_MESSAGE);
                listenMessageBT();
            }
        }
    }

    /**
     * метод вызываемый в конце выполнения задачи bluetooth_Rx
     */
    void onPostExecuteBTRx(String rxText){
        // если соединение активно
        if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
            // находимся в режиме MainStatus.CONNECTING ???
            if (mMainStatus == MainStatus.CONNECTING) {
                if (rxText.contains(RX_INIT_OK)){
                    // в принятых данных есть строка инициализации RX_INIT_OK
                    // изменяем основное состояние на CONNECTED
                    mMainStatus = MainStatus.CONNECTED;
                    // обнуляем счетчик принятых пакетов
                    btRxCnt = 0;
                    // установка зеленого цвета для кнопки FabConnect
                    setFabConnectColorGreen();
                }
            }
            // производим анализ полученных данных
            analiseRxData(rxText);
            // обновляем значение текста в окне
            if (!rxText.contains(RX_ERROR)){
                String text = String.valueOf(btRxCnt) + " " + rxText;
                tvBtRxData.setText(text);
                btRxCnt++;
                Log.d("MY_LOG", "Recieve:" + rxText);
            }
        }
    }

    /**
     * освобождаем медиаплеей
     */
    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
        mediaPlayer = null;
    }

    /**
     * закрытие потоков ввода вывода для BT
     */
    private void closeBtStreams(){
        try{
            mBluetoothDevice = null;
            if (bluetooth_Connect != null) {
                // завершаем задачу установки соединения
                bluetooth_Connect.cancel(true);
            }
            if (bluetooth_Tx != null) {
                // завершаем задачу передачи
                bluetooth_Tx.cancel(true);
            }
            if (bluetooth_Rx != null) {
                // завершаем задачу приема
                bluetooth_Rx.cancel(true);
            }
            // закрываем сокет и потоки ввода вывода
            if (mClientSocket != null) {
                mClientSocket.close();
                mClientSocket = null;
            }
            if (mInStream != null) {
                mInStream.close();
                mInStream = null;
            }
            if (mOutStream != null) {
                mOutStream.close();
                mOutStream = null;
            }
        }
        catch (IOException e) {
            // состояние - NO_CONNECT
            mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
            Toast.makeText(getApplicationContext(),
                    "Ошибка Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    // Вызывается перед уничтожением активности
    @Override
    public void onDestroy() {
        // Освободить все ресурсы, включая работающие потоки,
        // соединения с БД и т. д.
        super.onDestroy();
        mMainStatus = MainStatus.CLOSE;
        closeBtStreams();
        releaseMP();    // release the resources of the player
    }
}

