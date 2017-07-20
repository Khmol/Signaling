package com.example.khmelevoyoleg.signaling;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // пределяем константы
    private static final String BT_SERVER_NAME = "CAR2";
    private static final String BT_SERVER_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    // TODO перенести BT_SERVER_NAME в настройки
    // TODO перенести BT_SERVER_UUID в настройки
    private static final int REQUEST_ENABLE_BT = 1;     // запрос включения Bluetooth
    private static final int BT_CONNECT_OK = 5;         // соединение по Bluetooth установлено
    private static final int BT_CONNECT_ERR = 6;        // ошибка установки соединения по Bluetooth

    // определяем стринговые константы
    protected String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket clientSocket;
    private OutputStream outStream;
    private InputStream inStream;
    private FloatingActionButton fabConnect;        // кнопка поиска "Базового блока"

    Handler btConnectHandler;   // обработчик сообщений из потока btConnect

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // определяем объекты всех View
        fabConnect = (FloatingActionButton) findViewById(R.id.fabConnect);


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
                        // TODO - обработать ситуацию когда соединение уже установлено
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
                        break;
                }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        // по умолчанию возвращаем обработчик родителя
        return super.onOptionsItemSelected(item);
    }

    // обработка нажатия кнопки fabConnect
    public void pbConnectHeader(View view){
        // выводим сообщение о начале поиска "Базового блока"
        Toast.makeText(getApplicationContext(), "Запущен поиск сигнализации", Toast.LENGTH_SHORT).show();
        // создаем поток в котором будет производится поиск "Базового блока"
        Thread bluetooth_Connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mBluetoothDevice = findBTDevice(BT_SERVER_NAME);
                    UUID uuid = new UUID(0, 0);
                    uuid = UUID.fromString(BT_SERVER_UUID);
                    if (clientSocket != null){
                        if ( ! clientSocket.isConnected()) {
                            clientSocket.connect();
                        }
                    }
                    else {
                        clientSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        clientSocket.connect();
                    }
                    btConnectHandler.sendMessage(btConnectHandler.obtainMessage(BT_CONNECT_OK, 0, 0, BT_SERVER_NAME));
                }
                catch (IOException e) {
                    btConnectHandler.sendEmptyMessage(BT_CONNECT_ERR);
                }
            }
        });
        bluetooth_Connect.start();
    }

    /* поиск устройства Bluetooth по имени
    params: String nameDev - мя которое будет искаться в спаренных усройствах */
    public BluetoothDevice findBTDevice (String nameDev) {
        // получаем список спаренных устройств
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
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
}

