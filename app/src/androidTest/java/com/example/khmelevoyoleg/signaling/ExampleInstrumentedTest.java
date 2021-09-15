package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.khmelevoyoleg.signaling", appContext.getPackageName());
    }
}

    /*
    new CountDownTimer(500, 100) {
        public void onFinish() {
            // выводим сообщение "Запущен поиск сигнализации"
            Toast.makeText(getApplicationContext(), R.string.connectionStart, Toast.LENGTH_SHORT).show();
            BTService.btMainStatus = BTService.MainStatus.CONNECTING;   // переходим в установку соединения
            connectionStatusBT = BTService.ConnectionStatusBT.CONNECTING;    // переходим в режим CONNECTING
            // подключаемся к сервису serviceBT
            if (!boundBT)
                bindService(serviceBT, sConnBT, 0);
            // запускаем поиск по BT
            //pbConnectHeader();     // запускаем установление соединения
        }
        public void onTick(long millisUntilFinished) {
            // millisUntilFinished    The amount of time until finished.
        }
    }.start();
     */


/**
 * обработка нажатия кнопки fabConnect
 */
    /*
    private void pbConnectHeader(){
        String address;
        boolean setConnect = false;
        //fabConnect.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        // получаем список спаренных устройств
        pairedDevices = BTService.btBluetoothAdapter.getBondedDevices();
        mConnectionAttemptsCnt++;       // увеличиваем счетчик попыток установления соединения
        try {
            UUID serialUUID;
            // проверяем включен ли Bluetooth
            if (BTService.btBluetoothAdapter.isEnabled()) {
                // сервис запущен
                String selectedBoundedDevice = sp.getString(Utils.SELECTED_BOUNDED_DEV, "");
                // проверим определено ли устройство для связи
                for (BluetoothDevice device : pairedDevices) {
                    address = device.getAddress();
                    if (selectedBoundedDevice.equals(address) || selectedBoundedDevice.equals("")) {
                        // Bluetooth включен, проверяем устанавливалось ли ранее соединение
                        if (BTService.btClientSocket == null) {
                            // попытка установить соединение не производилась
                            // производим поиск нужного нам устройства, получаем его
                            BTService.btBluetoothDevice = findBTDevice(selectedBoundedDevice);
                            if (BTService.btBluetoothDevice != null) {
                                ParcelUuid[] uuids = BTService.btBluetoothDevice.getUuids();// получаем перечень доступных UUID данного устройства
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
                                        BTService.btClientSocket = BTService.btBluetoothDevice.createRfcommSocketToServiceRecord(serialUUID);
                                    } else {
                                        // переходим в исходное состояние
                                        returnIdleStateActivity(true);
                                        // если получено NO_BOUNDED_DEVICE, выдать предупреждение
                                        Toast.makeText(getApplicationContext(), R.string.noValidBoundedDevice, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        // запускаем задачу bluetoothConnect если она не запущена
                        if (BTService.bluetoothConnect.getStatus().equals(AsyncTask.Status.PENDING))
                            BTService.bluetoothConnect.execute(0);
                        setConnect = true;
                        break;
                    }
                }
                if (!setConnect) {
                    // переходим в исходное состояние поскольку запуск соединения не произведен
                    returnIdleStateActivity(true, true);
                    // выдаем сообщение "Устройство для связи не выбрано."
                    Toast.makeText(getApplicationContext(), R.string.noBoundedDevice, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                // Bluetooth выключен. Предложим пользователю включить его.
                startActivityForResult(new Intent(actionRequestEnable), Utils.REQUEST_ENABLE_BT);
            }
        } catch (IOException e) {
            if (BTService.btMainStatus == BTService.MainStatus.CLOSE) {
                // завершение работы программы
                // выводим сообщение "Соединение разорвано"
                Toast.makeText(getApplicationContext(),
                        R.string.connectionInterrupted, Toast.LENGTH_SHORT).show();
            }
        }
    }
     */

// регистрация активностей
        /*
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(connectionStatus, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
        */

// приемник широковещательных событий
// region BroadcastReceiver
    /*
    BroadcastReceiver connectionStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    // изменяем состояние BT - CONNECTED
                    connectionStatusBT = ConnectionStatusBT.CONNECTED;
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    if (btMainStatus != MainStatus.CLOSE  && btMainStatus != MainStatus.IDLE){
                        // если программа не в закрытии и не в исходном состоянии
                        // состояние - CONNECTING
                        connectionStatusBT = ConnectionStatusBT.CONNECTING;
                        // запускаем процесс установления соединения
                        btMainStatus = MainStatus.CONNECTING;
                        returnIdleState(false);
                    }
                    else {
                        connectionStatusBT = ConnectionStatusBT.NO_CONNECT;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    if (btMainStatus != MainStatus.CLOSE) {
                        // переходим в исходное состояние
                        returnIdleState(true);
                    }
                    break;
            }
        }
    };
    */
// endregion

/**
 * метод вызываемый в конце выполнения задачи bluetoothConnect
 */
    /*
    void onPostExecuteBTConnect(String result){
        // если соединение активно
        if (connectionStatusBT == BTService.ConnectionStatusBT.CONNECTED){
            // проверяем результат работы задачи bluetoothConnect
            if (result.equals(BTConnect.CONNECTION_OK)) {
                // обнуляем счетчик попыток установления связи
                mConnectionAttemptsCnt = 0;
            }
        }
    }
     */
    /**
     *  поиск устройства Bluetooth по имени
     *  params: String addressDev - адрес, который будет искаться в спаренных усройствах
     */
    /*
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


    // расписание опроса команд от BT
    void schedule() {
        if (tTaskBT != null) tTaskBT.cancel();
            tTaskBT = new TimerTask() {
                public void run() {
                    // если есть подключение к сервису BT
                    if (boundBT)
                        // выполняем команды пришедшие по BT
                        getCommandBT();
                }
            };
            timerBT.schedule(tTaskBT, 250, 250);
    }


     */

