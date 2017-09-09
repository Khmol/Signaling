package com.example.khmelevoyoleg.signaling;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    // тип перечисления состояний по Bluetooth
    private enum ConnectionStatusBT {
        CONNECTING,
        NO_CONNECT,
        CONNECTED
    }
    private enum MainStatus {
        IDLE,
        CONNECTING,
        CONNECTED,
        CLEAR_ALARM_TRIGGERED,
        CLEAR_ALARM,
        SET_ALARM
    }

    // пределяем константы
    private static final String BT_SERVER_NAME = "CAR";
    private static final String BT_SERVER_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private static final String BT_INIT_MESSAGE = "SIMCOMSPPFORAPP";    // посылка для инициализации SIM
    private static final String CLEAR_ALARM_TRIGGERED = "CLEAR ALARM TRIGGERED,1\r";        // посылка для снятия аварии с SIM
    private static final String CLEAR_ALARM = "CLEAR ALARM,1\r";                // посылка для снятия с охраны
    private static final String SET_ALARM = "SET ALARM,1\r";                    // посылка для установки на охрану
    private static final String RX_INIT_OK = "SPP APP OK\r";                    // ответ на BT_INIT_MESSAGE



    // TODO перенести BT_SERVER_NAME в настройки
    // TODO перенести BT_SERVER_UUID в настройки
    private static final int REQUEST_ENABLE_BT = 1;     // запрос включения Bluetooth
    private static final int SET_SETTINGS = 2;          // редактирование настроек
    private static final int BT_CONNECT_OK = 5;         // соединение по Bluetooth установлено успешно
    private static final int BT_CONNECT_ERR = 6;        // ошибка установки соединения по Bluetooth
    private static final int BT_CONNECTED = 7;          // соединения по Bluetooth уже было установлено
    private static final int BT_TX_OK = 8;              // передача по Bluetooth успешно выполнена
    private static final int BT_TX_ERR = 9;             // ошибка передачи по Bluetooth
    private static final int BT_RX_OK_ANSWER = 10;      // прием по Bluetooth успешно выполнен
    private static final int BT_RX_ERR_ANSWER = 11;     // ошибка приема по Bluetooth

    // определяем стринговые константы
    protected String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mClientSocket;

    private ConnectionStatusBT mConnectionStatusBT; // состояние подключения по Bluetooth
    private MainStatus mMainStatus; // состояние подключения по Bluetooth
    private OutputStream mOutStream;                 // поток по передаче bluetooth
    private InputStream mInStream;                   // поток по приему bluetooth
    private FloatingActionButton fabConnect;        // кнопка поиска "Базового блока"
    private ViewFlipper flipper;                    // определяем flipper для перелистываний экрана
    private float fromPosition;                     // позиция касания при перелистывании экранов
    private boolean mBtRxStatus;                    // состояние по приему bluetooth
    private TextView tvBtRxData;                    // текст с принятыми данными
    private Set<BluetoothDevice> pairedDevices;     // множество спаренных устройств

    private ArrayList<String> adressPairedDevices;  // адреса спаренных устройств


    Handler btConnectHandler;   // обработчик сообщений из потока btConnect
    Handler btRxHandler;        // обработчик сообщений из потока bluetooth_Rx
    Handler btTxHandler;        // обработчик сообщений из потока bluetooth_Tx

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flip);

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
        tvBtRxData = (TextView) findViewById(R.id.tvBtRxData);

        // регистрация активностей
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));

        // определяем адаптер
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



        // определяем Handler для соединения по Bluetooth
        btConnectHandler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what) {
                    case BT_CONNECT_OK:
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
                        break;
                    case BT_CONNECT_ERR:
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
                        Toast.makeText(getApplicationContext(), "Ошибка соединения", Toast.LENGTH_SHORT).show();
                        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;        // соединение не установлено
                        mMainStatus = MainStatus.IDLE;      // сбрасываем соединение
                        break;
                    case BT_CONNECTED:
                        Toast.makeText(getApplicationContext(), "Соединение уже активно", Toast.LENGTH_SHORT).show();
                        break;
                }
            };
        };

        // определяем Handler для передачи по Bluetooth
        btTxHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what) {
                    case BT_TX_OK:
                        Toast.makeText(getApplicationContext(),
                                R.string.DataTransmitted, Toast.LENGTH_SHORT).show();
                        break;
                    case BT_TX_ERR:
                        if (mMainStatus == MainStatus.CONNECTING) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.TransmitionError,
                                    Toast.LENGTH_SHORT).show();
                            SendInitMessage();  // отправка посылки для инициализации SIM
                        }
                        break;
                }
            };
        };

        // определяем Handler для приемника данных из Bluetooth
        btRxHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what) {
                    case BT_RX_OK_ANSWER:
                        String rxText = (String) msg.obj;
                        // если получен RX_INIT_OK и это первое соединение
                        if (rxText.equals(RX_INIT_OK) & mMainStatus == MainStatus.CONNECTING) {
                            // ответ получен правильный
                            Toast.makeText(getApplicationContext(),
                                    R.string.DataTransmitted, Toast.LENGTH_SHORT).show();
                            mMainStatus = MainStatus.CONNECTED;
                        }
                        else if (mMainStatus == MainStatus.CONNECTING) {
                            // ошибочный ответ
                            // повторная отправка посылки для инициализации SIM
                            SendInitMessage();  // отправка посылки для инициализации SIM
                        }
                        else if (mMainStatus == MainStatus.CONNECTED){
                            // получены рабочие данные
                        }
                        // обновляем значение текста
                        tvBtRxData.setText((String) msg.obj);
                        break;
                    case BT_RX_ERR_ANSWER:
                        Toast.makeText(getApplicationContext(),
                                getResources().getText(R.string.RecieveError),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                // снова запускаем прием данных
                if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED)
                    listenMessageBT();
            };
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // инициализация меню
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получаем код выбранного пункта меню
        int id = item.getItemId();


        // если выбран пункт меню "Настройки"
        if (id == R.id.action_settings) {
            // получаем список спаренных устройств
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            // новый список имен спаренных устройств
            ArrayList<String> namePairedDevices = new ArrayList<>();
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
                    //String mac_adress = device.getAddress();
                    //adressPairedDevices.add(i, mac_adress);
                    i++;
                }
            }
            intent.putExtra("paired_names", namePairedDevices);
            //intent.putExtra("paired_adresses", adressPairedDevices);
            //intent.putExtra("phone", mTextPhone.getText().toString());
            // запускаем активность
            startActivityForResult(intent, SET_SETTINGS);
            return true;
        }
        // по умолчанию возвращаем обработчик родителя
        return super.onOptionsItemSelected(item);
    }

    // обработка нажатия кнопки fabConnect
    public void pbConnectHeader(View view){
        if (mMainStatus == MainStatus.IDLE){
            // выводим сообщение о начале поиска "Базового блока"
            Toast.makeText(getApplicationContext(), "Запущен поиск сигнализации", Toast.LENGTH_SHORT).show();
        }
        mConnectionStatusBT = ConnectionStatusBT.CONNECTING;
        // создаем поток в котором будет производится поиск "Базового блока"
        Thread bluetooth_Connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // проверяем включен ли Bluetooth
                    if (mBluetoothAdapter.isEnabled()) {
                        // включен, проверяем устанавливалось ли ранее соединение
                        UUID uuid = new UUID(0, 0);
                        if ( mClientSocket == null ) {
                            // не устанавливалось
                            mBluetoothDevice = findBTDevice(BT_SERVER_NAME);
                            uuid = UUID.fromString(BT_SERVER_UUID);
                            mClientSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                            if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
                                btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECTED, 0, 0, BT_SERVER_NAME));
                            }
                            else {
                                // соединения нет, пробуем снова установить его
                                mClientSocket.connect();

                                btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECT_OK, 0, 0, BT_SERVER_NAME));
                            }
                        }
                        else {
                            // соединение устанавливалось, проверка есть ли соединение
                            if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
                                btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECTED, 0, 0, BT_SERVER_NAME));
                            }
                            else {
                                // соединения нет, пробуем снова установить его
                                mClientSocket.connect();
                                btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECT_OK, 0, 0, BT_SERVER_NAME));
                            }
                        }
                    }
                    else {
                        // Bluetooth выключен. Предложим пользователю включить его.
                        startActivityForResult(new Intent(actionRequestEnable), REQUEST_ENABLE_BT);
                    }
                }
                catch (IOException e) {
                    btConnectHandler.sendEmptyMessage(BT_CONNECT_ERR);
                }
            }
        });
        bluetooth_Connect.setDaemon(true);
        bluetooth_Connect.start();
    }

    // отправка посылки для инициализации SIM
    void SendInitMessage(){
        Thread bluetoothInit = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // передаем данные для начала работы с SIM
                sendDataBT(BT_INIT_MESSAGE);
                // запускаем прием сообщений от SIM
                listenMessageBT();
            }
        });
        bluetoothInit.setDaemon(true);
        bluetoothInit.start();
    }

    BroadcastReceiver connectionStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        // если не было ошибки при установке соединения
                        if (mConnectionStatusBT == ConnectionStatusBT.CONNECTING){
                            // изменяем состояние - CONNECTED
                            mConnectionStatusBT = ConnectionStatusBT.CONNECTED;
                            mMainStatus = MainStatus.CONNECTING;   // установка соединения - отправка посылки для инициализации SIM
                            SendInitMessage();      // отправка посылки для инициализации SIM
                        }
                        // в противном случае ничего не делаем, соединение не состоялось
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        // состояние - NO_CONNECT
                        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
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
                        Toast.makeText(getApplicationContext(),
                                "Соединение разорвано", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            setFloatingActionButtonColors(fabConnect,
                                    getResources().getColor(R.color.colorRed, null),
                                    getResources().getColor(R.color.colorCarSame, null));
                        } else {
                            setFloatingActionButtonColors(fabConnect,
                                    getResources().getColor(R.color.colorRed),
                                    getResources().getColor(R.color.colorCarSame));
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                        // состояние - NO_CONNECT
                        mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                        if (mInStream != null)
                            mInStream.close();
                        if (mOutStream != null)
                            mOutStream.close();
                        if (mClientSocket != null)
                            mClientSocket.close();
                        mBluetoothDevice = null;
                        Toast.makeText(getApplicationContext(),
                                "Разрыв соединения от базовой станции", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            setFloatingActionButtonColors(fabConnect,
                                    getResources().getColor(R.color.colorRed, null),
                                    getResources().getColor(R.color.colorCarSame, null));
                        } else {
                            setFloatingActionButtonColors(fabConnect,
                                    getResources().getColor(R.color.colorRed),
                                    getResources().getColor(R.color.colorCarSame));
                        }
                        break;
                }
            } catch (IOException e) {
                // состояние - NO_CONNECT
                mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                Toast.makeText(getApplicationContext(),
                        "Ошибка соединения", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     *  поиск устройства Bluetooth по имени
     *  params: String nameDev - мя которое будет искаться в спаренных усройствах
     */
    public BluetoothDevice findBTDevice (String nameDev) {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                if (name.equals(nameDev)) {
                    return device;
                }
            }
        }
        return null;
    }

    /** обработка результатов намерений */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // проверяем результат какого намерения вернулся
        if (requestCode == REQUEST_ENABLE_BT) {
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
        else if(requestCode == SET_SETTINGS){
            Toast.makeText(getApplicationContext(), data.getStringExtra("adress"), Toast.LENGTH_SHORT).show();
        }
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


    // Вызывается перед уничтожением активности
    @Override
    public void onDestroy() {
        // Освободить все ресурсы, включая работающие потоки,
        // соединения с БД и т. д.
        try {
            if (mInStream != null)
                mInStream.close();
            if (mOutStream != null)
                mOutStream.close();
            if (mClientSocket != null)
                mClientSocket.close();
            mBluetoothDevice = null;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Ошибка закрытия Bluetooth",
                    Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
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
        Thread bluetooth_Tx = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mClientSocket != null) {
                        if (mOutStream == null)
                            mOutStream = mClientSocket.getOutputStream();
                        byte[] byteArray = (txdata + " ").getBytes();
                        mOutStream.write(byteArray);
                        btTxHandler.sendEmptyMessage(BT_TX_OK);
                    }
                    else {
                        btTxHandler.sendEmptyMessage(BT_TX_ERR);
                    }
                } catch(IOException e){
                    btTxHandler.sendEmptyMessage(BT_TX_ERR);
                }
            }
        });
        bluetooth_Tx.setDaemon(true);
        bluetooth_Tx.start();
    }

    // привем данных из Bluetooth
    private void listenMessageBT() {

        Thread bluetooth_Rx = new Thread(new Runnable() {
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
                    mBtRxStatus = true;     // находимся в состянии приема
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
                    btRxHandler.sendEmptyMessage(BT_RX_ERR_ANSWER);
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

}

