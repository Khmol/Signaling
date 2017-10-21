package com.example.khmelevoyoleg.signaling;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class BTTx extends AsyncTask<Integer, Void, String> {

    private static final String LOG_TAG = "MY_LOG";  // вывод в LOG
    private static final String TX_ERROR = "TX_ERROR";  // ошибка передачи
    private static final String TX_INTERRUPTED = "TX_INTERRUPTED";  // передача прервана
    private static final String TX_OK = "TX_OK";  // передача выполнена успешно
    private String txData;      // данные для передачи
    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
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
            TimeUnit.SECONDS.sleep(delay);
            if (activity.mClientSocket != null) {
                if (activity.mOutStream == null)
                    activity.mOutStream = activity.mClientSocket.getOutputStream();
                byte[] byteArray = txData.getBytes();
                activity.mOutStream.write(byteArray);
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
        activity.onPostExecuteBTConnect(result);
        Log.d(LOG_TAG, "Tx end. Result = " + result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(LOG_TAG, "Tx cancel");
    }
}
