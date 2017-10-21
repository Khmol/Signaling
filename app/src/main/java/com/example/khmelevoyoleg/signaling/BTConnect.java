package com.example.khmelevoyoleg.signaling;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class BTConnect extends AsyncTask<Integer, Void, String> {

    private static final String LOG_TAG = "MY_LOG";
    static final String CONNECTION_OK = "CONNECTION_OK";
    private static final String CONNECTION_INTERRUPTED = "CONNECTION_INTERRUPTED";
    private static final String CONNECTION_ERROR = "CONNECTION_ERROR";
    private MainActivity activity;

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
    }

    // обнуляем ссылку
    void unLink() {
        activity = null;
    }

    @Override
    protected String doInBackground(Integer... delays) {
        try {
            long delay = delays[0];
            TimeUnit.SECONDS.sleep(delay);
            activity.mClientSocket.connect();
        }
        catch (IOException e) {
            return CONNECTION_ERROR;
        }
        catch (InterruptedException e) {
            return CONNECTION_INTERRUPTED;
        }
        return CONNECTION_OK;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        activity.onPostExecuteBTConnect(result);
        Log.d(LOG_TAG, "Connect end. Result = " + result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(LOG_TAG, "Connect cancel");
    }
}
