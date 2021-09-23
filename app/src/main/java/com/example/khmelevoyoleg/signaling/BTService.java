package com.example.khmelevoyoleg.signaling;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BTService extends Service{

    // инициализация переменных
    //region InitVar
    final String ACTION_DATA_FROM_SERVICE = "com.example.khmelevoyoleg.signaling:btprocess";
    final String ACTION_DATA_FROM_ACTIVITY = "com.example.khmelevoyoleg.signaling";

    MainStatus btMainStatus; // состояние подключения по Bluetooth

    static BluetoothSocket btClientSocket;
    BluetoothAdapter btBluetoothAdapter;
    static BluetoothDevice btBluetoothDevice;
    static BTConnect bluetoothConnect;   // задача установления соединения
    BTTx bluetooth_Tx;        // поток передачи
    BTRx bluetooth_Rx;        // поток приема
    OutputStream mOutStream;  // поток по передаче bluetooth
    InputStream mInStream;    // поток по приему bluetooth
    long btRxCnt;
    static boolean boundBT;          // наличие связи с основной программой

    Handler timerBTRxHandler;       // обработчик таймера

    // тип перечисления состояний по Bluetooth
    enum MainStatus {
        CLOSE,
        IDLE,
        CONNECTING,
        CONNECTED,
    }
    final String CMDBT_REQUEST_ENABLE_BT = "CMDBT_REQUEST_ENABLE_BT";
    final String CMDBT_BIND_OK = "CMDBT_BIND_OK";
    final String CMDBT_SET_IDLE_STATE = "CMDBT_SET_IDLE_STATE";
    final String CMDBT_SET_SEARCH_STATE = "CMDBT_SET_SEARCH_STATE";
    final String CMDBT_SET_CONNECTED_STATE = "CMDBT_SET_CONNECTED_STATE";
    final String CMDBT_ANALIZE_BT_DATA = "CMDBT_ANALIZE_BT_DATA";
    final String CMDBT_CONNECTION_IMPOSSIBLE = "CMDBT_CONNECTION_IMPOSSIBLE";
    final String CMDBT_CONNECTION_ERROR = "CMDBT_CONNECTION_ERROR";

    final String CMDACT_SET_CONNECT = "CMDACT_SET_CONNECT";
    final String CMDACT_SET_IDLE = "CMDACT_SET_IDLE";
    final String CMDACT_SET_SEARCH = "CMDACT_SET_SEARCH";
    final String CMDACT_SEND_DATA_BT = "CMDACT_SEND_DATA_BT";
    final String CMDACT_SEND_DATA_BT_ANYWAY = "CMDACT_SEND_DATA_BT_ANYWAY";

    // передать данные без проверки готовности передатчика
    // тип перечисления состояний по Bluetooth
    enum ConnectionStatusBT {
        CONNECTING,
        NO_CONNECT,
        CONNECTED
    }

    ConnectionStatusBT connectionStatusBT = ConnectionStatusBT.NO_CONNECT; // состояние подключения по Bluetooth

    private SharedPreferences sp; // настройки приложения
    public int test = 0;
    public Set<BluetoothDevice> pairedDevices;     // множество спаренных устройств

    static ArrayList<String> dataToSent = new ArrayList<>(); // данные для передачи в BT
    static ArrayList<Integer> delayToSent = new ArrayList<>(); // задержка перед передачей данных в BT
    ArrayList<String> btTxData = new ArrayList<>(); // данные для передачи в BT
    ArrayList<String> dataToAnalize = new ArrayList<>(); // данные принятые через BT для анализа

    static final int TIMER_MEDIA_PLAYER = 10000;  // периодичность вызова runMediaPlayer
    static final int TIMER_ACTIVITY_TASK = 250;   // периодичность вызова runBTTxTask

    final String LOG_TAG = "SERVICE_LOG";

    MediaPlayer mediaPlayer;
    Handler timerMediaPlayerHandler;
    Runnable runMediaPlayer = new Runnable() {
        @Override
        public void run() {
            mediaPlayer.start();
            timerMediaPlayerHandler.postDelayed(runMediaPlayer, TIMER_MEDIA_PLAYER);
        }
    };

    // исполнение команд от Activity
    Handler timerBTTxHandler;
    Runnable runBTTxTask = new Runnable() {
        @Override
        public void run() {
            // отправляем в BT полуенные команды
            sendBT(null);
            timerBTTxHandler.postDelayed(runBTTxTask, TIMER_ACTIVITY_TASK);
        }
    };

    /**
     * запуск приема по BT
     */
    Runnable runListenMessageBT = new Runnable() {
        @Override
        public void run() {
            listenMessageBT();
        }
    };
    // endregion

    // приемник широковещательных событий
    // region BroadcastReceiver
    BroadcastReceiver connectionStatus = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        // изменяем состояние BT - CONNECTED
                        connectionStatusBT = ConnectionStatusBT.CONNECTED;
                        sendMessageToActivity(null);
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        if (btMainStatus != MainStatus.CLOSE && btMainStatus != MainStatus.IDLE) {
                            // если программа не в закрытии и не в исходном состоянии
                            // состояние - CONNECTING
                            connectionStatusBT = ConnectionStatusBT.CONNECTING;
                            // запускаем процесс установления соединения
                            btMainStatus = MainStatus.CONNECTING;
                            returnIdleStateBT(false);
                            // перевести активити в исходное сотояние
                            sendMessageToActivity(CMDBT_SET_SEARCH_STATE);
                        } else {
                            connectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                            sendMessageToActivity(null);
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                        if (btMainStatus != MainStatus.CLOSE) {
                            // переходим в исходное состояние
                            Log.d(LOG_TAG, "ACTION_ACL_DISCONNECT_REQUESTED");
                            returnIdleStateBT(true);
                            // перевести активити в исходное сотояние
                            sendMessageToActivity(CMDBT_SET_IDLE_STATE);
                        }
                        break;
                }
            }
            catch (NullPointerException EXP){
                Log.d(LOG_TAG, "NullPointerException");
            }
        }
    };
    //endregion

    @Override
    public void onCreate(){
        super.onCreate();
        btMainStatus = MainStatus.IDLE;
        // получаем SharedPreferences, которое работает с файлом настроек
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        btBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // создаем задачу соединения BTConnect
        bluetoothConnect = new BTConnect();
        // передаем ссылку на основную activity
        bluetoothConnect.link(this);

//        mediaPlayer = MediaPlayer.create(this, R.raw.sound);
//        mediaPlayer.start();
        // запускаем таймер просмотра состояний программы
//        timerMediaPlayerHandler = new Handler();
//        timerMediaPlayerHandler.postDelayed(runMediaPlayer, TIMER_MEDIA_PLAYER);
        // запускаем таймер исполнения команд от Activity
        timerBTTxHandler = new Handler();
        timerBTTxHandler.postDelayed(runBTTxTask, TIMER_ACTIVITY_TASK);
        // запускаем таймер просмотра состояний программы
        timerBTRxHandler = new Handler();

        // регистрация активностей
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
        registerReceiver(mMessageReceiver, new IntentFilter(ACTION_DATA_FROM_ACTIVITY));

        Log.d(LOG_TAG, "BTService start");
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String dataFromAct = intent.getStringExtra("dataFromAct");
            if (dataFromAct != null)
                dataToSent.add(dataFromAct);
            String commandFromAct = intent.getStringExtra("commandFromAct");
            doCommandActivity(commandFromAct);
            Log.d(LOG_TAG, String.format("Got message: %s, data: %s", commandFromAct, dataFromAct));
        }
    };

    public void sendMessageToActivity(String message) {
        sendMessageToActivity(message, null);
    }

    public void sendMessageToActivity(String message, String data) {
        Intent intent = new Intent();
        intent.setAction(ACTION_DATA_FROM_SERVICE);
        if (message != null)
            intent.putExtra("commandFromBT", message);
        if (data != null)
            intent.putExtra("dataBT", data);
        intent.putExtra("btMainStatus", btMainStatus.toString());
        intent.putExtra("connectionStatusBT", connectionStatusBT.toString());
        sendBroadcast(intent);
    }

    // выполнение всех команд пришедших от Activity
    private void doCommandActivity(String command){
        switch (command) {
            case CMDACT_SET_CONNECT:
                // команда установить соединение по BT
                if (btMainStatus != MainStatus.CONNECTING){
                    btMainStatus = MainStatus.CONNECTING;   // переходим в установку соединения
                    connectionStatusBT = ConnectionStatusBT.CONNECTING;    // переходим в режим CONNECTING
                    sendMessageToActivity(null);
                }
                // запускаем установление соединения по BT
                startConnection();
                break;
            case CMDACT_SET_IDLE:
                // команда перейти в состояние IDLE
                returnIdleStateBT(true);
                break;
            case CMDACT_SET_SEARCH:
                // команда перейти в состояние SEARCH
                returnIdleStateBT(false);
                break;
            case CMDACT_SEND_DATA_BT:
                // передаем самые старые данные
                if (delayToSent.size() > 0) {
                    sendBT(dataToSent.get(0), delayToSent.get(0));
                    delayToSent.remove(0);
                }
                else
                    sendBT(dataToSent.get(0), 0);
                dataToSent.remove(0);
                break;
            case CMDACT_SEND_DATA_BT_ANYWAY:
                // передаем самые старые данные
                if (delayToSent.size() > 0) {
                    sendBT(dataToSent.get(0), delayToSent.get(0), true);
                    delayToSent.remove(0);
                }
                else
                    sendBT(dataToSent.get(0), 0, true);
                dataToSent.remove(0);
                // Запускаем прослушивание приема по BT
                listenMessageBT();
                break;
        }
    }

    public void returnIdleStateBT(boolean mode){
        // true - полное закрытие всего
        if (mode) {
            btMainStatus = MainStatus.IDLE;  // состояние IDLE
            connectionStatusBT = ConnectionStatusBT.NO_CONNECT;    // состояние BT = NO_CONNECT
            sendMessageToActivity(null);
            closeBtStreams();   // закрываем все потоки
            if (dataToSent != null)
                dataToSent.clear();  // очищаем очередь передачи по BT
            if (delayToSent != null)
                delayToSent.clear();  // очищаем очередь передачи по BT
            if (btTxData != null)
                btTxData.clear();  // очищаем очередь передачи по BT
        }
        else {
            // false - переход в режим поиска
        }
    }

    /**
     * закрытие потоков ввода вывода для BT
     */
    private void closeBtStreams(){
        try{
            btBluetoothDevice = null;
            if (bluetoothConnect != null) {
                // завершаем задачу установки соединения
                bluetoothConnect.cancel(true);
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
            if (btClientSocket != null) {
                btClientSocket.close();
                btClientSocket = null;
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
            connectionStatusBT = BTService.ConnectionStatusBT.NO_CONNECT;
            Toast.makeText(getApplicationContext(),
                    "Ошибка Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // даем команду что подключение успешно выполнено
        boundBT = true;
        sendMessageToActivity(CMDBT_BIND_OK);
        Log.d(LOG_TAG, "BTService onBind");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(LOG_TAG, "BTService onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "BTService onUnbind");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "BTService onStartCommand");
        return START_STICKY; // ервис будет восстановлен после убийства его системой
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "BTService onDestroy");
    }

    /**
     * обработка нажатия кнопки fabConnect
     */
    private void startConnection(){
        String address;
        boolean setConnect = false;
        // создаем асинхронную задачу соединения если прошлая уже отработала
        if (bluetoothConnect == null | bluetoothConnect.getStatus().toString().equals(Utils.FINISHED)) {
            bluetoothConnect = new BTConnect();
            // передаем ссылку на основную activity
            bluetoothConnect.link(this);
        }
        //fabConnect.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        // получаем список спаренных устройств
        pairedDevices = btBluetoothAdapter.getBondedDevices();
        try {
            UUID serialUUID;
            // проверяем включен ли Bluetooth
            if (btBluetoothAdapter.isEnabled()) {
                String selectedBoundedDevice = sp.getString(Utils.SELECTED_BOUNDED_DEV, "");
                // проверим определено ли устройство для связи
                if ( !selectedBoundedDevice.equals("")) {
                    for (BluetoothDevice device : pairedDevices) {
                        address = device.getAddress();
                        if (address != null)
                            if (selectedBoundedDevice.equals(address) || selectedBoundedDevice.equals("")) {
                                // Bluetooth включен, проверяем устанавливалось ли ранее соединение
                                if (btClientSocket == null) {
                                    // попытка установить соединение не производилась
                                    // производим поиск нужного нам устройства, получаем его
                                    btBluetoothDevice = findBTDevice(selectedBoundedDevice);
                                    if (btBluetoothDevice != null) {
                                        ParcelUuid[] uuids = btBluetoothDevice.getUuids();// получаем перечень доступных UUID данного устройства
                                        if (uuids != null) {
                                            serialUUID = new UUID(0, 0);
                                            for (ParcelUuid uuid : uuids) {
                                                long profileUUID = uuid.getUuid().getMostSignificantBits();
                                                profileUUID = (profileUUID & Utils.UUID_MASK) >> 32;
                                                if (profileUUID == Utils.UUID_SERIAL)
                                                    serialUUID = uuid.getUuid();
                                            }
                                            // проверяем установлен ли UUID
                                            if (serialUUID.getMostSignificantBits() != 0) {
                                                // устанавливаем связь, создаем Socket
                                                btClientSocket = btBluetoothDevice.createRfcommSocketToServiceRecord(serialUUID);
                                            } else {
                                                // переходим в исходное состояние
                                                returnIdleStateBT(true);
                                                // если получено NO_BOUNDED_DEVICE, выдать предупреждение
                                                Toast.makeText(getApplicationContext(), R.string.noValidBoundedDevice, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                                // запускаем задачу bluetoothConnect если она не запущена
                                if (bluetoothConnect.getStatus().equals(AsyncTask.Status.PENDING))
                                    bluetoothConnect.execute(0);
                                setConnect = true;
                                break;
                            }
                    }
                }
                // проверка запущшено ли соединение
                if (!setConnect) {
                    // переходим в исходное состояние поскольку запуск соединения не произведен
                    returnIdleStateBT(true);
                    // выдаем сообщение "Устройство для связи не выбрано."
                    sendMessageToActivity(CMDBT_CONNECTION_IMPOSSIBLE);
                }
            }
            else {
                // Bluetooth выключен. Предложим пользователю включить его.
                sendMessageToActivity(CMDBT_REQUEST_ENABLE_BT);
            }
        } catch (IOException e) {
            if (btMainStatus == MainStatus.CLOSE) {
                // завершение работы программы
                // выводим сообщение "Соединение разорвано"
                Toast.makeText(getApplicationContext(),
                        R.string.connectionInterrupted, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * метод вызываемый в конце выполнения задачи bluetoothConnect
     */
    void onPostExecuteBTConnect(String result){
        // соединение не установлено?
        if (connectionStatusBT == ConnectionStatusBT.CONNECTING){
            // проверяем результат работы задачи bluetoothConnect
            if (result.equals(BTConnect.CONNECTION_ERROR)) {
                sendMessageToActivity(CMDBT_CONNECTION_ERROR);
            }
        }
    }

    /**
     * проверка возможно ли осуществляь передачу по BT
     */
    boolean checkAbilityTxBT() {
        return btMainStatus == MainStatus.CONNECTED;
    }

    /**
     * метод вызываемый в конце выполнения задачи bluetooth_Tx
     * @param result
     */
    void onPostExecuteBTTx(String result){
        if (result.equals(BTTx.TX_OK)){
            // данные были переданы успешно, удаляем их
            btTxData.remove(0);
        }
    }

    /**
     * передача данных через Bluetooth
     * @param data - данные для передачи
     * @param delay - задержка передачи
     * @param withoutChecking - отправка без проверки checkAbilityTxBT
     */
    void sendBT(String data, Integer delay, boolean withoutChecking){
        // добавляем данные для передачи, если они есть
        if (data != null)
            btTxData.add('>' + data);
        if (checkAbilityTxBT() || withoutChecking) {
            // проверка завершилась ли прошлая задача передачи
            if (btTxData.size() > 0) {
                if (bluetooth_Tx == null || bluetooth_Tx.getStatus() == AsyncTask.Status.FINISHED) {
                    // передача завершилась, создаем новую задачу передачи
                    bluetooth_Tx = new BTTx(btTxData.get(0));
                    // передаем ссылку на основную activity
                    bluetooth_Tx.link(this);
                    // делаем задержку
                    bluetooth_Tx.execute(delay);
                    Log.d(LOG_TAG, "Send end.: " + btTxData.get(0));
                }
            }
        }
    }
    void sendBT(String data) {
        sendBT(data, 0, false);
    }
    void sendBT(String data, Integer delay) {
        sendBT(data, delay, false);
    }


    // прием данных из Bluetooth
    private void listenMessageBT() {
        try {
            if (btClientSocket != null) {
                if (mInStream == null)
                    mInStream = btClientSocket.getInputStream();
                if (bluetooth_Rx == null || bluetooth_Rx.getStatus().toString().equals(Utils.FINISHED)) {
                    // создаем задачу BTRx
                    bluetooth_Rx = new BTRx();
                    // передаем ссылку на основную activity
                    bluetooth_Rx.link(this);
                    // запускаем задачу с задержкой 0 с
                    bluetooth_Rx.execute(0);
                }
            } else Log.d(LOG_TAG, "Rx Error");
        }
        catch (IOException e) {
            returnIdleStateBT(true);
            Log.d(LOG_TAG, "BT_RX_INTERRUPT");
        }
    }

    /**
     * метод вызываемый в конце выполнения задачи bluetooth_Rx
     */
    void onPostExecuteBTRx(String rxData){
        // если соединение активно
        if (connectionStatusBT == BTService.ConnectionStatusBT.CONNECTED){
            // находимся в режиме MainStatus.CONNECTING ???
            if (btMainStatus == MainStatus.CONNECTING) {
                // есть принятые данные?
                if (rxData.length() > 0 ){
                    // принятые данные есть
                    // изменяем основное состояние на CONNECTED
                    btMainStatus = MainStatus.CONNECTED;
                    // перевести активити в  сотояние CONECTED
                    sendMessageToActivity(CMDBT_SET_CONNECTED_STATE);
                }
            }

            // обновляем значение текста в окне
            if (!rxData.contains(Utils.RX_ERROR)){
                // производим анализ полученных данных
                dataToAnalize.add(rxData);
                sendMessageToActivity(CMDBT_ANALIZE_BT_DATA, rxData);
                btRxCnt++;
                Log.d(LOG_TAG, "Recieve:" + rxData);
            }
            else {
                Log.d(LOG_TAG, "Error RX:" + rxData);
            }
        }
        // запускаем новый прием данных
        // вызываем runListenMessageBT с задержкой TIMER_LISTEN_BT в мс.
        timerBTRxHandler.postDelayed(runListenMessageBT, Utils.TIMER_LISTEN_BT);
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
}

