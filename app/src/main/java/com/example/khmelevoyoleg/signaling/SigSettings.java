package com.example.khmelevoyoleg.signaling;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

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
        //
        ArrayList<String> name = getIntent().getStringArrayListExtra("paired_names");
        ArrayList<String> adress = getIntent().getStringArrayListExtra("paired_adresses");
        clSettings = (LinearLayout) findViewById(R.id.llSettings);
        rgPairedDevices = (RadioGroup) findViewById(R.id.rgPairedDevices);
        // формируем список радио кнопок
        for (String n : name)
        {
            RadioButton rbListPairedDevices = new RadioButton(this);
            rbListPairedDevices.setText(n);
            rbListPairedDevices.setTextSize(Integer.parseInt(getResources().getText(R.string.radioButtomSize).toString()));
            rbListPairedDevices.setId(findId());
            // добавляем радиокнопку в группу
            rgPairedDevices.addView(rbListPairedDevices);
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
        int i = rgPairedDevices.getCheckedRadioButtonId(); // получаем номер выбранной радиокнопки
        Intent intent = new Intent();// запускаем намерение
        //Toast.makeText(getApplicationContext(),
        //        Integer.toString(i),
        //        Toast.LENGTH_SHORT).show();
        intent.putExtra("adress", Integer.toString(i));
        setResult(RESULT_OK, intent);
        finish();
    }
}
