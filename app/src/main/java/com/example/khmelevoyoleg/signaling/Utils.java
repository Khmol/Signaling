package com.example.khmelevoyoleg.signaling;


import android.util.Log;

import java.util.ArrayList;

class Utils {

    // определяем строковые константы
    static final String SETTINGS_FILENAME = "Signaling";   // имя файла для хранения настроек
    static final String ADC_IN_OFF = "ADC IN OFF,";   // команда выключиь аналоговый вход
    static final String ADC_IN_ON = "ADC IN ON,";   // команда включиь аналоговый вход
    static final String RX_ERROR = "RX_ERROR" ;
    static final String SELECTED_BOUNDED_DEV = "SELECTED_BOUNDED_DEV";   // выбранное спаренное устройство
    static final String PENDING = "PENDING";        // задача ожидает запуска
    static final String FINISHED = "FINISHED";      // задача завершена
    static final String AUTO_CONNECT = "AUTO_CONNECT";   // вкл/выкл автоматическое соединение
    static final String IN_NAME = "IN_NAME_"; // название ключа для имени входа в настройках
    static final String ANALOG_IN_NAME = "ANALOG_IN_NAME"; // название ключа для имени входа в настройках
    static final String OUT_NAME = "OUT_NAME_"; // название ключа для имени выхода в настройках
    static final String IN_STATE = "IN_STATE_"; // название ключа для состояния входа в настройках
    static final String ANALOG_IN_STATE = "ANALOG_IN_STATE_"; // название ключа для состояния входа в настройках
    static final String OUT_STATE = "OUT_STATE_"; // название ключа для состояния входа в настройках
    static final String DEFAULT_IN_NAME = "Вход "; // имя входа по умолчанию
    static final String DEFAULT_OUT_NAME = "Выход "; // имя выхода по умолчанию
    static final String DEFAULT_IN_OUT_STATUS = "STATUS_INPUT_OFF"; // статус входа по умолчанию - выкл
    static final String DEFAULT_IN_OUT_STATE = "STATE_ON"; // состояние входа по умолчанию - выкл
    static final String STATE_ON = "STATE_ON"; // состояние входа - включен
    static final String STATE_OFF = "STATE_OFF"; // состояние входа - выключен
    static final String STATE_OFF_TIME = "STATE_OFF_TIME"; // состояние входа - выключен по времени
    // команды по BT
    static final String BT_INIT_MESSAGE = "SIMCOMSPPFORAPP\r"; //SIMCOMSPPFORAPP посылка для инициализации SIM
    static final String SET_ALARM = "SET ALARM,1\r"; // посылка для установки на охрану
    static final String CLEAR_ALARM = "CLEAR ALARM,1\r"; // посылка для снятия с охраны
    static final String CLEAR_ALARM_TRIGGERED = "CLEAR ALARM TRIGGERED,1\r"; // посылка для снятия аварии с SIM
    static final String OUT_1_ON = "OUT ON,1\r"; // посылка для открытия 1-го выхода
    static final String RX_INIT_OK = "SPP APP OK\r"; // ответ на BT_INIT_MESSAGE
    static final String TYPE_INPUT = "INPUT,"; // тип команды в ответе от SIM
    static final String TYPE_INPUT_A = "INPUT A,"; // тип команды в ответе от SIM
    static final String TYPE_INPUT_ON_OFF = "INPUT ON OFF,"; // тип команды в ответе на IN GET ON от SIM
    static final String TYPE_ADC = "ADC,"; // тип команды ADC в ответе от SIM
    static final String TYPE_ADC_A = "ADC A,"; // тип команды "ADC A" в ответе от SIM
    static final String TYPE_ADC_VAL = "ADC VAL int,"; // тип команды "ADC A" в ответе от SIM
    static final String TYPE_ADC_ON_OFF = "ADC ON OFF,"; // тип команды "ADC ON OFF" в ответе от SIM
    static final String ADC_IN_GET_ON = "ADC IN GET ON,00\r"; // команда Запросить статус включенных входов
    static final String OUT_OFF = "OUT OFF,"; // команда выключить реле по времени
    static final String OUT_ON_TIME = "OUT ON TIME,"; // команда включить реле по времени
    static final String OUT_ON = "OUT ON,"; // команда включить реле
    static final String IN_GET_ON = "IN GET ON,00\r"; // команда Запросить статус включенных входов
    static final String IN_ON = "IN ON,"; // команда включить вход для обработки
    static final String IN_OFF = "IN OFF,"; // команда выключить вход для обработки
    static final String IN_OFF_TIME = "IN OFF TIME,"; // команда выключить вход для обработки на время
    static final String STATUS_GENERAL_ALARM = "STATUS_GENERAL_ALARM"; // состояние модуля - АВАРИЯ
    static final String STATUS_ALARM_TRIGGERED = "STATUS_ALARM_TRIGGERED"; // состояние модуля - предварительная АВАРИЯ
    static final String STATUS_GUARD_ON = "STATUS_GUARD_ON"; // состояние модуля - на охране
    static final String STATUS_GUARD_OFF = "STATUS_GUARD_OFF"; // состояние модуля - не на охране
    static final String STATUS_CLEAR_ALARM = "STATUS_CLEAR_ALARM"; // отключение оповещения об аварии
    static final String STATUS_INPUT_ON = "STATUS_INPUT_ON"; // сработал вход
    static final String STATUS_INPUT_OFF = "STATUS_INPUT_OFF"; // вход не сработал
    static final String STATUS_INPUT_FAULT = "STATUS_INPUT_FAULT"; // вход не обрабатывается
    static final String STATUS_INPUT_ON_LARGER = "STATUS_INPUT_ON_LARGER"; // вход сработал по превышению
    static final String STATUS_INPUT_ON_LESS = "STATUS_INPUT_ON_LESS"; // вход сработал по уменьшению
    static final String STATUS_INPUT_ON_SHOCK = "STATUS_INPUT_ON_SHOCK"; // вход сработал в диапазоне
    static final String STATUS_INPUT_FAULT_LARGER = "STATUS_INPUT_FAULT_LARGER"; // вход не обрабатывается по превышению
    static final String STATUS_INPUT_FAULT_LESS = "STATUS_INPUT_FAULT_LESS"; // вход не обрабатывается по уменьшению

    // имена атрибутов для Map
    final static String ATRIBUTE_NUMBER = "number";
    final static String ATTRIBUTE_NAME = "name";
    final static String ATTRIBUTE_STATUS_IMAGE = "image";
    final static String ATTRIBUTE_TIME = "time";
    final static String ATTRIBUTE_STATE = "swith";
    // определяем числовые константы
    static final int FIRST_START = -1; // состояние модуля - не на охране
    static final int MAX_CONNECTION_ATTEMPTS = 3;   // максимальное количество попыток установления соединения
    static final int REQUEST_ENABLE_BT = 1;     // запрос включения Bluetooth
    static final int SET_SETTINGS = 2;          // редактирование настроек
    static final long UUID_SERIAL = 0x1101;     // нужный UUID
    static final long UUID_MASK = 0xFFFFFFFF00000000L;  // маска для извлечения нужных битов UUID
    static final short MASK_GUARD = 0;  // 0-й бит в маске - для извлечения флага нахождения на охране
    static final short MASK_ALARM = 9;  // 9-й бит в маске - для извлечения флага сработала авария
    static final short MASK_ALARM_CUR = 13;  // 13-й бит в маске - текущее значение флага сработала авария
    static final short MASK_ALARM_TRIGERED = 8;  // 8-й бит в маске - для извлечения флага сработала предварительная авария
    static final short MASK_ALARM_TRIGERED_CUR = 12;  // 12-й бит в маске - текущее значение флага предварительной аварии
    static final int DELAY_TX_INIT_MESSAGE = 3; // задержка перед повторной передачей  INIT_MESSAGE
    static final int TIMER_CHECK_STATUS = 400;  // периодичность вызова runCheckStatus
    static final int DELAY_CONNECTING = 12;     // задержка перед повторной попыткой соединения по BT
    static final int MAX_PROGRESS_VALUE = 3;    // количество ступеней в ProgressBar
    static final int AUTO_CONNECT_TIMEOUT = 300;  // время между запуском поиска SIM 2 мин = TIMER_CHECK_STATUS * AUTO_CONNECT_TIMEOUT
    static final long VIBRATE_TIME = 200;      //  длительность вибрации при нажатии кнопки
    static final short DEFAULT_DIG_IN_NUMBER = 20; // количество входов по умолчанию
    static final short DEFAULT_ANALOG_IN_NUMBER = 3; // количество входов по умолчанию
    static final short DEFAULT_OUT_NUMBER = 15; // количество выходов по умолчанию
    static final short SB_DIG_IN_ON = 1; // SeekBar для цифровых входов в среднем положении
    static final short SB_DIG_IN_OFF = 0; // SeekBar для цифровых входов в левом положении
    static final short SB_DIG_IN_TIME_OFF = 2; // SeekBar для цифровых входов в правом положении
    static final int DRFAULT_DIG_IN_TIME_OFF = 0; // время выключения дискретного входа по умолчанию

    static final short CMD_INPUT_STATUS_FROM = 7; // начало флагов статуса охраны в команде INPUT
    static final short CMD_INPUT_LATCH_FROM = 12; // начало флагов защелки статуса входов в команде INPUT
    static final short CMD_INPUT_CUR_LATCH_FROM = 37; // начало флагов защелки статуса входов в команде INPUT
    static final short CMD_INPUT_RSSI_FROM = 62; // начало значения RSSI в команде INPUT

    static final short CMD_ADC_STATUS_FROM = 5; // начало флагов статуса охраны в команде ADC
    static final short CMD_ADC_LARGER_LATCH_FROM = 10; // начало флагов защелки статуса входов по превышшению в команде ADC
    static final short CMD_ADC_LESS_LATCH_FROM = 19; // начало флагов защелки статуса входов на уменьшение в команде ADC
    static final short CMD_ADC_SHOCK_LATCH_FROM = 28; // начало флагов защелки статуса входов в диапазоне в команде ADC
    static final short CMD_ADC_LARGER_CUR_FROM = 37; // начало текущих флагов статуса входов по превышшению в команде ADC
    static final short CMD_ADC_LESS_CUR_FROM = 46; // начало текущих флагов статуса входов по уменьшению в команде ADC
    static final short CMD_ADC_SHOCK_CUR_FROM = 55; // начало текущих флагов статуса входов в диапазоне в команде ADC
    static final short CMD_ADC_RSSI_FROM = 64; // начало значения RSSI в команде ADC
    static final short CMD_ADC_ON_OFF_STATUS_FROM = 12; // начало флагов сатуса входов в команде ADC_ON_OFF


    static final short CMD_INPUT_A_CUR_ON_FROM = 9; // начало флагов включенных датчиков в команде INPUT_A
    static final short CMD_INPUT_A_STATUS_FROM = 34; // начало флагов сатуса входов в команде INPUT_А
    static final short CMD_ADC_A_STATUS_FROM = 25; // начало флагов стауса входов в команде ADC_A
    static final short CMD_ADC_A_LARGER_FROM = 7; // начало флагов LARGER в команде ADC_A
    static final short CMD_ADC_A_LESS_FROM = 16; // начало флагов LESS в команде ADC_A


    static final short CMD_INPUT_ON_OFF_STATUS_FROM = 14; // начало флагов сатуса входов в команде INPUT_ON_OFF
    static final short LENGTH_INPUT_GROUP = 4; // длина данных для группы входов (по 16 входов)
    static final short NUMBER_DIGITAL_INPUTS = 96; // количество цифровых входов в посылке по BT
    static final short NUMBER_ANALOG_INPUTS = 32; // количество аналоговых входов в посылке по BT
    static final short ALL_OUT = 0;         // количество для выключения всех выходов

    /**
     * установка в digInStatus актуальных значений
     * @param digitalInputCurrent - массив текущих значений состояний дискретных входов
     * @param oldDigitalInputCurrent - массив прошлых значений состояний дискретных входов
     * @param digitalInputACurOn - массив значений состояний дискретных входов при установке на охрану
     * @param digInStatus - список для изменения
     */
    static void modifyDigInStatus(boolean[] digitalInputCurrent, boolean[] oldDigitalInputCurrent,
                                   boolean[] digitalInputACurOn, ArrayList<String> digInStatus) {
        int len = digInStatus.size();
        for (int i = 0; i < len; i++) {
            if (digitalInputCurrent[i] != oldDigitalInputCurrent[i]) {
                if (digitalInputCurrent[i]) {
                    digInStatus.set(i, STATUS_INPUT_ON);
                } else {
                    digInStatus.set(i, STATUS_INPUT_OFF);
                }
                oldDigitalInputCurrent[i] = digitalInputCurrent[i];
                continue;
            }
            // если вход не обрабатывается, нужно изменить его состояние
            if (!digitalInputACurOn[i]){
                //вход не обрабатывается
                digInStatus.set(i, STATUS_INPUT_FAULT);
            }
        }
    }

    /**
     * установка всех элементов массива boolean[] в true
     * @param  - массив текущих значений состояний дискретных входов
     * @param  - массив прошлых значений состояний дискретных входов
     * @param  - массив значений состояний дискретных входов при установке на охрану
     * @param analogInStatus - список для изменения
     */
    static void modifyAnalogInStatus(boolean[] analogLargerCur, boolean[] oldAnalogLargerCur,
                                     boolean[] analogLessCur, boolean[] oldAnalogLessCur,
                                     boolean[] analogShockCur, boolean[] oldAnalogShockCur,
                                     boolean[] analogACurLarger, boolean[] analogACurLess,
                                     ArrayList<String> analogInStatus) {
        int len = analogInStatus.size();
        for (int i = 0; i < len; i++) {
            if (analogLargerCur[i] != oldAnalogLargerCur[i]) {
                oldAnalogLargerCur[i] = analogLargerCur[i];
                if (analogLargerCur[i]) {
                    analogInStatus.set(i, STATUS_INPUT_ON_LARGER);
                    continue;
                } else {
                    if (analogInStatus.get(i).equals(STATUS_INPUT_ON_LARGER))
                        analogInStatus.set(i, STATUS_INPUT_OFF);
                }
            }
            if (analogLessCur[i] != oldAnalogLessCur[i]) {
                oldAnalogLessCur[i] = analogLessCur[i];
                if (analogLessCur[i]) {
                    analogInStatus.set(i, STATUS_INPUT_ON_LESS);
                    continue;
                } else {
                    if (analogInStatus.get(i).equals(STATUS_INPUT_ON_LESS))
                        analogInStatus.set(i, STATUS_INPUT_OFF);
                }
            }
            if (analogShockCur[i] != oldAnalogShockCur[i]) {
                oldAnalogShockCur[i] = analogShockCur[i];
                if (analogShockCur[i]) {
                    analogInStatus.set(i, STATUS_INPUT_ON_SHOCK);
                    continue;
                } else {
                    if (analogInStatus.get(i).equals(STATUS_INPUT_ON_SHOCK))
                        analogInStatus.set(i, STATUS_INPUT_OFF);
                }
            }
            // если входы не обрабатываются при постановке на охрану, нужно указать по какой причине
            if (!analogACurLarger[i]){
                //вход не обрабатывается по превышению
                analogInStatus.set(i, STATUS_INPUT_FAULT_LARGER);
            }
            if (!analogACurLess[i]){
                //вход не обрабатывается по уменьшению
                analogInStatus.set(i, STATUS_INPUT_FAULT_LESS);
            }
        }
    }

    /**
     * установка всех элементов массива boolean[] в true
     * @param length - длина массива
     * @param list - массив для обработки
     */
    static void setTrueArray(int length, boolean[] list) {
        for (int i = 0; i < length; i++)
            list[i] = true;
    }

    /**
     * установка всех элементов массива boolean[] в false
     * @param length - длина массива
     * @param list - массив для обработки
     */
    static void setFalseArray(int length, boolean[] list) {
        for (int i = 0; i < length; i++)
            list[i] = false;
    }

    /**
     * занесение информации для вывода в lvMainInStatus
     * @param value - значение для проверки бита
     * @param masks - номера битов для проверки
     */
    static int getValueOnMask(int value, int... masks) {
        int result = 0;
        for ( int mask : masks ) {
            result = result + ((1 << mask) & value);
        }
        return result;
    }

    /**
     * получение нужной картинки для вывода в ivAnalogInStatus
     * @param position - позиция элемента в списке
     * @return - номер ресурса
     */
    static int getImageViewValue(ArrayList<String> inputStatus, int position) {
        if (inputStatus.get(position).equals(Utils.STATUS_INPUT_OFF)) {
            return R.drawable.circle_grey48;
        } else if (inputStatus.get(position).equals(Utils.STATUS_INPUT_ON)) {
            return R.drawable.circle_green48;
        } else if (inputStatus.get(position).equals(Utils.STATUS_INPUT_FAULT)) {
            return R.drawable.circle_red48;
        } else if (inputStatus.get(position).equals(Utils.STATUS_INPUT_ON_LARGER)) {
            return R.drawable.circle_green48_high;
        } else if (inputStatus.get(position).equals(Utils.STATUS_INPUT_ON_SHOCK)) {
            return R.drawable.circle_green48_delta;
        } else if (inputStatus.get(position).equals(Utils.STATUS_INPUT_ON_LESS)) {
            return R.drawable.circle_green48_low;
        } else if (inputStatus.get(position).equals(Utils.STATUS_INPUT_FAULT_LARGER)) {
            return R.drawable.circle_red48_high;
        } else if (inputStatus.get(position).equals(Utils.STATUS_INPUT_FAULT_LESS)) {
            return R.drawable.circle_red48_low;
        }
        return 0;
    }

}
