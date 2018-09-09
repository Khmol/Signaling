package com.example.khmelevoyoleg.signaling;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
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

    public enum MainStatus {
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
    private static final String AUTO_CONNECT = "AUTO_CONNECT";   // вкл/выкл автоматическое соединение
    private static final String IN_NAME = "IN_NAME_"; // название ключа для имени входа в настройках
    private static final String ANALOG_IN_NAME = "ANALOG_IN_NAME"; // название ключа для имени входа в настройках
    private static final String OUT_NAME = "OUT_NAME_"; // название ключа для имени выхода в настройках
    private static final String IN_STATE = "IN_STATE_"; // название ключа для состояния входа в настройках
    private static final String ANALOG_IN_STATE = "ANALOG_IN_STATE_"; // название ключа для состояния входа в настройках
    private static final String OUT_STATE = "OUT_STATE_"; // название ключа для состояния входа в настройках
    private static final String DEFAULT_IN_NAME = "Вход "; // имя входа по умолчанию
    private static final String DEFAULT_OUT_NAME = "Выход "; // имя выхода по умолчанию
    private static final String DEFAULT_IN_OUT_STATUS = "STATUS_OFF"; // статус входа по умолчанию - выкл
    private static final String DEFAULT_IN_OUT_STATE = "STATE_OFF"; // состояние входа по умолчанию - выкл
    private static final String STATE_ON = "STATE_ON"; // состояние входа - включен
    private static final String STATE_OFF = "STATE_OFF"; // состояние входа - выключен

    // команды по BT
    static final String BT_INIT_MESSAGE = "SIMCOMSPPFORAPP\r"; //SIMCOMSPPFORAPP посылка для инициализации SIM
    static final String SET_ALARM = "SET ALARM,1\r"; // посылка для установки на охрану
    static final String CLEAR_ALARM = "CLEAR ALARM,1\r"; // посылка для снятия с охраны
    static final String CLEAR_ALARM_TRIGGERED = "CLEAR ALARM TRIGGERED,1\r"; // посылка для снятия аварии с SIM
    static final String OUT_1_ON = "OUT ON,1\r"; // посылка для открытия 1-го выхода
    static final String RX_INIT_OK = "SPP APP OK\r"; // ответ на BT_INIT_MESSAGE
    static final String TYPE_INPUT = "INPUT,"; // тип команды в ответе от SIM
    static final String TYPE_INPUT_A = "INPUT A,"; // тип команды в ответе от SIM
    static final String TYPE_INPUT_ON_OFF = "INPUT ON OFF,"; // тип команды в ответе на IN GET ON от SIM
    static final String OUT_OFF = "OUT OFF,"; // команда выключить реле по времени
    static final String OUT_ON_TIME = "OUT ON TIME,"; // команда включить реле по времени
    static final String OUT_ON = "OUT ON,"; // команда включить реле
    static final String IN_GET_ON = "IN GET ON,00\r"; // команда Запросить статус включенных входов
    static final String IN_ON = "IN ON,"; // команда включить вход для обработки
    static final String IN_OFF = "IN OFF,"; // команда выключить вход для обработки

    static final String STATUS_OFF = "STATUS_OFF"; // состояние входа - выключен
    static final String STATUS_ON = "STATUS_ON"; // состояние входа - включен
    static final String STATUS_START_ACTIVE = "STATUS_START_ACTIVE"; // состояние входа - активен на момент включения
    static final String STATUS_ALARM = "STATUS_ALARM"; // состояние входа - сработал
    static final String STATUS_GENERAL_ALARM = "STATUS_GENERAL_ALARM"; // состояние модуля - АВАРИЯ
    static final String STATUS_ALARM_TRIGGERED = "STATUS_ALARM_TRIGGERED"; // состояние модуля - предварительная АВАРИЯ
    static final String STATUS_GUARD_ON = "STATUS_GUARD_ON"; // состояние модуля - на охране
    static final String STATUS_GUARD_OFF = "STATUS_GUARD_OFF"; // состояние модуля - не на охране
    static final String STATUS_CLEAR_ALARM = "STATUS_CLEAR_ALARM"; // отключение оповещения об аварии
    static final String STATUS_INPUT_ON = "STATUS_INPUT_ON"; // сработал цифровой вход
    static final String STATUS_INPUT_OFF = "STATUS_INPUT_OFF"; // цифровой вход разомкнулся
    static final String STATUS_INPUT_FAULT = "STATUS_INPUT_FAULT"; // цифровой вход не обрабатывается

    // имена атрибутов для Map
    final String ATRIBUTE_NUMBER = "number";
    final String ATTRIBUTE_NAME = "name";
    final String ATTRIBUTE_STATUS_IMAGE = "image";
    final String ATTRIBUTE_TIME = "time";
    final String ATTRIBUTE_STATE = "swith";

    // определяем числовые константы
    private static final int FIRST_START = -1; // состояние модуля - не на охране
    private static final int MAX_CONNECTION_ATTEMPTS = 3;   // максимальное количество попыток установления соединения
    private static final int REQUEST_ENABLE_BT = 1;     // запрос включения Bluetooth
    private static final int SET_SETTINGS = 2;          // редактирование настроек
    private static final long UUID_SERIAL = 0x1101;     // нужный UUID
    private static final long UUID_MASK = 0xFFFFFFFF00000000L;  // маска для извлечения нужных битов UUID
    private static final short MASK_GUARD = 0;  // 0-й бит в маске - для извлечения флага нахождения на охране
    private static final short MASK_ALARM = 9;  // 9-й бит в маске - для извлечения флага сработала авария
    private static final short MASK_ALARM_CUR = 13;  // 13-й бит в маске - текущее значение флага сработала авария
    private static final short MASK_ALARM_TRIGERED = 8;  // 8-й бит в маске - для извлечения флага сработала предварительная авария
    private static final short MASK_ALARM_TRIGERED_CUR = 12;  // 12-й бит в маске - текущее значение флага предварительной аварии
    private static final int DELAY_TX_INIT_MESSAGE = 3; // задержка перед повторной передачей  INIT_MESSAGE
    private static final int TIMER_CHECK_STATUS = 400;  // периодичность вызова runCheckStatus
    private static final int DELAY_CONNECTING = 12;     // задержка перед повторной попыткой соединения по BT
    private static final int MAX_PROGRESS_VALUE = 3;    // количество ступеней в ProgressBar
    private static final int AUTO_CONNECT_TIMEOUT = 300;  // время между запуском поиска SIM 2 мин = TIMER_CHECK_STATUS * AUTO_CONNECT_TIMEOUT
    private static final long VIBRATE_TIME = 200;      //  длительность вибрации при нажатии кнопки
    private static final short DEFAULT_DIG_IN_NUMBER = 20; // количество входов по умолчанию
    private static final short DEFAULT_ANALOG_IN_NUMBER = 3; // количество входов по умолчанию
    private static final short DEFAULT_OUT_NUMBER = 10; // количество выходов по умолчанию
    private static final short CMD_INPUT_STATUS_FROM = 7; // начало флагов статуса охраны в команде INPUT
    private static final short CMD_INPUT_LATCH_FROM = 12; // начало флагов защелки статуса входов в команде INPUT
    private static final short CMD_INPUT_CUR_LATCH_FROM = 37; // начало флагов защелки статуса входов в команде INPUT
    private static final short CMD_INPUT_RSSI_FROM = 62; // начало значения RSSI в команде INPUT
    private static final short CMD_INPUT_A_CUR_ON_FROM = 9; // начало флагов включенных датчиков в команде INPUT_A
    private static final short CMD_INPUT_A_STATUS_FROM = 34; // начало флагов сатуса входов в команде INPUT_А
    private static final short CMD_INPUT_ON_OFF_STATUS_FROM = 14; // начало флагов сатуса входов в команде INPUT_ON_OFF
    private static final short LENGTH_INPUT_GROUP = 4; // длина данных для группы входов (по 16 входов)
    private static final short NUMBER_DIGITAL_INPUTS = 96; // количество цифровых входов
    private static final short ALL_OUT = 0;         // количество для выключения всех выходов
    private static final short RSSI_MASK = 127;         // маска для извлечения RSSI

    // переменные для адаптера
    ListView lvDigIn;
    ListView lvAnalogIn;
    DigInListViewAdapter adapterDigIn;
    AnalogInListViewAdapter adapterAnalogIn;
    ListView lvMainInStatus;
    SimpleAdapter adapterMainInStatus;
    ListView lvOutSettigs;
    OutListViewAdapter adapterOut;
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
    GregorianCalendar currentTime;
    MainStatus mMainStatus; // состояние подключения по Bluetooth
    SoundStatus mSoundStatus; // состояние звукового оповещения
    OutputStream mOutStream;                 // поток по передаче bluetooth
    InputStream mInStream;                   // поток по приему bluetooth
    private FloatingActionButton fabConnect;        // кнопка поиска "Базового блока"
    private short fabConnectPicture = 1;
    private Menu mainMenu;                          // гдавное меню
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
    Button pbDigInSave;                     // кнопка сохранить изменения на вкладке In
    Button pbAnalogInSave;                  // кнопка сохранить изменения на вкладке AnalogIn
    Button pbNext;                          // кнопка перейти к следующим настройкам
    Button pbAnalogNext;                          // кнопка перейти к следующим настройкам
    Button pbPrevious;                      // кнопка перейти к предыдущим настройкам
    Button pbAnalogPrevious;                // кнопка перейти к предыдущим настройкам
    Button pbOutPrevious;                   // кнопка перейти к предыдущим настройкам
    Button pbOutSave;                       // кнопка сохранить изменения на вкладке Out

    MediaPlayer mediaPlayer;
    AudioManager am;

    // создаем массивы данных для имен и состояний входов/выходов
    ArrayList<String> digInName = new ArrayList<>();
    ArrayList<String> digInNumber = new ArrayList<>();
    ArrayList<String> digInStatus = new ArrayList<>();
    ArrayList<Boolean> digInState = new ArrayList<>();
    ArrayList<String> analogInName = new ArrayList<>();
    ArrayList<String> analogInNumber = new ArrayList<>();
    ArrayList<String> analogInStatus = new ArrayList<>();
    ArrayList<Boolean> analogInState = new ArrayList<>();
    ArrayList<String> outName = new ArrayList<>();
    ArrayList<String> outNumber = new ArrayList<>();
    ArrayList<Boolean> outState = new ArrayList<>();
    ArrayList<String> mainStatusNumber = new ArrayList<>();
    ArrayList<String> mainStatusName = new ArrayList<>();
    ArrayList<String> mainStatusTime = new ArrayList<>();
    ArrayList<String> mainStatusImage = new ArrayList<>();
    ArrayList<Map<String, Object>> alDigInStatus;
    ArrayList<Map<String, Object>> alAnalogInStatus;
    ArrayList<Map<String, Object>> alOutStatus;
    ArrayList<Map<String, Object>> alMainInStatus;
    //endregion

    int statusSIM;     // флаги статусов охраны
    int oldStatusSIM = FIRST_START;  // флаги статусов охраны - прошлое сосояние
    boolean[] digitalInputLatch;       // фдаги статусов цифровых входов защелкнутые
    boolean[] oldDigitalInputLatch;    // прошлые фдаги статусов цифровых входов защелкнутые
    boolean[] digitalInputCurrent;     // фдаги статусов цифровых входов текущие
    boolean[] oldDigitalInputCurrent;  // прошные фдаги статусов цифровых входов текущие
    boolean[] digitalInputActive;      // фдаги статусов включенных входов
    boolean[] digitalInputACurOn;      // фдаги текущих статусов сработавших входов
    boolean[] oldDigitalInputACurOn;   // прошлые фдаги текущих статусов сработавших входов
    boolean[] digitalInputOnOffStatus; // фдаги текущих статусов входов - включены или выключены

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
        int layouts[] = new int[]{ R.layout.activity_main, R.layout.activity_dig_in,
                R.layout.activity_analog_in, R.layout.activity_out};
        for (int layout : layouts) {
            View currentView = inflater.inflate(layout, null);
            currentView.setTag(layout);
            flipper.addView(currentView);
        }
        // читаем значения настроек дискретных входов, результат в digInName, digInNumber, digInState
        loadInOutPreferences(DEFAULT_DIG_IN_NUMBER, IN_NAME, DEFAULT_IN_NAME, digInName,
                digInNumber, DEFAULT_IN_OUT_STATUS, digInStatus, IN_STATE, DEFAULT_IN_OUT_STATE, digInState);
        // читаем значения настроек аналоговых входов, результат в analogInName, analogInNumber, analogInState
        loadInOutPreferences(DEFAULT_ANALOG_IN_NUMBER, ANALOG_IN_NAME, DEFAULT_IN_NAME, analogInName,
                analogInNumber, DEFAULT_IN_OUT_STATUS, analogInStatus, ANALOG_IN_STATE,
                DEFAULT_IN_OUT_STATE, analogInState);
        // читаем значения настроек выходов, результат в outName, outNumber, outState
        loadInOutPreferences(DEFAULT_OUT_NUMBER, OUT_NAME, DEFAULT_OUT_NAME, outName,
                outNumber, null, null, OUT_STATE, DEFAULT_IN_OUT_STATE, outState);

        // заполняем значениями список InOutListView и создаем адапер
        adapterDigIn = createDigInAdapter(digInName, digInNumber, digInState);
        adapterAnalogIn = createAnalogInAdapter(analogInName, analogInNumber, analogInState);
        adapterMainInStatus = createMainInStatusAdapter();
        adapterOut = createOutSettingsAdapter();
        // определяем список lvDigIn и присваиваем ему адаптер
        lvDigIn = (ListView) findViewById(R.id.lvDigIn);
        lvDigIn.setAdapter(adapterDigIn);    // назначаем адаптер для ListView
        lvDigIn.setItemsCanFocus(true); // разрешаем элементам списка иметь фокус
        // определяем список lvDigIn и присваиваем ему адаптер
        lvAnalogIn = (ListView) findViewById(R.id.lvAnalogIn);
        lvAnalogIn.setAdapter(adapterAnalogIn);    // назначаем адаптер для ListView
        lvAnalogIn.setItemsCanFocus(true); // разрешаем элементам списка иметь фокус
        // определяем список lvMainInStatus и присваиваем ему адаптер
        lvMainInStatus = (ListView) findViewById(R.id.lvMainInStatus);
        lvMainInStatus.setAdapter(adapterMainInStatus);    // назначаем адаптер для lvMainInStatus
        // определяем список lvOutSettigs и присваиваем ему адаптер
        lvOutSettigs = (ListView) findViewById(R.id.lvOut);
        lvOutSettigs.setAdapter(adapterOut);    // назначаем адаптер для lvOutSettigs
        lvOutSettigs.setItemsCanFocus(true); // разрешаем элементам списка иметь фокус

        pbDigInSave = (Button) findViewById(R.id.pbDigInSave);
        pbDigInSave.setOnClickListener(this);
        pbAnalogInSave = (Button) findViewById(R.id.pbAnalogInSave);
        pbAnalogInSave.setOnClickListener(this);
        pbNext = (Button) findViewById(R.id.pbNext);
        pbNext.setOnClickListener(this);
        pbPrevious = (Button) findViewById(R.id.pbPrevious);
        pbPrevious.setOnClickListener(this);
        pbAnalogNext = (Button) findViewById(R.id.pbAnalogNext);
        pbAnalogNext.setOnClickListener(this);
        pbAnalogPrevious = (Button) findViewById(R.id.pbAnalogPrevious);
        pbAnalogPrevious.setOnClickListener(this);
        pbOutPrevious = (Button) findViewById(R.id.pbOutPrevious);
        pbOutPrevious.setOnClickListener(this);
        pbOutSave = (Button) findViewById(R.id.pbOutSave);
        pbOutSave.setOnClickListener(this);

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
        // массивы для состояний входов
        digitalInputLatch = new boolean[NUMBER_DIGITAL_INPUTS]; // защелкнутые
        oldDigitalInputLatch = new boolean[NUMBER_DIGITAL_INPUTS]; // защелкнутые
        digitalInputCurrent = new boolean[NUMBER_DIGITAL_INPUTS]; // текущие
        oldDigitalInputCurrent = new boolean[NUMBER_DIGITAL_INPUTS]; // текущие
        digitalInputActive = new boolean[NUMBER_DIGITAL_INPUTS]; // активные
        digitalInputACurOn = new boolean[NUMBER_DIGITAL_INPUTS]; // текущие при постановке на охр.
        oldDigitalInputACurOn = new boolean[NUMBER_DIGITAL_INPUTS]; // прошлое при постановке на охр.
        digitalInputOnOffStatus = new boolean[NUMBER_DIGITAL_INPUTS]; // текущее состояне входов

        // TODO - удалить
        tvBtRxData.setOnClickListener(this);

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

    /**
     * заполнение значениями список InOutListView
     * @param inOutName - массив имен входов/ выходов
     * @param inOutNumber - массив номеров входов/ выходов
     * @param inOutState - массив состояний входов/ выходов
     */
    private DigInListViewAdapter createDigInAdapter(ArrayList<String> inOutName, ArrayList<String> inOutNumber,
                                                    ArrayList<Boolean> inOutState) {
        // настраиваем ListView
        // упаковываем данные в понятную для адаптера структуру
        alDigInStatus = new ArrayList<>(1);
        Map<String, Object> m;
        DigInListViewAdapter adapter;
        for (int i = 0; i < inOutNumber.size(); i++) {
            m = new HashMap<>();
            m.put(ATRIBUTE_NUMBER, inOutNumber.get(i));
            m.put(ATTRIBUTE_NAME, inOutName.get(i));
            m.put(ATTRIBUTE_STATE, inOutState.get(i));
            m.put(ATTRIBUTE_STATUS_IMAGE, R.drawable.circle_grey48);
            alDigInStatus.add(m);
        }
        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {ATRIBUTE_NUMBER, ATTRIBUTE_NAME,
                ATTRIBUTE_STATE, ATTRIBUTE_STATUS_IMAGE};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvDigInNumber, R.id.etDigInName,
                R.id.swDigInState, R.id.ivDigInStatus}; //R.id.cbChecked,
        // создаем адаптер
        adapter = new DigInListViewAdapter(this, alDigInStatus, R.layout.dig_in_item, from, to);
        // передаем ссылку на основную activity
        adapter.link(this);
        return adapter;
    }

    /**
     * изменяем адаптер adapterDigIn
     */
    void modifyDigInAdapter () {
        Map<String, Object> m;
        for (int i = 0; i < digInNumber.size(); i++) {
            m = new HashMap<>();
            m.put(ATRIBUTE_NUMBER, digInNumber.get(i));
            m.put(ATTRIBUTE_NAME, digInName.get(i));
            m.put(ATTRIBUTE_STATE, digInState.get(i));
            m.put(ATTRIBUTE_STATUS_IMAGE, getDigInImageViewValue(i));
            alDigInStatus.set(i, m);
        }
        // подтверждаем изменения
        adapterDigIn.notifyDataSetChanged();
    }


    /**
     * заполнение значениями список InOutListView
     * @param inOutName - массив имен входов/ выходов
     * @param inOutNumber - массив номеров входов/ выходов
     * @param inOutState - массив состояний входов/ выходов
     */
    private AnalogInListViewAdapter createAnalogInAdapter(ArrayList<String> inOutName, ArrayList<String> inOutNumber,
                                                    ArrayList<Boolean> inOutState) {
        // настраиваем ListView
        // упаковываем данные в понятную для адаптера структуру
        alAnalogInStatus = new ArrayList<>(1);
        Map<String, Object> m;
        AnalogInListViewAdapter adapter;
        for (int i = 0; i < inOutNumber.size(); i++) {
            m = new HashMap<>();
            m.put(ATRIBUTE_NUMBER, inOutNumber.get(i));
            m.put(ATTRIBUTE_NAME, inOutName.get(i));
            m.put(ATTRIBUTE_STATE, inOutState.get(i));
            m.put(ATTRIBUTE_STATUS_IMAGE, R.drawable.circle_grey48);
            alAnalogInStatus.add(m);
        }
        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {ATRIBUTE_NUMBER, ATTRIBUTE_NAME,
                ATTRIBUTE_STATE, ATTRIBUTE_STATUS_IMAGE};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvAnalogInNumber, R.id.etAnalogInName,
                R.id.swAnalogInState, R.id.ivAnalogInStatus}; //R.id.cbChecked,
        // создаем адаптер
        adapter = new AnalogInListViewAdapter(this, alAnalogInStatus, R.layout.analog_item, from, to);
        // передаем ссылку на основную activity
        adapter.link(this);
        return adapter;
    }

    /**
     * заполнение значениями список MainInStatus
     */
    private SimpleAdapter createMainInStatusAdapter() {
        // настраиваем ListView
        // упаковываем данные в понятную для адаптеру структуру
        alMainInStatus = new ArrayList<>(0);
        SimpleAdapter adapter;
        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {ATRIBUTE_NUMBER, ATTRIBUTE_NAME,
                ATTRIBUTE_TIME, ATTRIBUTE_STATUS_IMAGE};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvMainStatusNumber, R.id.tvMainStatusName,
                R.id.tvMainStatusTime, R.id.ivMaimStatus}; //R.id.cbChecked,
        // создаем адаптер
        adapter = new SimpleAdapter(this, alMainInStatus, R.layout.main_status_item, from, to);
        // передаем ссылку на основную activity
        return adapter;
    }

    /**
     * заполнение значениями список MainInStatus
     */
    private OutListViewAdapter createOutSettingsAdapter() {
        // настраиваем ListView
        // упаковываем данные в понятную для адаптера структуру
        alOutStatus = new ArrayList<>(0);
        // добавляем данные в адаптер Out
        // получаем размер массива номеров входов
        int cnt = outNumber.size();
        Map<String, Object> m;
        for (int i = 0; i < cnt; i++) {
            // добавляем пункт в список
            m = new HashMap<>();
            m.put(ATRIBUTE_NUMBER, outNumber.get(i));
            m.put(ATTRIBUTE_NAME, outName.get(i));
            m.put(ATTRIBUTE_STATE, outState.get(i));
            m.put(ATTRIBUTE_STATUS_IMAGE, R.drawable.circle_grey32);
            alOutStatus.add(m);
        }
        // обновляем адаптер
        //adapterOut.notifyDataSetChanged();
        OutListViewAdapter adapter;
        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {ATRIBUTE_NUMBER, ATTRIBUTE_NAME, ATTRIBUTE_STATE, ATTRIBUTE_STATUS_IMAGE};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvOutNumber, R.id.etOutName, R.id.swOutState, R.id.ivOutTimeSwitch}; //R.id.cbChecked,
        // создаем адаптер
        adapter = new OutListViewAdapter(this, alOutStatus, R.layout.out_item, from, to);
        adapter.link(this);
        // передаем ссылку на основную activity
        return adapter;
    }

    /**
     * загрузка данных из настроек либо сохранение настроек по умолчанию если настроек не было
     * @param defaultInOutNumber - количество входов по умолчанию при создании настроек
     * @param inOutName - массив имен входов/ выходов
     * @param inOutNumber - массив номеров входов/ выходов
     * @param inOutStatus - массив статусов входов/ выходов
     * @param inOutState - массив состояний входов/ выходов
     */
    private void loadInOutPreferences(short defaultInOutNumber,
                                      String keyName,
                                      String defaultName,
                                      ArrayList<String> inOutName,
                                      ArrayList<String> inOutNumber,
                                      String defaultStatus,
                                      ArrayList<String> inOutStatus,
                                      String keyState,
                                      String defaultState,
                                      ArrayList<Boolean> inOutState) {
        StringBuilder  prefKey = new StringBuilder(keyName);  // задаем ключ для чтения настроек
        StringBuilder  prefText = new StringBuilder(""); // задаем значение прочтенной настройки
        int i = 0;
        do {
            // меняем ключ для чтения настроек "имени"
            prefKey.replace(0, prefKey.capacity(), keyName + Integer.toString(i));
            // читаем настройки
            prefText.replace(0, prefText.capacity(), sPref.getString(prefKey.toString(), ""));
            // сохраняем значение имени для вывода на экран
            inOutName.add(prefText.toString());
            // сохраняем значение номера для вывода на экран
            inOutNumber.add(Integer.toString(i + 1));
            // меняем ключ для чтения настроек "состояния"
            prefKey.replace(0, prefKey.capacity(), keyState + Integer.toString(i));
            // читаем настройки
            prefText.replace(0, prefText.capacity(), sPref.getString(prefKey.toString(), ""));
            // сохраняем значение имени для вывода на экран
            if (prefText.toString().equals(STATE_ON))
                inOutState.add(true);
            else
                inOutState.add(false);
            if (inOutStatus != null)
                inOutStatus.add(defaultStatus);
            i++;
        } while ( !prefText.toString().equals(""));
        // удаляем последнюю запись, так как она пустая
        inOutName.remove(--i);
        inOutState.remove(i);
        inOutNumber.remove(i);
        // проверяем есть ли данные в настройках
        if (inOutName.size() == 0) {
            // сохраненных настроек нет, нужно сохранить настройки по умолчанию
            // создаем эдитор для записи настройки
            SharedPreferences.Editor ed = sPref.edit();
            for (i = 0; i < defaultInOutNumber; i++){
                // формируем ключ для записи настроек "имени"
                prefKey.replace(0, prefKey.capacity(), keyName + Integer.toString(i));
                // указываем значение для вводимой настройки "имени"
                prefText.replace(0, prefText.capacity(), defaultName + Integer.toString(i + 1));
                // записываем настройку
                ed.putString(prefKey.toString(), prefText.toString());
                // добавляем значение "имени" по умолчанию в список
                inOutName.add(prefText.toString());

                // формируем ключ для записи настроек "состояния"
                prefKey.replace(0, prefKey.capacity(), keyState + Integer.toString(i));
                // указываем значение для вводимой настройки "состояния"
                prefText.replace(0, prefText.capacity(), defaultState);
                // записываем настройку
                ed.putString(prefKey.toString(), prefText.toString());
                // добавляем значение "состояния" по умолчанию в список
                if (prefText.toString().equals(STATE_ON))
                    inOutState.add(true);
                else
                    inOutState.add(false);
                // добавляем значение "номера" по умолчанию в список для вывода на экран
                inOutNumber.add(Integer.toString(i + 1));
                if (inOutStatus != null)
                    inOutStatus.add(defaultStatus);
            }
            // сохраняем изменения для настроек
            ed.apply();
        }
    }

    /**
     * сохранение настроек InOut в xml файл
     * @param inOutName - массив имен входов/ выходов
     * @param inOutState - массив состояний входов/ выходов
     */
    private void saveInOutPreferences(String keyName, ArrayList<String> inOutName,
                                      String keyState, ArrayList<Boolean> inOutState) {
        StringBuilder  prefKey = new StringBuilder(keyName);  // задаем ключ для чтения настроек
        StringBuilder  prefText = new StringBuilder(""); // задаем значение прочтенной настройки
        // создаем эдитор для записи настройки
        SharedPreferences.Editor ed = sPref.edit();

        for (int i = 0; i < inOutName.size(); i++){
            // формируем ключ для записи настроек "имени"
            prefKey.replace(0, prefKey.capacity(), keyName + Integer.toString(i));
            // указываем значение для вводимой настройки "имени"
            prefText.replace(0, prefText.capacity(), inOutName.get(i));
            // записываем настройку
            ed.putString(prefKey.toString(), prefText.toString());

            // формируем ключ для записи настроек "состояния"
            if (inOutState != null) {
                prefKey.replace(0, prefKey.capacity(), keyState + Integer.toString(i));
                // указываем значение для вводимой настройки "состояния"
                if (inOutState.get(i))
                    prefText.replace(0, prefText.capacity(), STATE_ON);
                else
                    prefText.replace(0, prefText.capacity(), STATE_OFF);
                // записываем настройку
                ed.putString(prefKey.toString(), prefText.toString());
            }
        };
        // сохраняем изменения для настроек
        ed.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // инициализация меню
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mainMenu = menu;
        return true;
    }

    @Override
    public void onClick(View v) {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        switch (v.getId()){
            case R.id.pbDigInSave:
                // сохраняем настройки
                saveInOutPreferences(IN_NAME, digInName, IN_STATE, digInState);
            case R.id.pbPrevious:
                // фиксируем изменения в адаптере
                modifyDigInAdapter();
                // закрываем экранную клавиатуру
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // переходим на предыдущую страницу
                flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_in));
                flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_out));
                flipper.showPrevious();
                break;
            case R.id.pbAnalogInSave:
                // сохраняем настройки
                saveInOutPreferences(ANALOG_IN_NAME, analogInName, ANALOG_IN_STATE, analogInState);
            case R.id.pbAnalogPrevious:
                // закрываем экранную клавиатуру
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // переходим на предыдущую страницу
                flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_in));
                flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_out));
                flipper.showPrevious();
                break;
            case R.id.pbNext:
                // фиксируем изменения в адаптере
                modifyDigInAdapter();
            case R.id.pbAnalogNext:
                // закрываем экранную клавиатуру
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // переходим на следующую страницу
                flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_next_in));
                flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_next_out));
                flipper.showNext();
                break;
            case R.id.tvBtRxData:
                break;
            case R.id.pbOutSave:
                // сохраняем настройки
                saveInOutPreferences(OUT_NAME, outName, null, null);
            case R.id.pbOutPrevious:
                // закрываем экранную клавиатуру
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // задаем вопрос нужно ли выключить все реле при выходе из окна
                showDialogOut();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получаем код выбранного пункта меню
        int id = item.getItemId();
        // если выбран пункт меню "Настройки"
        switch (id) {
            case R.id.action_settings :
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
            case R.id.action_connect :
                if (mMainStatus == MainStatus.IDLE){
                    // выводим сообщение "Запущен поиск сигнализации"
                    Toast.makeText(getApplicationContext(), R.string.connectionStart, Toast.LENGTH_SHORT).show();
                    mMainStatus = MainStatus.CONNECTING;   // переходим в установку соединения
                    mConnectionStatusBT = ConnectionStatusBT.CONNECTING;    // переходим в режим CONNECTING
                    //setFabConnectColorBlue();   // синий цвет для кнопки FabConnect
                    // создаем асинхронную задачу соединения если прошлая уже отработала
                    if (bluetooth_Connect.getStatus().toString().equals(FINISHED)) {
                        bluetooth_Connect = new BTConnect();
                        // передаем ссылку на основную activity
                        bluetooth_Connect.link(this);
                    }
                    // запускаем поиск по BT
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
                return true;
            case R.id.action_clear_main_listview:
                // очищаем все ArrayList для adapterMainInStatus,
                alMainInStatus.clear();
                mainStatusNumber.clear();
                mainStatusName.clear();
                mainStatusTime.clear();
                mainStatusImage.clear();
                // подтверждаем изменения
                adapterMainInStatus.notifyDataSetChanged();
                return true;
        }
        // по умолчанию возвращаем обработчик родителя
        return super.onOptionsItemSelected(item);
    }

    /**
     * обработка нажатия кнопки fabConnect
     */
    private void pbConnectHeader(){
        //fabConnect.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
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
                // обработка перелиствывания экрана на экран InOut
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
                                if (checkAbilityTxBT())
                                    // запрашиваем новое значения входов (включенные или выключенные)
                                    sendDataBT(IN_GET_ON, 0);
                                else
                                    // выдаем текстовое оповещение что соединение отсутствует
                                    Toast.makeText(getApplicationContext(),
                                            R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                                            Toast.LENGTH_SHORT).show();
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
                changePictureMenuConnect();
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
    private void changePictureMenuConnect() {
        switch (fabConnectPicture) {
            case 1:
                mainMenu.findItem(R.id.action_connect).setIcon(R.drawable.antenna1red);
                break;
            case 2:
                mainMenu.findItem(R.id.action_connect).setIcon(R.drawable.antenna2red);
                break;
            case 3:
                mainMenu.findItem(R.id.action_connect).setIcon(R.drawable.antenna3red);
                break;
            case 4:
                mainMenu.findItem(R.id.action_connect).setIcon(R.drawable.antenna3green);
                break;
            case 5:
                mainMenu.findItem(R.id.action_connect).setIcon(R.drawable.antenna3green_light);
                break;
        }
        // проверка идет поиск базы
        if (fabConnectPicture <= 3) {
            if (fabConnectPicture == 3)
                fabConnectPicture = 1;
            else fabConnectPicture++;
        }
        // соединение установлено идет прием данных
        else {
            if (fabConnectPicture == 4)
                fabConnectPicture = 5;
            else
                fabConnectPicture = 4;
        }
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
    boolean sendDataBT(String data, int delay){
        // проверка завершилась ли прошлая задача передачи
        if ( bluetooth_Tx == null || bluetooth_Tx.getStatus().toString().equals(FINISHED)) {
            // передача завершилась, создаем новую задачу передачи
            bluetooth_Tx = new BTTx(data);
            // передаем ссылку на основную activity
            bluetooth_Tx.link(this);
            bluetooth_Tx.execute(delay);
            Log.d("MY_LOG_TX", "Send: " + data);
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
        ArrayList<String> commandsToParse = new ArrayList<>();
        if (data.length() > 0){
            int startIndexIDCommandBT = 0;
            int endIndexIDCommandBT = 0;
            // находим символ "<" - признак начала строки
            startIndexIDCommandBT = data.indexOf('<');
            if (startIndexIDCommandBT >= 0)
                do {
                    // находим символ "<" - признак начала строки
                    endIndexIDCommandBT = data.indexOf('<', (startIndexIDCommandBT + 1));
                    if (endIndexIDCommandBT >= 0) {
                        // выделячем команду от startIndexIDCommandBT до endIndexIDCommandBT
                        commandsToParse.add(data.substring(startIndexIDCommandBT, endIndexIDCommandBT ));
                        startIndexIDCommandBT = endIndexIDCommandBT;
                    } else {
                        // выделячем команду от startIndexIDCommandBT до конца data
                        commandsToParse.add(data.substring(startIndexIDCommandBT, data.length()));
                    }
                } while (endIndexIDCommandBT >= 0);
            for (String parseData: commandsToParse) {
                // проверка на команду INPUT
                // region INPUT
                if (parseData.indexOf(TYPE_INPUT) > 0){
                    // принята команда INPUT
                    String strStatusSIM = parseData.substring(CMD_INPUT_STATUS_FROM,
                            CMD_INPUT_LATCH_FROM - 1);
                    // обновляем значение статуса SIM модуля
                    statusSIM = Integer.parseInt(strStatusSIM, 16);
                    // обновляем защелкнутые значения флагов входов
                    parseRxInputFlags(parseData, CMD_INPUT_LATCH_FROM, CMD_INPUT_CUR_LATCH_FROM,
                            LENGTH_INPUT_GROUP, digitalInputLatch);
                    // обновляем текущие значения флагов входов
                    parseRxInputFlags(parseData, CMD_INPUT_CUR_LATCH_FROM, CMD_INPUT_RSSI_FROM,
                            LENGTH_INPUT_GROUP, digitalInputCurrent);
                    // обновляем значение RSSI в главном меню
                    String rssi = "-" + Integer.toString(parseRxRSSI(parseData, CMD_INPUT_RSSI_FROM)) + "dB";
                    mainMenu.findItem(R.id.RSSI).setTitle(rssi);
                    // обновляем рисунок показывая что связь установлена и есть прием данных
                    changePictureMenuConnect();
                    // проверка изменилось ли состояние модуля
                    if (statusSIM != oldStatusSIM) {
                        // устанавливаем нужный рисунок на крыше машины
                        if (getValueOnMask(statusSIM, MASK_GUARD) > 0) {
                            // сигн. на охране
                            if (getValueOnMask(statusSIM, MASK_GUARD) !=
                                    getValueOnMask(oldStatusSIM, MASK_GUARD) |
                                    oldStatusSIM == FIRST_START) {
                                // устанавливаем рисунок - close_small "закрыто"
                                ivSigState.setImageResource(R.drawable.close_small);
                                // добавляем событие "Установка на охрану" в главный список
                                addEventStatusList(STATUS_GUARD_ON, null);
                            }
                        } else {
                            if (getValueOnMask(statusSIM, MASK_GUARD) !=
                                    getValueOnMask(oldStatusSIM, MASK_GUARD) |
                                    oldStatusSIM == FIRST_START) {
                                // устанавливаем рисунок - close_small "открыто"
                                ivSigState.setImageResource(R.drawable.open_small);
                                // добавляем событие "Снятие с охраны" в главный список
                                addEventStatusList(STATUS_GUARD_OFF, null);
                            }
                        }
                        // включаем/ выключаем звук аварии
                        if (getValueOnMask(statusSIM, MASK_ALARM) > 0) {
                            // сработала авария, включаем звук
                            if ( getValueOnMask(statusSIM, MASK_ALARM) != getValueOnMask(oldStatusSIM, MASK_ALARM) |
                                    oldStatusSIM == FIRST_START ) {
                                // запуск аварии
                                startAlarm();
                            } else
                                if (getValueOnMask(statusSIM, MASK_ALARM_CUR) > 0) {
                                    // запуск аварии
                                    startAlarm();
                                } else {
                                    // выключаем звук аварии
                                    stopMediaPlayer();
                                }
                        } else  if ( getValueOnMask(statusSIM, MASK_ALARM) !=
                                getValueOnMask(oldStatusSIM, MASK_ALARM) &
                                oldStatusSIM != FIRST_START ) {
                            // авария не сработала либо была выключена, выключаем звук
                            stopMediaPlayer();
                            // добавляем событие "Сброс оповещения" в главный список
                            addEventStatusList(STATUS_CLEAR_ALARM, null);
                        }
                        // включаем/ выключаем звук предварительной аварии если при этом нет аварии
                        if (getValueOnMask(statusSIM, MASK_ALARM_TRIGERED) > 0 ) {
                            if ( getValueOnMask(statusSIM, MASK_ALARM_CUR) == 0 ) {
                                // сработала предварительная авария, включаем звук
                                if (getValueOnMask(statusSIM, MASK_ALARM_TRIGERED_CUR) > 0 |
                                        getValueOnMask(statusSIM, MASK_ALARM_TRIGERED) !=
                                                getValueOnMask(oldStatusSIM, MASK_ALARM_TRIGERED) |
                                        oldStatusSIM == FIRST_START) {
                                    Log.d(LOG_TAG, "start pre_alarm");
                                    if (mediaPlayer == null) {
                                        // не включаем звук предварительной аварии когда звучит "Авария"
                                        mSoundStatus = SoundStatus.PREALARM_ACTIVE;
                                        mediaPlayer = MediaPlayer.create(this, R.raw.prealarm);
                                        mediaPlayer.setOnCompletionListener(this);
                                        mediaPlayer.start();
                                        // добавляем событие "Предварительная авария" в главный список
                                        addEventStatusList(STATUS_ALARM_TRIGGERED, null);
                                    }
                                }
                            }
                        } else if (getValueOnMask(statusSIM, MASK_ALARM_TRIGERED) !=
                                getValueOnMask(oldStatusSIM, MASK_ALARM_TRIGERED) &
                                oldStatusSIM != FIRST_START ) {
                            // авария не сработала либо была выключена, выключаем звук
                            stopMediaPlayer();
                        }
                        // проверка изменились ли входы, выводим сообщения в главный список
                        checkChangeDigitalInput();
                        // обновляем значение статуса
                        oldStatusSIM = statusSIM;
                    }
                }
                // endregion
                // проверка на команду INPUT_А
                // region INPUT_A
                if (parseData.indexOf(TYPE_INPUT_A) > 0) {
                    // принята команда INPUT_A
                    // получаем значение обрабаываемых входов
                    parseRxInputFlags(parseData, CMD_INPUT_A_CUR_ON_FROM, CMD_INPUT_A_STATUS_FROM,
                            LENGTH_INPUT_GROUP, digitalInputACurOn);
                    // получаем статус включенных входов
                    parseRxInputFlags(parseData, CMD_INPUT_A_STATUS_FROM, parseData.length(),
                            LENGTH_INPUT_GROUP, digitalInputActive);
                    // проверка были ли изменения в состоянии датчиков
                    for (int i = 0; i < NUMBER_DIGITAL_INPUTS; i++) {
                        if (digitalInputACurOn[i] != oldDigitalInputACurOn[i]) {
                            // проверка включен ли вход
                            if (digitalInputActive[i]) {
                                // вход ключен проверка обрабатывается ли он
                                if ( !digitalInputACurOn[i]) {
                                    // вход не обрабатывается
                                    // добавляем событие "Вход выключен" в главный список
                                    addEventStatusList(STATUS_INPUT_FAULT, digInName.get(i));
                                }
                            }
                        }
                        // перезаписываем старое значение флагов
                        oldDigitalInputACurOn[i] = digitalInputACurOn[i];
                    }
                }
                // endregion
                // проверка на команду INPUT_ON_OFF
                // region INPUT_ON_OFF
                if (parseData.indexOf(TYPE_INPUT_ON_OFF) > 0) {
                    // получаем значения состояний входов
                    parseRxInputFlags(parseData, CMD_INPUT_ON_OFF_STATUS_FROM, parseData.length(),
                            LENGTH_INPUT_GROUP, digitalInputOnOffStatus);
                    // обновляем значения состояний входов которые отображаются
                    for(int i = 0; i < DEFAULT_DIG_IN_NUMBER; i++) {
                        digInState.set(i, digitalInputOnOffStatus[i]);
                    }
                    // обновляем список для настроек цифровых входов
                    modifyDigInAdapter();
                }
                // endregion
            }
        }
        // выводим сообщение, "Соединение отсутствует"
        //Toast.makeText(getApplicationContext(), statusSIM, Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * выделение значения RSSI из принятых данных
     */
    private int parseRxRSSI(String parse_data, int start_index) {
        String strInput = parse_data.substring(start_index, start_index + 2);
        int val = Integer.parseInt(strInput, 16);

        return (val & RSSI_MASK);
    }

    /**
     * проверка возможно ли осуществляь передачу по BT
     */
    boolean checkAbilityTxBT() {
        return mMainStatus == MainStatus.CONNECTED;
    }

    /**
     * запуск звучания аварии и добавление события в главный список
     */
    private void startAlarm () {
        Log.d(LOG_TAG, "start alarm");
        // выключаем предварительную аварию если она есть
        if ( mSoundStatus == SoundStatus.PREALARM_ACTIVE )
            stopMediaPlayer();
        // включаем запуск сигнала "Авария"
        if ( mediaPlayer == null ) {
            // включаем звук "Авария"
            mSoundStatus = SoundStatus.ALARM_ACTIVE;
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm1);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
            // добавляем событие "Авария" в главный список
            addEventStatusList(STATUS_GENERAL_ALARM, null);
        }
    }

    /**
     * парсинг флагов полученных в текстовом формате
     * каждый полученный байт - 4 бита фдагов
     * @param parse_data - данные для парсинга
     * @param start_index - индекс символа в строке с которого начинается парсинг
     * @param stop_index - индекс символа в строке до которого выполняется парсинг
     * @param group_length - длина группы для парсинга
     * @param array_out - выходной массив флагов, в котором будут представлены результаты парсинга

     */
    private boolean parseRxInputFlags(String parse_data, int start_index, int stop_index,
                                      int group_length, boolean[] array_out) {
        try {
            int index = 0;
            // получаем текущий статус сработавших входов
            for ( int i = start_index;
                  i < stop_index - group_length;
                  i = i + group_length ) {
                String strInput = parse_data.substring(i, i + group_length);
                int intInput = Integer.parseInt(strInput, 16);
                for (int bit = index; bit < index + 16; bit++) {
                    array_out[bit] = checkBit(intInput, (bit - index));
                }
                index += (group_length *= 4 );
            }
            return true;
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return false;   // данные не удалось распознать
        }
    }

    /**
     * проверка изменения дискретных входов
     */
    private void checkChangeDigitalInput() {
        for (int i = 0; i < DEFAULT_DIG_IN_NUMBER; i++) {
            // проверяем изменения состояния защелкнутых входов
            if ( digitalInputLatch[i] != oldDigitalInputLatch[i] ) {
                if (digitalInputLatch[i]) {
                    // добавляем событие "сработал вход" в главный список
                    addEventStatusList(STATUS_INPUT_ON, digInName.get(i));
                }
            } else // проверяем изменения состояния текущих входов
                if ( digitalInputCurrent[i] != oldDigitalInputCurrent[i] ) {
                    if (digitalInputCurrent[i]) {
                        // добавляем событие "сработал вход" в главный список
                        addEventStatusList(STATUS_INPUT_ON, digInName.get(i));
                    }
                }
            oldDigitalInputLatch[i] = digitalInputLatch[i];
            oldDigitalInputCurrent[i] = digitalInputCurrent[i];
        }
    }

    /**
     * отсановка проигрывания звука
     */
    private void stopMediaPlayer () {
        // авария не сработала либо была выключена, выключаем звук
        Log.d(LOG_TAG, "stop media player");
        mSoundStatus = SoundStatus.IDLE;
        if (mediaPlayer == null)
            return;
        mediaPlayer.stop();
        mediaPlayer = null;
    }

    /**
     * занесение информации для вывода в lvMainInStatus
     * @param value - значение для проверки бита
     * @param masks - номера битов для проверки
     */
    private int getValueOnMask(int value, int... masks) {
        int result = 0;
        for ( int mask : masks ) {
           result = result + ((1 << mask) & value);
        }
        return result;
    }

    /**
     * занесение информации для вывода в lvMainInStatus
     * @param value - значение для проверки бита
     * @param bit_number - номер бита для проверки
     */
    private boolean checkBit (int value, int bit_number) {
        int temp = 1 << bit_number;
        int t2 = (value & (temp));
        return ( t2 > 0 );
    }

    /**
     * занесение информации для вывода в lvMainInStatus
     * @param event - значение события для занесения в список
     */
    private void addEventStatusList(String event, String input_name) {
        int cnt = mainStatusNumber.size();
        // прибавляем 1 к последнему номеру в списке
        if ( cnt > 0 ) {
            String strNumber = mainStatusNumber.get(cnt - 1);
            int intNumber = Integer.parseInt(strNumber);
            mainStatusNumber.add(Integer.toString( ++intNumber ));
        } else
            mainStatusNumber.add(Integer.toString(1));
        // проверка сработала авания ??
        if (event.equals(STATUS_GENERAL_ALARM)) {
            // выводим надпись Авария
            mainStatusName.add("Авария");
            // добавляем нужную картинку
            mainStatusImage.add(STATUS_GENERAL_ALARM);
        } else // проверка сработала предварительная авания ??
            if (event.equals(STATUS_ALARM_TRIGGERED)) {
            // выводим надпись Авария
            mainStatusName.add("Предупреждение");
            // добавляем нужную картинку
            mainStatusImage.add(STATUS_ALARM_TRIGGERED);
        } else // проверка произвели установку на охрану ??
            if (event.equals(STATUS_GUARD_ON)) {
            // выводим надпись Охрана установлена
            mainStatusName.add("Охрана установлена");
            // добавляем нужную картинку
            mainStatusImage.add(STATUS_GUARD_ON);
        } else // проверка произвели выключение охраны ??
            if (event.equals(STATUS_GUARD_OFF)) {
            // выводим надпись Охрана установлена
            mainStatusName.add("Охрана снята");
            // добавляем нужную картинку
            mainStatusImage.add(STATUS_GUARD_OFF);
        } else // проверка сбросили аварию ??
            if (event.equals(STATUS_CLEAR_ALARM)) {
            // выводим надпись Охрана установлена
            mainStatusName.add("Сброс аварии");
            // добавляем нужную картинку
            mainStatusImage.add(STATUS_CLEAR_ALARM);
        } else // проверка сработал вход ??
            if (event.equals(STATUS_INPUT_ON)) {
                // выводим надпись Охрана установлена
                mainStatusName.add(input_name);
                // добавляем нужную картинку
                mainStatusImage.add(STATUS_INPUT_ON);
        } else // проверка вход разомкнулся ??
            if (event.equals(STATUS_INPUT_OFF)) {
                // выводим надпись - иня входа
                mainStatusName.add(input_name);
                // добавляем нужную картинку
                mainStatusImage.add(STATUS_INPUT_OFF);
        } else // проверка вход не обрабатывается ??
            if (event.equals(STATUS_INPUT_FAULT)) {
                // выводим надпись - иня входа
                mainStatusName.add(input_name);
                // добавляем нужную картинку
                mainStatusImage.add(STATUS_INPUT_FAULT);
            }
        // добавляем информацию о времени срабатывания
        String strTime = new SimpleDateFormat("dd.MM\nHH:mm:ss").format(GregorianCalendar.getInstance().getTime());
        mainStatusTime.add(strTime);

        // добавляем пункт в список
        Map<String, Object> m;
        m = new HashMap<>();
        m.put(ATRIBUTE_NUMBER, mainStatusNumber.get(cnt));
        m.put(ATTRIBUTE_NAME, mainStatusName.get(cnt));
        m.put(ATTRIBUTE_TIME, mainStatusTime.get(cnt));
        m.put(ATTRIBUTE_STATUS_IMAGE, getImageViewValue(mainStatusImage.get(cnt)));
        alMainInStatus.add(m);
        adapterMainInStatus.notifyDataSetChanged();
        // усанавливаем фокус на последнем элементе
        lvMainInStatus.smoothScrollToPosition(cnt);
    }

    /**
     * получение нужной картинки для вывода в lvMainInStatus
     * @param value - значение параметра для обработки
     * @return - номер ресурса
     */
    private int getImageViewValue(String value) {
        if (value.equals(STATUS_GUARD_ON)) {
            return R.drawable.shield_green_mark;
        } else if (value.equals(STATUS_GUARD_OFF)) {
            return R.drawable.shield_yellow;
        } else if (value.equals(STATUS_CLEAR_ALARM)) {
            return R.drawable.shield_green;
        } else if (value.equals(STATUS_GENERAL_ALARM)) {
            return R.drawable.alarm32;
        } else if (value.equals(STATUS_ALARM_TRIGGERED)) {
            return R.drawable.warning;
        } else if (value.equals(STATUS_INPUT_ON)) {
            return R.drawable.circle_red32;
        } else if (value.equals(STATUS_INPUT_OFF)) {
            return R.drawable.circle_green32;
        } else if (value.equals(STATUS_INPUT_FAULT)) {
            return R.drawable.circle_blue32;
        }
        return 0;
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
        setMenuConnectColorRed();    // кнопка соединеия - красная
        mConnectionAttemptsCnt = 0; // счетчик попыток соединения в 0
        // устанавливаем рисунок - no_connect_small
        ivSigState.setImageResource(R.drawable.no_connect_small);
        closeBtStreams();   // закрываем все потоки
    }

    /**
     * установка зеленого цвета кнопки fabConnect
     */
    private void setMenuConnectColorGreen(){
        // задаем рисунок с 3-мя полосками на антенне
        mainMenu.findItem(R.id.action_connect).setIcon(R.drawable.antenna3green);
        // переходим в режим соединения с базоцй
        fabConnectPicture = 4;
    }

    /**
     * установка красного цвета кнопки fabConnect
     */
    private void setMenuConnectColorRed(){
        // задаем рисунок с 3-мя полосками на антенне
        mainMenu.findItem(R.id.action_connect).setIcon(R.drawable.antenna3red);
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
                    setMenuConnectColorGreen();
                }
            }

            // обновляем значение текста в окне
            if (!rxText.contains(RX_ERROR)){
                // производим анализ полученных данных
                analiseRxData(rxText);
                // выводим данные для отладки
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
        // выключаем предварительную аварию если она есть
        if ( mSoundStatus == SoundStatus.PREALARM_ACTIVE )
            stopMediaPlayer();
        else
            mediaPlayer.start();
        //stopMediaPlayer();
    }

    /**
     * закрытие потоков ввода вывода для BT
     */
    private void closeBtStreams(){
        try{
            oldStatusSIM = FIRST_START;  // обнуляем прошлое сосояние
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

    @Override
    public void onBackPressed() {
        View currentView = flipper.getCurrentView();
        int currentViewTag = (int) currentView.getTag();
        // проверка на каком экране нажата кнопка BACK
        switch (currentViewTag) {
            case R.layout.activity_main:
                //на главном экране, нужно свернуть программу
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                break;
            case R.layout.activity_out:
                // задаем вопрос нужно ли выключить все реле при выходе из окна
                showDialogOut();
                break;
            case R.layout.activity_dig_in:
                // фиксируем изменения в адаптере
                modifyDigInAdapter();
            case R.layout.activity_analog_in:
                //на InOut
                showPreviousActivity();
                break;
        }
    }

    /**
     * диалог при закрытии окна Out
     */
    private void showDialogOut () {
        AlertDialog.Builder alertDialog;
        // настраиваем alertDialog
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.daOutState);  // заголовок
        alertDialog.setMessage(R.string.daOutQuestion); // сообщение
        alertDialog.setPositiveButton(R.string.daOutAnswerOn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // оставляем адаптер Out без изменений (т.е. все остается включенным)
                showPreviousActivity();
                dialog.cancel();
            }
        });
        alertDialog.setNegativeButton(R.string.daOutAnswerOff, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // обновляем адаптер Out, чтобы вернуть переключатели в выключенное состояние
                adapterOut.notifyDataSetChanged();
                if (checkAbilityTxBT())
                    sendDataBT(String.format("%s%d\r", OUT_OFF, ALL_OUT), 0);
                else
                    // выдаем текстовое оповещение что соединение отсутствует
                    Toast.makeText(getApplicationContext(),
                            R.string.connectionFailed, // + Integer.toString(ivNumber) + R.string.outOnTimeEnd,
                            Toast.LENGTH_SHORT).show();
                showPreviousActivity();
                dialog.cancel();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    // показать предыдущий экран настроек
    private void showPreviousActivity () {
        flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.go_prev_out));
        flipper.showPrevious();
    }

    /**
     * получение нужной картинки для вывода в ivAnalogInStatus
     * @param position - позиция элемента в списке
     * @return - номер ресурса
     */
    int getDigInImageViewValue(int position) {
        if (digInStatus.get(position).equals(STATUS_OFF)) {
            return R.drawable.circle_grey48;
        } else if (digInStatus.get(position).equals(STATUS_ON)) {
            return R.drawable.circle_green48;
        } else if (digInStatus.get(position).equals(STATUS_START_ACTIVE)) {
            return R.drawable.circle_blue48;
        } else if (digInStatus.get(position).equals(STATUS_ALARM)) {
            return R.drawable.circle_red48;
        }
        return 0;
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

