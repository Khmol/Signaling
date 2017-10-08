package com.example.khmelevoyoleg.signaling;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;

public class SigSettings extends Activity implements View.OnClickListener{
    private static final String SETTINGS_FILENAME = "Signaling";   // имя файла для хранения настроек
    private static final String SELECTED_BOUNDED_DEV = "SELECTED_BOUNDED_DEV";   // выбранное спаренное устройство

    private ListView lvPairedDevices;   // ListView для спаренных устройств
    private SharedPreferences sPref;    // настройки приложения
    ArrayList<String> addresses;        // список адресов спаренных устройств

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button pbSave = (Button) findViewById(R.id.pbSave);
        pbSave.setOnClickListener(this);

        // определяем объект для работы с настройками
        sPref = getSharedPreferences(SETTINGS_FILENAME, MODE_PRIVATE);
        // получаем списки имен и адресов спаренных устройств
        ArrayList<String> names = getIntent().getStringArrayListExtra("paired_names");
        addresses = getIntent().getStringArrayListExtra("paired_adresses");
        // определяем ListView спаренных устройств
        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
        lvPairedDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // настраиваем ArrayAdapter спаренных устройств
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.settings_item,
                names); //simple_list_item_single_choice
        lvPairedDevices.setAdapter(adapter);
        // выделяем сохраненный номер спаренного устройства
        for (int i = 0; i < addresses.size(); i++)
        {
            // читаем сохраненный номер спаренного устройства
            String boundDev = loadBoundDevice();
            if (addresses.get(i).equals(boundDev)){
                // номер совпадает - выделяем данную позицию в списке
                lvPairedDevices.setItemChecked(i, true);
            }
        }
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
        saveBoundDevice(addr);  // сохраняем адрес выбранного устройства
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Сохранение адреса связанного устройства
     */
    void saveBoundDevice(String address) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SELECTED_BOUNDED_DEV, address);
        ed.apply();
    }

    /**
     * Чтение адреса связанного устройства
     */
    String loadBoundDevice() {
        return sPref.getString(SELECTED_BOUNDED_DEV, "");
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
        }
    }
}