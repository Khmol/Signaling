package com.example.khmelevoyoleg.signaling;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by Oleg_ on 21.07.17.
 */

public class SigSettings extends Activity {

    static int id = 1;  // начальное id для поиска свободного номера

    private EditText etNameBaseStation;
    private LinearLayout clSettings;
    private RadioGroup rgPairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String text = getIntent().getStringExtra("name");
        clSettings = (LinearLayout) findViewById(R.id.clSettings);
        // etNameBaseStation = (EditText) findViewById(R.id.etNameBaseStation);
        // etNameBaseStation.setText(text);
        rgPairedDevices = (RadioGroup) findViewById(R.id.rgPairedDevices);
        RadioButton rbListPairedDevices = new RadioButton(this);
        rbListPairedDevices.setText("Проверка");
        rbListPairedDevices.setId(findId());
        // добавляем радиокнопку в группу
        rgPairedDevices.addView(rbListPairedDevices);
        // добавляем группу радиокнопок на Layout
        //clSettings.addView(rgPairedDevices);
    }

    /**
     * Поиск свободного ID
     */
    public int findId(){
        id = R.id.clSettings;
        View v = findViewById(id);
        while (v != null){
            v = findViewById(++id);
        }
        return id;
    }

    /**
     * Закрываем настройки
     */
    public void pbSaveHeader(View view) {
        Intent intent = new Intent();
        intent.putExtra("name", etNameBaseStation.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
