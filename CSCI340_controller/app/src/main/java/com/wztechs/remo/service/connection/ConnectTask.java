package com.wztechs.remo.service.connection;

import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class ConnectTask extends AsyncTask<String, String, Connector> {

    private Connector conn;

    public ConnectTask(Connector conn){
        this.conn = conn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Connector doInBackground(String... strings) {
        conn.run();
        return null;
    }
}