package com.example.khmelevoyoleg.signaling;


import android.util.Log;

import java.util.ArrayList;

class Utils {

    static final int INDEX_STATUS_SIM = 1;
    static final int INDEX_LATCH_INPUT = 2;
    static final int INDEX_LATCH_LARGER_ADC = 2;
    static final int INDEX_LATCH_LESS_ADC = 3;
    static final int INDEX_LATCH_SHOCK_ADC = 4;
    static final int INDEX_CUR_LARGER_ADC = 2;
    static final int INDEX_CUR_LESS_ADC = 3;
    static final int INDEX_CUR_SHOCK_ADC = 4;
    static final int ADC_GROUP_LENGTH = 8;
    static final int INDEX_CURRENT_INPUT = 3;

    // определяем строковые константы
    static final String SETTINGS_FILENAME = "Signaling";   // имя файла для хранения настроек
    static final String RX_ERROR = "RX_ERROR" ;
    static final String SELECTED_BOUNDED_DEV = "listPrefPairedDevices";   // выбранное спаренное устройство
    static final String FINISHED = "FINISHED";      // задача завершена
    static final String AUTO_CONNECT = "cbAutoConnect";   // вкл/выкл автоматическое соединение

    static final String IN_NAME = "IN_NAME_"; // название ключа для имени входа в настройках
    static final String ANALOG_IN_NAME = "ANALOG_IN_NAME"; // название ключа для имени входа в настройках
    static final String OUT_NAME = "OUT_NAME_"; // название ключа для имени выхода в настройках
    static final String IN_STATE = "IN_STATE_"; // название ключа для состояния входа в настройках
    static final String ANALOG_IN_STATE = "ANALOG_IN_STATE_"; // название ключа для состояния входа в настройках
    static final String OUT_STATE = "OUT_STATE_"; // название ключа для состояния входа в настройках

    static final String DEFAULT_IN_NAME = "Вход замыкания "; // имя входа по умолчанию
    static final String DEFAULT_AN_IN_NAME = "Вход сигнала "; // имя входа по умолчанию
    static final String DEFAULT_OUT_NAME = "Выход "; // имя выхода по умолчанию
    static final String DEFAULT_IN_OUT_STATUS = "STATUS_INPUT_OFF"; // статус входа по умолчанию - выкл
    static final String DEFAULT_IN_OUT_STATE = "STATE_ON"; // состояние входа по умолчанию - выкл
    static final String DEFAULT_OUT_STATE = "STATE_OFF"; // состояние выхода по умолчанию - выкл
    static final String STATE_ON = "STATE_ON"; // состояние входа - включен
    static final String STATE_OFF = "STATE_OFF"; // состояние входа - выключен
    // команды по BT
    static final String BT_INIT_MESSAGE = "SIMCOMSPPFORAPP\r"; //SIMCOMSPPFORAPP посылка для инициализации SIM
    static final String SET_ALARM = "SET ALARM,1\r"; // посылка для установки на охрану
    static final String SET_ALARM_SILENT = "SET ALARM,2\r"; // посылка для установки на охрану в беззвучном режиме
    static final String CLEAR_ALARM = "CLEAR ALARM,1\r"; // посылка для снятия с охраны
    static final String CLEAR_ALARM_SILENT = "CLEAR ALARM,2\r"; // посылка для снятия с охраны в беззвучном режиме
    static final String CLEAR_ALARM_TRIGGERED = "CLEAR ALARM TRIGGERED,1\r"; // посылка для снятия аварии с SIM
    static final String OUT_1_ON_TIME = "OUT ON TIME,1\r"; // посылка для открытия 1-го выхода по времени
    static final String RX_INIT_OK = "SPP APP OK\r"; // ответ на BT_INIT_MESSAGE

    static final String TYPE_INPUT = "INPUT,"; // тип команды в ответе от SIM
    static final String TYPE_INPUT_A = "INPUT A,"; // тип команды в ответе от SIM
    static final String TYPE_INPUT_ON_OFF = "INPUT ON OFF,"; // тип команды в ответе на IN GET ON от SIM
    static final String TYPE_INPUT_TIME_OFF = "INPUT TIME OFF,"; // тип команды "INPUT TIME OFF" в ответе от SIM
    static final String TYPE_INPUT_DELAY_START = "INPUT DELAY START,"; // тип команды "INPUT DELAY START" в ответе от SIM

    static final String TYPE_OUT_ON_OFF = "OUTPUT ON OFF,"; // тип команды в ответе на OUT ON OFF от SIM

    static final String TYPE_ADC = "ADC,"; // тип команды ADC в ответе от SIM
    static final String TYPE_ADC_A = "ADC A,"; // тип команды "ADC A" в ответе от SIM
    static final String TYPE_ADC_VAL = "ADC VAL int,"; // тип команды "ADC VAL int" в ответе от SIM
    static final String TYPE_ADC_ON_OFF = "ADC ON OFF,"; // тип команды "ADC ON OFF" в ответе от SIM
    static final String TYPE_ADC_TIME_OFF = "ADC TIME OFF,"; // тип команды "ADC TIME OFF" в ответе от SIM
    static final String TYPE_ADC_DELAY_START = "ADC DELAY START,"; // тип команды "ADC DELAY START" в ответе от SIM

    static final String ADC_IN_OFF = "ADC IN OFF,";   // команда выключиь аналоговый вход
    static final String ADC_IN_ON = "ADC IN ON,";   // команда включиь аналоговый вход
    static final String ADC_IN_GET_ON = "ADC IN GET ON,01\r"; // команда Запросить статус включенных входов
    static final String ADC_IN_OFF_TIME = "ADC IN OFF TIME,"; // команда выключить вход на время
    static final String ADC_IN_GET_TIME_OFF = "ADC IN GET TIME OFF,01\r"; // команда запросить время выключения входа
    static final String ADC_IN_GET_DELAY_START = "ADC IN GET DELAY START,01\r"; // запросить время задержки опроса входа при постановке на охрану

    static final String OUT_OFF = "OUT OFF,"; // команда выключить реле по времени
    static final String OUT_ON_TIME = "OUT ON TIME,"; // команда включить реле по времени
    static final String OUT_ON = "OUT ON,"; // команда включить реле
    static final String OUT_GET_ON = "OUT GET ON,01\r"; // команда включить запрос статуса включенных выходов
    static final String OUT_GET_OFF = "OUT GET ON,00\r"; // команда выкобчить запрос статуса включенных выходов

    static final String IN_GET_ON = "IN GET ON,01\r"; // команда Запросить статус включенных входов
    static final String IN_ON = "IN ON,"; // команда включить вход для обработки
    static final String IN_OFF = "IN OFF,"; // команда выключить вход для обработки
    static final String IN_OFF_TIME = "IN OFF TIME,"; // команда выключить вход для обработки на время
    static final String IN_GET_TIME_OFF = "IN GET TIME OFF,01\r"; // запросить время выключения входов
    static final String IN_GET_DELAY_START = "IN GET DELAY START,01\r"; // запросить время задержки опроса входа при постановке на охрану
    static final String IN_ALL_STATE = "IN ALL STATE,01\r"; // запросить передачу пакета INPUT и ADC

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

    static final String MAIN_IN_STATUS = "MAIN_IN_STATUS_";//ключ для записи состояния главного ArrayList

    // имена атрибутов для Map
    final static String ATRIBUTE_NUMBER = "number";
    final static String ATTRIBUTE_NAME = "name";
    final static String ATTRIBUTE_STATUS_IMAGE = "image";
    final static String ATTRIBUTE_TIME = "time";
    final static String ATTRIBUTE_STATE = "swith";
    // определяем числовые константы
    static final int FIRST_START = -1; // состояние модуля - пкерый запуск
    static final int IDLE = 0; // состояние модуля - не на охране
    static final int MAX_CONNECTION_ATTEMPTS = 3;   // максимальное количество попыток установления соединения
    static final int REQUEST_ENABLE_BT = 1;     // запрос включения Bluetooth
    static final int SET_SETTINGS = 2;          // редактирование настроек
    static final int SET_PREFERENCES = 3;       // редактирование настроек
    static final long UUID_SERIAL = 0x1101;     // нужный UUID
    static final long UUID_MASK = 0xFFFFFFFF00000000L;  // маска для извлечения нужных битов UUID
    static final short MASK_GUARD = 0;  // 0-й бит в маске - для извлечения флага нахождения на охране
    static final short MASK_ALARM = 9;  // 9-й бит в маске - для извлечения флага сработала авария
    static final short MASK_ALARM_TRIGERED = 8;  // 8-й бит в маске - для извлечения флага сработала предварительная авария
    static final short MASK_ALARM_TRIGERED_START = 10;  // начало счетчика предварительной аварии
    static final short MASK_ALARM_START = 13;  // начало счетчика аварии

    static final int TIMER_CHECK_STATUS = 400;  // периодичность вызова runCheckStatus
    static final int TIMER_INIT_MESSAGE = 10000;  // периодичность вызова runCheckStatus
    static final int TIMER_LISTEN_BT = 500;  // периодичность вызова runListenMessageBT
    static final int TIMER_TOAST = 3000;  // периодичность вызова runToast
    static final int DELAY_CONNECTING = 12;     // задержка перед повторной попыткой соединения по BT
    static final int MAX_PROGRESS_VALUE = 3;    // количество ступеней в ProgressBar
    static final int AUTO_CONNECT_TIMEOUT = 540;  // время между запуском поиска SIM 5 мин = TIMER_CHECK_STATUS * AUTO_CONNECT_TIMEOUT
    static final long VIBRATE_TIME = 200;      //  длительность вибрации при нажатии кнопки
    static final short DEFAULT_LIST_VIEW_DIG_IN_NUMBER = 12; // количество входов по умолчанию
    static final short DEFAULT_LIST_VIEW_ANALOG_IN_NUMBER = 2; // количество входов по умолчанию
    static final short DEFAULT_LIST_VIEW_OUT_NUMBER = 15; // количество выходов по умолчанию
    static final short SB_IN_ON = 1; // SeekBar для цифровых входов в среднем положении
    static final short SB_IN_OFF = 0; // SeekBar для цифровых входов в левом положении
    static final short SB_IN_TIME_OFF = 2; // SeekBar для цифровых входов в правом положении
    static final int DEFAULT_IN_TIME_OFF_DELAY_TIME = 0; // время выключения дискретного входа по умолчанию
    static final int DEFAULT_HOUR = 0; // время выключения дискретного входа по умолчанию
    static final int DEFAULT_MINUTE = 10; // время выключения дискретного входа по умолчанию

    static final int DIG_IN = 0; // цифровой вход
    static final int ANALOG_IN = 1; // аналоговый вход

    static final short CMD_ADC_MAIN_STATUS_FROM = 5; // начало флагов статуса охраны в команде ADC
    static final short CMD_ADC_LARGER_LATCH_FROM = 10; // начало флагов защелки статуса входов по превышшению в команде ADC
    static final short CMD_ADC_LESS_LATCH_FROM = 19; // начало флагов защелки статуса входов на уменьшение в команде ADC
    static final short CMD_ADC_SHOCK_LATCH_FROM = 28; // начало флагов защелки статуса входов в диапазоне в команде ADC
    static final short CMD_ADC_LARGER_CUR_FROM = 37; // начало текущих флагов статуса входов по превышшению в команде ADC
    static final short CMD_ADC_LESS_CUR_FROM = 46; // начало текущих флагов статуса входов по уменьшению в команде ADC
    static final short CMD_ADC_SHOCK_CUR_FROM = 55; // начало текущих флагов статуса входов в диапазоне в команде ADC
    static final short CMD_ADC_RSSI_FROM = 64; // начало значения RSSI в команде ADC
    static final short CMD_ADC_ON_OFF_STATUS_FROM = 12; // начало флагов сатуса входов в команде ADC_ON_OFF
    static final short IN_VOLTAGE_POSITION = 2; // позиция напряжения питания в посылке ADC Val
    static final short CAR_BATTERY_VOLTAGE_POSITION = 2; // напряжение автомобильной батареи
    static final short RTC_BATTERY_POSITION = 3; // напряжение батарейки RTC
    static final short TEMPERATURE_POSITION = 4; // температура

    static final short CMD_INPUT_A_MAIN_STATUS_FROM = 9; // начало флагов статуса охраны в команде INPUT
    static final short CMD_INPUT_A_CUR_ON_FROM = 14; // начало флагов включенных датчиков в команде INPUT_A
    static final short CMD_INPUT_A_STATUS_FROM = 39; // начало флагов сатуса входов в команде INPUT_А

    static final short CMD_CAN_A_MAIN_STATUS_FROM = 10; // начало флагов статуса охраны в команде CAN A
    static final short CMD_CAN_A_CUR_ON_FROM = 15; // начало флагов включенных датчиков в команде CAN A
    static final short CMD_CAN_A_STATUS_FROM = 24; // начало флагов сатуса входов в команде CAN A

    static final short CMD_ADC_A_MAIN_STATUS_FROM = 7; // начало флагов стауса входов в команде ADC_A
    static final short CMD_ADC_A_STATUS_FROM = 30; // начало флагов стауса входов в команде ADC_A
    static final short CMD_ADC_A_LARGER_FROM = 12; // начало флагов LARGER в команде ADC_A
    static final short CMD_ADC_A_LESS_FROM = 21; // начало флагов LESS в команде ADC_A

    static final short CMD_INPUT_ON_OFF_STATUS_FROM = 14; // начало флагов сатуса входов в команде INPUT_ON_OFF
    static final short LENGTH_INPUT_GROUP = 4; // длина данных для группы входов (по 16 входов)
    static final short NUMBER_BT_DIGITAL_INPUTS = 96; // количество цифровых входов в посылке по BT
    static final short NUMBER_BT_DIGITAL_OUTPUTS = 128; // количество цифровых входов в посылке по BT
    static final short NUMBER_BT_ANALOG_INPUTS = 32; // количество аналоговых входов в посылке по BT
    static final short NUMBER_BT_CAN = 32; // количество входов CAN в посылке по BT

    static final short CMD_CAN_ON_OFF_STATUS_FROM = 15; // начало флагов сатуса входов в команде CAN_ON_OFF


    static final short ALL_OUT = 0;         // количество для выключения всех выходов
    static final short CMD_OUT_ON_OFF_STATUS_FROM = 15; // начало флагов сатуса входов в команде INPUT_ON_OFF

    private static final short CMD_INPUT_TIME_OFF_TIME_NUMBER = 24; // количество времен в посылке
    static final short INPUT_OFF_TIME_NUMBER = 2; // положение переключателя в списке настроек цифровых входов
    static final int MAX_MAIN_STATUS_SIZE = 200; // максимальный размер списка событий на главном экране
    static final int TIMER_BT_TASK = 250;   // периодичность вызова timerBTTaskHandler
    static final int TIMER_PAUSE_INIT_MESSAGE = 10000;   // периодичность вызова timerPauseExecution
    static final int TIMER_DELAY = 500;   // периодичность вызова timerPauseExecution

    /**
     * установка в digInStatus актуальных значений
     * @param digitalInputCurrent - массив текущих значений состояний дискретных входов
     * @param oldDigitalInputCurrent - массив прошлых значений состояний дискретных входов
     * @param digitalInputACurOn - массив значений состояний дискретных входов при установке на охрану
     * @param digInStatus - список для изменения
     */
    static void modifyInStatus(boolean[] digitalInputCurrent, boolean[] oldDigitalInputCurrent,
                               boolean[] digitalInputACurOn, ArrayList<String> digInStatus) {
        int len = digInStatus.size();
        for (int i = 0; i < len; i++) {
            if (digitalInputCurrent[i]) {
                digInStatus.set(i, STATUS_INPUT_ON);
            }
            else {
                digInStatus.set(i, STATUS_INPUT_OFF);
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
            else {
                // если входы уже обрабатываются, нужно их выключить
                if (analogInStatus.get(i).equals(STATUS_INPUT_FAULT_LARGER))
                    analogInStatus.set(i, STATUS_INPUT_OFF);
            }
            if (!analogACurLess[i]){
                //вход не обрабатывается по уменьшению
                analogInStatus.set(i, STATUS_INPUT_FAULT_LESS);
            }
            else {
                // если входы уже обрабатываются, нужно их выключить
                if (analogInStatus.get(i).equals(STATUS_INPUT_FAULT_LESS))
                    analogInStatus.set(i, STATUS_INPUT_OFF);
            }
        }
    }

    /**
     * добавление false элементов в ArrayList
     * @param length - длина списка
     * @param list - список для обработки
     */
    static void addFalseArrayList(int length, ArrayList<Boolean> list) {
        for (int i = 0; i < length; i++) {
            list.add(i, false);
        }
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
     * установка всех элементов массива boolean[] в true
     * @param length - длина массива
     * @param list - массив для обработки
     */
    static void setTrueArray(int length, boolean[] list) {
        for (int i = 0; i < length; i++)
            list[i] = true;
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
        } else
            return 0;
    }

    static boolean parseRxInputTimeOffDelayStart(String parseData, ArrayList<Integer> digInTimeOff, int inNumber ) {
        int endIndex = 0, startIndex = 0;
        int time, i = 0;
        int digInTimeOffSize = digInTimeOff.size();
        int groupNumber = 0;
        try {
            // находим символ "," - признак следующего символа
            startIndex = parseData.indexOf(',', 0);
            endIndex = parseData.indexOf(',', (startIndex + 1));
            if (endIndex >= 0) {
                // выделячем команду от startIndex до endIndex
                groupNumber = Integer.parseInt(parseData.substring(startIndex + 1, endIndex), 10);
                groupNumber--;
                startIndex = endIndex;
            }
            else return false;   // данные не удалось распознать
            // проверяем нужно ли обрабатывать данную посылку по количеству активных входов
            if (groupNumber * CMD_INPUT_TIME_OFF_TIME_NUMBER < inNumber ) {
                do {
                    endIndex = parseData.indexOf(',', (startIndex + 1));
                    if (endIndex >= 0) {
                        // выделячем команду от startIndex до endIndex
                        time = Integer.parseInt(parseData.substring(startIndex + 1, endIndex), 16);
                    } else {
                        // выделячем команду от startIndex до endIndex
                        time = Integer.parseInt(parseData.substring(startIndex + 1, parseData.length()), 16);
                    }
                    // обновляем значение времени в digInTimeOff
                    digInTimeOff.set(i + (CMD_INPUT_TIME_OFF_TIME_NUMBER * groupNumber), time);
                    startIndex = endIndex;
                    i++;
                } while (endIndex >= 0 & i < digInTimeOffSize);
            }
            return true;
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return false;   // данные не удалось распознать
        }
    }

    static boolean parseRxAdcVal(String parseData, ArrayList<Float> analogVal ) {
        int endIndex = 0, startIndex = 0, i = 0;
        float value;
        int analogValSize = analogVal.size();
        try {
            // находим символ "," - признак следующего символа
            startIndex = parseData.indexOf(',', 0);
            // проверяем нужно ли обрабатывать данную посылку по количеству активных входов
            do {
                endIndex = parseData.indexOf(',', (startIndex + 1));
                if (endIndex >= 0) {
                    // выделячем команду от startIndex до endIndex
                    value = Float.parseFloat(parseData.substring(startIndex + 1, endIndex));
                } else {
                    // выделячем команду от startIndex до endIndex
                    value = Float.parseFloat(parseData.substring(startIndex + 1, parseData.length()));
                }
                // обновляем значение времени в digInTimeOff
                analogVal.set(i, value);
                startIndex = endIndex;
                i++;
            } while (endIndex >= 0 & i < analogValSize);
            return true;
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return false;   // данные не удалось распознать
        }
    }

    /**
     * проверка всех элементов списка на False
     * @param outState - номера битов для проверки
     * @return [true] - все элементы списка false, [false] - есть эленент true
     */
    static boolean checkAllFalse (ArrayList<Boolean> outState) {
        // перебираем все элементы списка и выходим с нужным результатом
        for (boolean item : outState) {
            if (item)
                return false;
        }
        return true;
    }

    /**
     * выделение значения RSSI из принятых данных
     */
    private int parseRxRSSI(String parse_data, int start_index) {
        String strInput = parse_data.substring(start_index, start_index + 2);
        int val = Integer.parseInt(strInput, 16);
        return ((byte) ~val);
    }

}
