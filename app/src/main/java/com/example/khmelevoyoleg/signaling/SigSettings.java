package com.example.khmelevoyoleg.signaling;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import java.util.ArrayList;

public class SigSettings extends Activity implements View.OnClickListener{
    private static final String SETTINGS_FILENAME = "Signaling";   // имя файла для хранения настроек
    private static final String SELECTED_BOUNDED_DEV = "SELECTED_BOUNDED_DEV";   // выбранное спаренное устройство
    private static final String AUTO_CONNECT = "AUTO_CONNECT";   // вкл/выкл автоматическое соединение

    private ListView lvPairedDevices;   // ListView для спаренных устройств
    private SharedPreferences sPref;    // настройки приложения
    ArrayList<String> addresses;        // список адресов спаренных устройств
    ArrayList<String> names;            // список имен спаренныъ устройств
    ArrayAdapter<String> adapter;
    CheckBox cbAutoConnect;             // автоподлючение

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button pbSave = (Button) findViewById(R.id.pbSave);
        Button pbAdd = (Button) findViewById(R.id.pbAdd);
        pbSave.setOnClickListener(this);
        pbAdd.setOnClickListener(this);
        cbAutoConnect = (CheckBox) findViewById(R.id.cbAutoConnect);

        // определяем объект для работы с настройками
        sPref = getSharedPreferences(SETTINGS_FILENAME, MODE_PRIVATE);
        // получаем списки имен и адресов спаренных устройств
        names = getIntent().getStringArrayListExtra("paired_names");
        addresses = getIntent().getStringArrayListExtra("paired_adresses");
        // определяем ListView спаренных устройств
        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
        lvPairedDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // настраиваем ArrayAdapter спаренных устройств
        adapter = new ArrayAdapter<>(this,
                R.layout.settings_item,
                names); //simple_list_item_single_choice
        lvPairedDevices.setAdapter(adapter);
        // отмечаем сохраненный номер спаренного устройства
        for (int i = 0; i < addresses.size(); i++)
        {
            // читаем сохраненный номер спаренного устройства
            String boundDev = loadBoundedDevice();
            if (addresses.get(i).equals(boundDev)){
                // номер совпадает - выделяем данную позицию в списке
                lvPairedDevices.setItemChecked(i, true);
            }
        }
        // отмечаем в cbAutoConnect его сохраненное значение
        cbAutoConnect.setChecked(loadAutoConnect());
    }

    /**
     * Обработчик нажатия кнопки Save
     */
    public void pbSaveHeader() {
        // получаем номер выделенного пункта списка
        int position = lvPairedDevices.getCheckedItemPosition();
        // получаем адрес данного устройтва
        String addr = addresses.get(position);
        // запускаем намерение
        Intent intent = new Intent();
        intent.putExtra("address", addr); // возвращаем адрес выбранного устройства
        saveSettings(addr, cbAutoConnect.isChecked());  // сохраняем настройки
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Обработчик нажатия кнопки Add
     */
    public void pbAddHeader() {
        names.add("Hello " + names.size());
        adapter.notifyDataSetChanged();
        lvPairedDevices.setSelection(names.size());
    }


    /**
     * Сохранение адреса связанного устройства
     */
    void saveSettings(String address, boolean autoConnect) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SELECTED_BOUNDED_DEV, address);
        ed.putString(AUTO_CONNECT, Boolean.toString(autoConnect));
        ed.apply();
    }

    /**
     * Чтение адреса связанного устройства
     */
    String loadBoundedDevice() {
        return sPref.getString(SELECTED_BOUNDED_DEV, "");
    }

    /**
     * Чтение флага автоматического подключения
     */
    boolean loadAutoConnect() {
        return Boolean.parseBoolean(sPref.getString(AUTO_CONNECT, ""));
    }

    // Вызывается перед уничтожением активности
    @Override
    public void onDestroy() {
        // Освободить все ресурсы, включая работающие потоки,
        // соединения с БД и т. д.
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pbSave:
                pbSaveHeader();
            case R.id.pbAdd:
                pbAddHeader();
        }
    }
}