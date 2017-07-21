package com.example.khmelevoyoleg.signaling;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Oleg_ on 21.07.17.
 */

public class SigSettings extends Activity {

    private EditText etNameBaseStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String text = getIntent().getStringExtra("name");
        etNameBaseStation = (EditText) findViewById(R.id.etNameBaseStation);
        etNameBaseStation.setText(text);
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
