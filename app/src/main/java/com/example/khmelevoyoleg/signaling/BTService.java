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
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BTService extends Service{

    // инициализация переменных
    //region InitVar
    public static MainStatus btMainStatus; // состояние подключения по Bluetooth

    static BluetoothSocket btClientSocket;
    static BluetoothAdapter btBluetoothAdapter;
    static BluetoothDevice btBluetoothDevice;
    static BTConnect bluetoothConnect;   // задача установления соединения

    BTBinder binder = new BTBinder();

    // тип перечисления состояний по Bluetooth
    static enum MainStatus {
        CLOSE,
        IDLE,
        CONNECTING,
        CONNECTED,
    }
    static enum CommandBT {
        CMDBT_REQUEST_ENABLE_BT,
        CMDBT_BIND_OK,
        CMDBT_SET_IDLE_STATE,
        CMDACT_SET_SEARCH_STATE
    }
    static enum CommandActivity {
        CMDACT_SET_CONNECT,
        CMDACT_SET_IDLE,
        CMDACT_SET_SEARCH
    }

    // тип перечисления состояний по Bluetooth
    static enum ConnectionStatusBT {
        CONNECTING,
        NO_CONNECT,
        CONNECTED
    }

    private SharedPreferences sp; // настройки приложения
    public int test = 0;
    public Set<BluetoothDevice> pairedDevices;     // множество спаренных устройств
    public ConnectionStatusBT mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT; // состояние подключения по Bluetooth
    int mConnectionAttemptsCnt = 0;         // счетчик попыток подключения по Bluetooth

    ArrayList<CommandBT> commandBt = new ArrayList<>(); // команды от BT к основной программе
    static ArrayList<CommandActivity> commandActivity = new ArrayList<>(); // команды от Activity к сервису

    static final int TIMER_MEDIA_PLAYER = 10000;  // периодичность вызова runMediaPlayer
    static final int TIMER_ACTIVITY_TASK = 250;   // периодичность вызова runActivityTask

    final String LOG_TAG = "myLogs";

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
    Handler timerActivityTaskHandler;
    Runnable runActivityTask = new Runnable() {
        @Override
        public void run() {
            // выполняем полученные команды
            getCommandActivity();
            timerActivityTaskHandler.postDelayed(runActivityTask, TIMER_ACTIVITY_TASK);
        }
    };
    // endregion

    // Getter на вес клас
    class BTBinder extends Binder {
        BTService getService() {
            return BTService.this;
        }
    }

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
                        mConnectionStatusBT = ConnectionStatusBT.CONNECTED;
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        if (btMainStatus != MainStatus.CLOSE && btMainStatus != MainStatus.IDLE) {
                            // если программа не в закрытии и не в исходном состоянии
                            // состояние - CONNECTING
                            mConnectionStatusBT = ConnectionStatusBT.CONNECTING;
                            // запускаем процесс установления соединения
                            btMainStatus = MainStatus.CONNECTING;
                            returnIdleStateBT(false);
                            // перевести активити в исходное сотояние
                            sendCommandBT(CommandBT.CMDACT_SET_SEARCH_STATE);
                        } else {
                            mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                        }
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                        if (btMainStatus != MainStatus.CLOSE) {
                            // переходим в исходное состояние
                            Log.d(LOG_TAG, "ACTION_ACL_DISCONNECT_REQUESTED");
                            returnIdleStateBT(true);
                            // перевести активити в исходное сотояние
                            sendCommandBT(CommandBT.CMDBT_SET_IDLE_STATE);
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
        // получаем SharedPreferences, которое работает с файлом настроек
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        btBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // создаем задачу соединения BTConnect
        bluetoothConnect = new BTConnect();
        // передаем ссылку на основную activity
        bluetoothConnect.link(this);

        mediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mediaPlayer.start();
        // запускаем таймер просмотра состояний программы
        timerMediaPlayerHandler = new Handler();
        timerMediaPlayerHandler.postDelayed(runMediaPlayer, TIMER_MEDIA_PLAYER);
        // запускаем таймер исполнения команд от Activity
        timerActivityTaskHandler = new Handler();
        timerActivityTaskHandler.postDelayed(runActivityTask, TIMER_ACTIVITY_TASK);

        // регистрация активностей
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
    }
    /**
     * возврат в исходное состояние
     */

    // выполнение всех команд пришедших от Activity
    private void getCommandActivity(){
        while (commandActivity.size() != 0) {
            switch (commandActivity.get(0)) {
                case CMDACT_SET_CONNECT:
                    // команда установить соединение по BT
                    btMainStatus = BTService.MainStatus.CONNECTING;   // переходим в установку соединения
                    mConnectionStatusBT = ConnectionStatusBT.CONNECTING;    // переходим в режим CONNECTING
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
            }
            commandActivity.remove(0);
        }
    }


    public void returnIdleStateBT(boolean mode){
        // true - полное закрытие всего
        if (mode) {
            btMainStatus = MainStatus.IDLE;  // состояние IDLE
            //btRxCnt = 0;
            //necessaryInitMessage = 0;
            mConnectionStatusBT = ConnectionStatusBT.NO_CONNECT;    // состояние BT = NO_CONNECT
            closeBtStreams();   // закрываем все потоки
            /*
            if (mBTDataTx != null)
                mBTDataTx.clear();  // очищаем очередь передачи по BT
             */
        }
        else {
            // false - переход в режим поиска
            mConnectionAttemptsCnt = 0; // счетчик попыток соединения в 0
        }
        mConnectionAttemptsCnt = 0; // счетчик попыток соединения в 0
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
            /*
            if (bluetooth_Tx != null) {
                // завершаем задачу передачи
                bluetooth_Tx.cancel(true);
            }
            if (bluetooth_Rx != null) {
                // завершаем задачу приема
                bluetooth_Rx.cancel(true);
            }
            */
            // закрываем сокет и потоки ввода вывода
            if (BTService.btClientSocket != null) {
                BTService.btClientSocket.close();
                BTService.btClientSocket = null;
            }
            /*
            if (mInStream != null) {
                mInStream.close();
                mInStream = null;
            }
            if (mOutStream != null) {
                mOutStream.close();
                mOutStream = null;
            }
            */
        }
        catch (IOException e) {
            // состояние - NO_CONNECT
            mConnectionStatusBT = BTService.ConnectionStatusBT.NO_CONNECT;
            Toast.makeText(getApplicationContext(),
                    "Ошибка Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // даем команду что подключение успешно выполнено
        sendCommandBT(CommandBT.CMDBT_BIND_OK);

        Log.d(LOG_TAG, "BTService onBind");
        return binder;
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
        if (bluetoothConnect.getStatus().toString().equals(Utils.FINISHED)) {
            bluetoothConnect = new BTConnect();
            // передаем ссылку на основную activity
            bluetoothConnect.link(this);
        }
        //fabConnect.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        // получаем список спаренных устройств
        pairedDevices = btBluetoothAdapter.getBondedDevices();
        mConnectionAttemptsCnt++;       // увеличиваем счетчик попыток установления соединения
        try {
            UUID serialUUID;
            // проверяем включен ли Bluetooth
            if (btBluetoothAdapter.isEnabled()) {
                String selectedBoundedDevice = sp.getString(Utils.SELECTED_BOUNDED_DEV, "");
                // проверим определено ли устройство для связи
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
                if (!setConnect) {
                    // переходим в исходное состояние поскольку запуск соединения не произведен
                    returnIdleStateBT(true);
                    // выдаем сообщение "Устройство для связи не выбрано."
                    Toast.makeText(getApplicationContext(), R.string.noBoundedDevice, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                // Bluetooth выключен. Предложим пользователю включить его.
                sendCommandBT(CommandBT.CMDBT_REQUEST_ENABLE_BT);
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
        // если соединение активно
        if (mConnectionStatusBT == ConnectionStatusBT.CONNECTED){
            // проверяем результат работы задачи bluetoothConnect
            if (result.equals(BTConnect.CONNECTION_OK)) {
                // обнуляем счетчик попыток установления связи
                mConnectionAttemptsCnt = 0;
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

    /**
     *  команда в основную программу
     *  params: String command - команда, которую нужно выполнить в основной программе
     */
    private void sendCommandBT(CommandBT command){
        commandBt.add(command);
    }

}

