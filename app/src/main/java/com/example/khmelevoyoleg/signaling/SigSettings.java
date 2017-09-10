package com.example.khmelevoyoleg.signaling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Oleg_ on 21.07.17.
 */

public class SigSettings extends Activity {
    private static final String SETTINGS_FILENAME = "Signaling";   // имя файла для хранения настроек
    private static final String SELECTED_BOUNDED_DEV = "SELECTED_BOUNDED_DEV";   // выбранное спаренное устройство

    static int id = 1;  // начальное id для поиска свободного номера
    private RadioGroup rgPairedDevices; // группа радиокнопок спаренных устройств
    private HashMap<Integer, String> boundedDevices; // карты спаренных устройств - id/adress
    private SharedPreferences sPref;    // настройки приложения


    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // определяем объект для работы с настройками
        sPref = getSharedPreferences(SETTINGS_FILENAME, MODE_PRIVATE);
        RadioButton rbListPairedDevices;
        // определяем группу радиокнопок спаренных устройств
        rgPairedDevices = (RadioGroup) findViewById(R.id.rgPairedDevices);
        boundedDevices = new HashMap<>();
        // получаем списки имен и адресов спаренных устройств
        ArrayList<String> names = getIntent().getStringArrayListExtra("paired_names");
        ArrayList<String> addresses = getIntent().getStringArrayListExtra("paired_adresses");
        // формируем список радио кнопок
        int i = 0;
        for (String name : names)
        {
            // орпеделяем радиокнопку, добавляем ее в группу
            rbListPairedDevices = new RadioButton(this);
            rbListPairedDevices.setText(name);  // установка текста
            rbListPairedDevices.setTextSize(Integer.parseInt(getResources().getText(R.string.radioButtomSize).toString())); // размер текстаfindId()
            int boundId = findId(); // находим свободный id для радиокнопке
            rbListPairedDevices.setId(boundId);    // задаем ID радиокнопки
            String addr = addresses.get(i);  // получаем адрес устройства
            boundedDevices.put(boundId, addr);   // формируем карты адресов с привязкой к радиокнопкам
            // если номер сохраненного устройства совпадает с текущим
            if (addr.equals(loadBoundDevice())){
                // выбираем данную кнопку как активную
                rbListPairedDevices.toggle();
            }
            rgPairedDevices.addView(rbListPairedDevices);   // добавляем элемент в группу
            i++;
        }
    }

    /**
     * Поиск свободного ID
     */
    public int findId(){
        id = R.id.llSettings;
        View v = findViewById(id);
        while (v != null){
            v = findViewById(++id);
        }
        return id;
    }

    /**
     * Обработчик нажатия кнопки Save
     */
    public void pbSaveHeader(View view) {
        int buttonId = rgPairedDevices.getCheckedRadioButtonId(); // получаем номер выбранной радиокнопки
        Intent intent = new Intent();// запускаем намерение
        // Получаем набор элементов
        Set<Map.Entry<Integer, String>> set = boundedDevices.entrySet();
            // пребераем список и ищем выбранное устройство
            for (Map.Entry<Integer, String> device : set) {
            if (device.getKey() == buttonId){
                String addr = device.getValue();
                intent.putExtra("address", addr); // возвращаем адрес выранного устройства
                saveBoundDevice(addr);  // сохраняем адрес выбранного устройства
                break;
            }
        }
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
}