package com.example.khmelevoyoleg.signaling;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class BTRx extends AsyncTask<Integer, Void, String> {

    private static final String LOG_TAG = "SERVICE_LOG";  // вывод в LOG
    private static final String LOG_TAG_ERR = "ERR_SERV_LOG";  // вывод в LOG
    private static final String RX_ERROR = "RX_ERROR";  // ошибка передачи
    private static final String RX_ERROR_0 = "RX_ERROR_0";  // ошибка передачи принято 0 байт
    private static final String RX_INTERRUPTED = "RX_INTERRUPTED";  // передача прервана
    private static final String RX_OK = "RX_OK";  // передача выполнена успешно
    private String rxData;      // принятые данные
    //private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу
    private BTService serviceBT = null;
    private static final int EMPTY_MSG = 65535;
    private static final int BUFFER_SIZE = 1024;

    // получаем ссылку на BTService
    void link(BTService act) {
        serviceBT = act;
    }

    /**
     *
     * @param delays - задержка перед запуском приема по BT
     * @return - результат - что получено
     */
    @Override
    protected String doInBackground(Integer... delays) {
        int bytesRead;  // количество полученных байт данных
        byte[] buffer = new byte[BUFFER_SIZE];  // буфер для принятия данных
        String result = "";     // полученные данные

        try {
            // выполняем задержку перед запуском приема
            long delay = delays[0];
            TimeUnit.SECONDS.sleep(delay);
            while (true) {
                int numBytes = serviceBT.mInStream.available();
                if (numBytes == EMPTY_MSG) {
                    // ошибка выполнения mInStream.available()
                    Log.d("MY_LOG", "Rx Error");
                    return RX_ERROR;
                }
                else {
                    if (numBytes != 0) {
                        // данные получены
                        if (BTService.btClientSocket != null) {
                            bytesRead = serviceBT.mInStream.read(buffer);
                            if (bytesRead != -1) {
                                while (bytesRead == BUFFER_SIZE) {
                                    result = result + new String(buffer, 0, bytesRead);
                                    bytesRead = serviceBT.mInStream.read(buffer);
                                }
                                result = result + new String(buffer, 0, bytesRead);
                                return result;
                            }
                            else return RX_ERROR;
                        }
                        else return RX_ERROR;
                    }
                    else return RX_ERROR_0;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return RX_ERROR;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return RX_INTERRUPTED;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result.equals(RX_ERROR_0))
            Log.d(LOG_TAG_ERR, "Rx ERROR = " + result);
        else
            Log.d(LOG_TAG, "Rx end. Result = " + result);
        super.onPostExecute(result);
        serviceBT.onPostExecuteBTRx(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(LOG_TAG, "Rx cancel");
    }
}
