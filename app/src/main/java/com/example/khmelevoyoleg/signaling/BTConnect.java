package com.example.khmelevoyoleg.signaling;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class BTConnect extends AsyncTask<Integer, Void, String> {

    private static final String LOG_TAG = "SERVICE_LOG";
    private static final String CONNECTION_OK = "CONNECTION_OK";
    private static final String CONNECTION_INTERRUPTED = "CONNECTION_INTERRUPTED";
    static final String CONNECTION_ERROR = "CONNECTION_ERROR";
    private BTService serviceBT = null;

    // получаем ссылку на MainActivity
    void link(BTService act) {
        serviceBT = act;
    }

    @Override
    protected String doInBackground(Integer... delays) {
        try {
            long delay = delays[0];
            TimeUnit.SECONDS.sleep(delay);
            BTService.btClientSocket.connect();
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
        Log.d(LOG_TAG, "Connect end. Result = " + result);
        super.onPostExecute(result);
        serviceBT.onPostExecuteBTConnect(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(LOG_TAG, "Connect cancel");
    }
}
