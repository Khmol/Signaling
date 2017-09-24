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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
//import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {
    // инициализация переменных
    //region InitVar
    // тип перечисления состояний по Bluetooth
    private enum ConnectionStatusBT {
        CONNECTING,
        NO_CONNECT,
        CONNECTED;
    }

    private enum MainStatus {
        CLOSE,
        IDLE,
        CONNECTING,
        CONNECTED,
        CLEAR_ALARM_TRIGGERED,
        CLEAR_ALARM,
        SET_ALARM;
    }
    // определяем строковые константы
    private static final String SETTINGS_FILENAME = "Signaling";   // имя файла для хранения настроек
    private static final String SELECTED_BOUNDED_DEV = "SELECTED_BOUNDED_DEV";   // выбранное спаренное устройство
    private static final String NO_BOUNDED_DEVICE = "NO_BOUNDED_DEVICE";   // устройство для соединения не выбрано
    private static final String CONNECTION_ERR = "CONNECTION_ERR";   // ошибка соединения
    private static final String BT_INIT_MESSAGE = "SIMCOMSPPFORAPP\r";    //SIMCOMSPPFORAPP посылка для инициализации SIM
    private static final String CLEAR_ALARM_TRIGGERED = "CLEAR ALARM TRIGGERED,1\r";        // посылка для снятия аварии с SIM
    private static final String CLEAR_ALARM = "CLEAR ALARM,1\r";                // посылка для снятия с охраны
    private static final String SET_ALARM = "SET ALARM,1\r";                    // посылка для установки на охрану
    private static final String RX_INIT_OK = "SPP APP OK\r";                    // ответ на BT_INIT_MESSAGE
    // определяем числовые константы
    private static final int MAX_CONNECTION_ATTEMPTS = 3;   // максимальное количество попыток установления соединения
    private static final int REQUEST_ENABLE_BT = 1;     // запрос включения Bluetooth
    private static final int SET_SETTINGS = 2;          // редактирование настроек
    private static final int BT_CONNECT_OK = 5;         // соединение по Bluetooth установлено успешно
    private static final int BT_CONNECT_ERR = 6;        // ошибка установки соединения по Bluetooth
    private static final int BT_CONNECTED = 7;          // соединения по Bluetooth уже было установлено
    private static final int BT_TX_OK = 8;              // передача по Bluetooth успешно выполнена
    private static final int BT_TX_ERR = 9;             // ошибка передачи по Bluetooth
    private static final int BT_RX_OK_ANSWER = 10;      // прием по Bluetooth успешно выполнен
    private static final int BT_RX_ERR_ANSWER = 11;     // ошибка приема по Bluetooth
    private static final int BT_CONNECT_INTERRUPT = 12;  // прерывание соединения по BT
    private static final int BT_TX_INTERRUPT = 13;       // прерывание передачи по BT
    private static final int BT_RX_INTERRIPT = 14;       // прерывание приема по BT

    // определяем стринговые переменные
    protected String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mClientSocket;
    private ConnectionStatusBT mConnectionStatusBT; // состояние подключения по Bluetooth
    Handler btConnectHandler;   // обработчик сообщений из потока btConnect
    Handler btRxHandler;        // обработчик сообщений из потока bluetooth_Rx
    Handler btTxHandler;        // обработчик сообщений из потока bluetooth_Tx
    Thread bluetooth_Rx;
    Thread bluetooth_Connect;
    Thread bluetooth_Tx;

    private int mConnectionAttemptsCnt = 0;         // счетчик попыток подключения по Bluetooth
    private MainStatus mMainStatus; // состояние подключения по Bluetooth
    private OutputStream mOutStream;                 // поток по передаче bluetooth
    private InputStream mInStream;                   // поток по приему bluetooth
    private FloatingActionButton fabConnect;        // кнопка поиска "Базового блока"
    private ViewFlipper flipper;                    // определяем flipper для перелистываний экрана
    private float fromPosition;                     // позиция касания при перелистывании экранов
    private TextView tvBtRxData;                    // текст с принятыми данными
    private Set<BluetoothDevice> pairedDevices;     // множество спаренных устройств
    private SharedPreferences sPref;                // настройки приложения
    int btRxCnt;                                    // счетчик принятых пакетов
    //endregion

    // приемник широковещательных событий
    // region BroadcastReceiver
    BroadcastReceiver connectionStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // обновляем значение текста в окне
            String text = "mMainStatus = " + mMainStatus.toString() + "mConnectionStatusBT = "
                    + mConnectionStatusBT.toString();
            tvBtRxData.setText(text);
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    // изменяем состояние BT - CONNECTED
                    mConnectionStatusBT = ConnectionStatusBT.CONNECTED;
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    if (mMainStatus != MainStatus.CLOSE){
                        // если программа не в закрытии
                        // состояние - NO_CONNECT
                        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                        // запускаем процесс установления соединения
                        mMainStatus = MainStatus.CONNECTING;
                        // закрываем потоки ввода вывода для BT
                        closeBtStreams();
                        // установка красного цвета для кнопки FabConnect
                        setFabConnectColorRed();
                        // делаем попытку снова установить соединение через 12 с
                        pbConnectHeader(fabConnect, 12000);
                    }
                    Toast.makeText(getApplicationContext(),
                            "Соединение разорвано", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    if (mMainStatus != MainStatus.CLOSE) {
                        // состояние - NO_CONNECT
                        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                        // запускаем процесс установления соединения
                        mMainStatus = MainStatus.CONNECTING;
                        // закрываем потоки ввода вывода для BT
                        closeBtStreams();
                        // установка красного цвета для кнопки FabConnect
                        setFabConnectColorRed();
                        // делаем попытку снова установить соединение через 12 с
                        pbConnectHeader(fabConnect, 12000);
                    }
                    Toast.makeText(getApplicationContext(),
                            "Разрыв соединения от базовой станции", Toast.LENGTH_SHORT).show();
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
            setFabConnectColorRed();
        }
        else if(requestCode == SET_SETTINGS){
            if (data != null){
                Toast.makeText(getApplicationContext(), data.getStringExtra("address"), Toast.LENGTH_SHORT).show();
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
        tvBtRxData = (TextView) findViewById(R.id.tvBtRxData);
        tvBtRxData.setOnClickListener(this);

        // регистрация активностей
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));

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
        //endregion
        // определяем Handler для соединения по Bluetooth
        //region btConnectHandler
        btConnectHandler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what) {
                    case BT_CONNECT_OK:
                        // TODO - UUID нужно выбирать минимальный и устанавливать связь с ним
                        // если соединение все еще активно
                        if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
                            // обнуляем счетчик попыток установления связи
                            mConnectionAttemptsCnt = 0;
                            // запускаем прием  от SIM
                            listenMessageBT();
                            // передаем данные для начала работы с SIM
                            sendDataBT(BT_INIT_MESSAGE);
                        }
                        break;
                    case BT_CONNECT_ERR:
                        if (mMainStatus != MainStatus.CLOSE){
                            // установка красного цвета для кнопки FabConnect
                            setFabConnectColorRed();
                            // получаем данные сообщения
                            String rxMsg = (String) msg.obj;
                            // проверка получено ли rxMsg
                            if (rxMsg != null){
                                // получено NO_BOUNDED_DEVICE ???
                                if (rxMsg.equals(NO_BOUNDED_DEVICE)){
                                    // если получено NO_BOUNDED_DEVICE, выдать предупреждение
                                    Toast.makeText(getApplicationContext(), R.string.noBoundedDevice, Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    // значит получено BT_CONNECT_ERR (других пока нет)
                                    if (mConnectionAttemptsCnt <= MAX_CONNECTION_ATTEMPTS){
                                        // делаем попытку снова установить соединение через 12 с
                                        pbConnectHeader(fabConnect, 12000);
                                    }
                                    else{
                                        // все попытки установки соединения закончились неудачей
                                        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;  // соединение не установлено
                                        mMainStatus = MainStatus.IDLE;      // сбрасываем соединение
                                        mConnectionAttemptsCnt = 0;         // счетчик попыток сбрасываем в 0
                                        fabConnect.setEnabled(true);     // кнопка поиска устройств активна
                                        // если получено NO_BOUNDED_DEVICE, выдать предупреждение
                                        Toast.makeText(getApplicationContext(), "Ошибка установления связи", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else{
                                // ошибочное сообщение, переходим в исходное состояние
                                mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;  // соединение не установлено
                                mMainStatus = MainStatus.IDLE;      // сбрасываем соединение
                                mConnectionAttemptsCnt = 0;         // счетчик попыток сбрасываем в 0
                                fabConnect.setEnabled(true);     // кнопка поиска устройств активна
                            }
                        }
                        break;
                    case BT_CONNECTED:
                        Toast.makeText(getApplicationContext(), R.string.connectionActiv, Toast.LENGTH_SHORT).show();
                        break;
                    case BT_CONNECT_INTERRUPT:
                        Toast.makeText(getApplicationContext(),
                                "Прерывание соединения", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        //endregion
        // определяем Handler для передачи по Bluetooth
        //region btTxHandler
        btTxHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what) {
                    case BT_TX_OK:
                        // TODO - перенести listenMessageBT так чтобы был только один запущенный
                        Toast.makeText(getApplicationContext(),
                                R.string.DataTransmitted, Toast.LENGTH_SHORT).show();
                        break;
                    case BT_TX_ERR:
                        if (mMainStatus == MainStatus.CONNECTING) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.TransmitionError,
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case BT_TX_INTERRUPT:
                        Toast.makeText(getApplicationContext(),
                                "Прерывание передачи", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        //endregion
        // определяем Handler для приема по Bluetooth
        //region btRxHandler
        btRxHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what) {
                    case BT_RX_OK_ANSWER:
                        String rxText = (String) msg.obj;
                        // если получен RX_INIT_OK и это первое соединение
                        if (mMainStatus == MainStatus.CONNECTING) {
                            if (rxText.equals(RX_INIT_OK)){
                                // изменяем основное состояние
                                mMainStatus = MainStatus.CONNECTED;
                                btRxCnt = 0;
                                // установка зеленого цвета для кнопки FabConnect
                                setFabConnectColorGreen();
                            }
                        }
                        // обновляем значение текста в окне
                        String text = String.valueOf(btRxCnt) + " " + (String) msg.obj;
                        tvBtRxData.setText(text);
                        btRxCnt++;
                        break;
                    case BT_RX_ERR_ANSWER:
                        Toast.makeText(getApplicationContext(),
                                getResources().getText(R.string.RecieveError),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case BT_RX_INTERRIPT:
                        Toast.makeText(getApplicationContext(),
                                "Прерывание приема", Toast.LENGTH_SHORT).show();
                        return;
                }
                if (mMainStatus != MainStatus.CLOSE){
                    // снова запускаем прием данных
                    if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED)
                        listenMessageBT();
                }
            };
        };
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
        // проверяем какая кнопка нажата
        switch (v.getId()){
            case R.id.fabConnect:
                pbConnectHeader(fabConnect, 0);
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

    // обработка нажатия кнопки fabConnect
    public void pbConnectHeader(View view, int delay_ms){
        // TODO - удалить View из описания метода
        final int delay = delay_ms;
        // получаем список спаренных устройств
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        // запускаем поиск сигнализации если mMainStatus = IDLE
        if (mMainStatus == MainStatus.IDLE){
            // выводим сообщение о начале поиска "Базового блока"
            Toast.makeText(getApplicationContext(), "Запущен поиск сигнализации", Toast.LENGTH_SHORT).show();
            mMainStatus = MainStatus.CONNECTING;   // переходим в установку соединения
            fabConnect.setEnabled(false);     // кнопка не активна на время поиска устройств
        }
        setFabConnectColorBlue();   // синий цвет для кнопки FabConnect
        mConnectionStatusBT = ConnectionStatusBT.CONNECTING;    // переходим в режим CONNECTING
        mConnectionAttemptsCnt++;       // увеличиваем счетчик попыток установления соединения
        // создаем поток в котором будет производится поиск "Базового блока"
        bluetooth_Connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // проверяем включен ли Bluetooth
                    if (mBluetoothAdapter.isEnabled()) {
                        String selectedBoundedDevice = sPref.getString(SELECTED_BOUNDED_DEV, "");
                        // проверим определено ли устройство для связи
                        if (selectedBoundedDevice.equals("")){
                            // устройство для связи не выбрано
                            btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECT_ERR, 0, 0, NO_BOUNDED_DEVICE));
                            return;
                        }
                        // ожидаем t=delay мс
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // Bluetooth включен, проверяем устанавливалось ли ранее соединение
                        if ( mClientSocket == null ) {
                            // попытка установить соединение не производилась
                            // производим поиск нужного нам устройства, получаем его
                            mBluetoothDevice = findBTDevice(selectedBoundedDevice);
                            ParcelUuid[] uuid = mBluetoothDevice.getUuids();// получаем перечень доступных UUID данного устройства
                            // устанавливаем связь, создаем Socket
                            mClientSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid[11].getUuid());
                        }
                        // проверка установлено ли соединение
                        if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
                            btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECTED, 0, 0, selectedBoundedDevice));
                        }
                        else {
                            // соединения нет, пробуем снова установить его
                            mClientSocket.connect();
                            btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECT_OK, 0, 0, selectedBoundedDevice));
                        }
                    }
                    else {
                        // Bluetooth выключен. Предложим пользователю включить его.
                        startActivityForResult(new Intent(actionRequestEnable), REQUEST_ENABLE_BT);
                    }
                }
                catch (IOException e) {
                    if (mMainStatus == MainStatus.CLOSE){
                        // завершение работы программы
                        // передаем сообщение - прерывание связи
                        btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECT_INTERRUPT, 0, 0, CONNECTION_ERR));
                    }
                    else{
                        // ошибка установления связи
                        // передаем сообщение - ошибка установления связи
                        btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECT_ERR, 0, 0, CONNECTION_ERR));
                    }
                }
            }
        });
        bluetooth_Connect.setDaemon(true);
        bluetooth_Connect.start();
    }

    /**
     *  поиск устройства Bluetooth по имени
     *  params: String addressDev - адрес, который будет искаться в спаренных усройствах
     */
    public BluetoothDevice findBTDevice (String addressDev) {
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
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN: // Пользователь нажал на экран, т.е. начало движения
                // fromPosition - координата по оси X начала выполнения операции
                fromPosition = event.getX();
                break;
            case MotionEvent.ACTION_UP: // Пользователь отпустил экран, т.е. окончание движения
                float toPosition = event.getX();
                if (fromPosition > toPosition){
                    flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.go_next_in));
                    flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.go_next_out));
                    flipper.showNext();
                }
                else if (fromPosition < toPosition) {
                    flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.go_prev_in));
                    flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.go_prev_out));
                    flipper.showPrevious();
                }
            default:
                break;
        }
        return true;
    }

    // передача данных через Bluetooth
    private void sendDataBT(String d){
        final String txdata = d;
        bluetooth_Tx = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mClientSocket != null) {
                        if (mOutStream == null)
                            mOutStream = mClientSocket.getOutputStream();
                        byte[] byteArray = txdata.getBytes();
                        mOutStream.write(byteArray);
                        btTxHandler.sendEmptyMessage(BT_TX_OK);
                    }
                    else {
                        btTxHandler.sendEmptyMessage(BT_TX_ERR);
                    }
                } catch(IOException e){
                    btTxHandler.sendEmptyMessage(BT_TX_INTERRUPT);
                }
            }
        });
        bluetooth_Tx.setDaemon(true);
        bluetooth_Tx.start();
    }

    // привем данных из Bluetooth
    private void listenMessageBT() {
        bluetooth_Rx = new Thread(new Runnable() {
            String result = "";
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            @Override
            public void run() {
                try {
                    if (mClientSocket != null) {
                        if (mInStream == null)
                            mInStream = mClientSocket.getInputStream();
                    }
                    else {
                        btRxHandler.sendEmptyMessage(BT_RX_ERR_ANSWER);
                        return;
                    }
                    int bytesRead = -1;
                    while (true) {
                        int numBytes = mInStream.available();
                        if (numBytes != 0)
                            if(mClientSocket != null){
                                bytesRead = mInStream.read(buffer);
                                if (bytesRead != -1) {
                                    while (bytesRead == bufferSize){
                                        result = result + new String(buffer, 0, bytesRead);
                                        bytesRead = mInStream.read(buffer);
                                    }
                                    result = result + new String(buffer, 0, bytesRead);
                                    btRxHandler.sendMessage(btRxHandler.obtainMessage(BT_RX_OK_ANSWER, 0, 0, result));
                                    break;
                                }
                            }
                    }
                } catch (IOException e) {
                    btRxHandler.sendEmptyMessage(BT_RX_INTERRIPT);
                }
            }
        });
        bluetooth_Rx.setDaemon(true);
        bluetooth_Rx.start();
    }

    // обработка нажатия кнопки close
    public void pbCloseHeader(View view){
        sendDataBT(SET_ALARM);
    }

    // обработка нажатия кнопки Open
    public void pbOpenHeader(View view){
        sendDataBT(CLEAR_ALARM);
    }

    // обработка нажатия кнопки Mute
    public void pbClearAlarmTriggered(View view){
        sendDataBT(CLEAR_ALARM_TRIGGERED);
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
    }

    /**
     * закрытие потоков ввода вывода для BT
     */
    private void closeBtStreams(){
        try{
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
            mBluetoothDevice = null;
            // завершаем нами созданные потоки для BT
            if (bluetooth_Connect != null) {
                Thread bt_Connect = bluetooth_Connect;
                bluetooth_Connect = null;
                bt_Connect.interrupt();
            }
            if (bluetooth_Rx != null) {
                Thread bt_Rx = bluetooth_Rx;
                bluetooth_Rx = null;
                bt_Rx.interrupt();
            }
            if (bluetooth_Tx != null) {
                Thread bt_Tx = bluetooth_Tx;
                bluetooth_Tx = null;
                bt_Tx.interrupt();
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
        super.onDestroy();
        // Освободить все ресурсы, включая работающие потоки,
        // соединения с БД и т. д.
        mMainStatus = MainStatus.CLOSE;
        closeBtStreams();
    }
}

