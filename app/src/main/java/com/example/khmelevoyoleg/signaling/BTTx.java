package com.example.khmelevoyoleg.signaling;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class BTTx extends AsyncTask<Integer, Void, String> {

    static final String LOG_TAG = "SERVICE_LOG";  // вывод в LOG
    static final String TX_ERROR = "TX_ERROR";  // ошибка передачи
    static final String TX_INTERRUPTED = "TX_INTERRUPTED";  // передача прервана
    static final String TX_OK = "TX_OK";  // передача выполнена успешно
    private String txData;      // данные для передачи
    //private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу
    private BTService serviceBT = null;

    // получаем ссылку на MainActivity
    //void link(MainActivity act) {
    //    activity = act;
    //}
    void link(BTService act) {
        serviceBT = act;
    }
    /**
     * конструктор задачи передачи
     * @param _data - данные для передачи
     */
    BTTx (String _data){
        // данные для передачи
        txData = _data;
    }

    /**
     * задача передачи данных по BT
     * @param delays - задержка перед передачей
     * @return - результат передачи
     */
    @Override
    protected String doInBackground(Integer... delays) {
        try {
            long delay = delays[0];
            TimeUnit.MILLISECONDS.sleep(delay);
            if (BTService.btClientSocket != null) {
                if (serviceBT.mOutStream == null)
                    serviceBT.mOutStream = BTService.btClientSocket.getOutputStream();
                byte[] byteArray = txData.getBytes();
                serviceBT.mOutStream.write(byteArray);
            }
            else {
                return TX_ERROR;
            }
            return TX_OK;
        }
        catch (IOException e) {
            e.printStackTrace();
            return TX_ERROR;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return TX_INTERRUPTED;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        serviceBT.onPostExecuteBTTx(result);
        Log.d(LOG_TAG, String.format("Tx end. Result = %s: %s ", result, txData));
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(LOG_TAG, "Tx cancel");
    }
}
