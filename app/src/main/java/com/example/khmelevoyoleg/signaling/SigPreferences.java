package com.example.khmelevoyoleg.signaling;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * Created by Oleg on 31.07.2019.
 */


public class SigPreferences extends PreferenceActivity {

    CharSequence[] addresses;        // список адресов спаренных устройств
    CharSequence[] names;            // список имен спаренныъ устройств
    CharSequence active_address;   //адрес выбранного устройства для связи с ним
    Bundle bundlePF;
    PreferenceFragment prefFragment;

    private static int prefs = R.xml.preferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // получаем списки имен и адресов спаренных устройств
        names = getIntent().getCharSequenceArrayExtra("paired_names");
        addresses = getIntent().getCharSequenceArrayExtra("paired_adresses");
        active_address = getIntent().getCharSequenceExtra("paired_device_adress");

        // передаем данные в Bundle
        bundlePF = new Bundle();
        bundlePF.putCharSequenceArray("names", names);
        bundlePF.putCharSequenceArray("addresses", addresses);
        bundlePF.putCharSequence("paired_device_adress", active_address);
        // запускаем настройки в фрагменте
        prefFragment = new PF();
        prefFragment.setArguments(bundlePF);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                prefFragment).commit();
    }


    @TargetApi(11)
    public static class PF extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener
    {
        ListPreference lpPairedDevices; // список для выбора устройства для подключения
        CharSequence[] _addresses;      // список адресов спаренных устройств
        CharSequence[] _names;          // список имен спаренныъ устройств
        CharSequence _active_address;   //адрес выбранного устройства для связи с ним

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(SigPreferences.prefs); //outer class
            // назначаем обработчик нажатия на ListPreference
            lpPairedDevices = (ListPreference) findPreference(Utils.SELECTED_BOUNDED_DEV);
            lpPairedDevices.setOnPreferenceChangeListener(this);
            // получаем данные из Bundle
            _names = getArguments().getCharSequenceArray("names");
            _addresses = getArguments().getCharSequenceArray("addresses");
            _active_address = getArguments().getCharSequence("paired_device_adress");
            // если параметр не получен, заменяем его на пустую строку
            if (_active_address == null)
                _active_address = "";
            int i = 0;
            if ( !_active_address.equals("")) {
                for(CharSequence address : _addresses) {
                    if (address.equals(_active_address)) {
                        lpPairedDevices.setSummary(_names[i]);
                        break;
                    }
                    i++;
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (lpPairedDevices != null) {
                lpPairedDevices.setEntries(_names);
                lpPairedDevices.setEntryValues(_addresses);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            CharSequence value = (CharSequence) newValue;
            short i = 0;
            for(CharSequence address : _addresses) {
                if (address.equals(value)) {
                    lpPairedDevices.setSummary(_names[i]);
                    return true;
                }
                i++;
            }
            return false;
        }
    }
}
