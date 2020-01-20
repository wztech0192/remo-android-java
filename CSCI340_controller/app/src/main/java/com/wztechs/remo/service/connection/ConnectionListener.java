package com.wztechs.remo.service.connection;

/*
* A interface to be implement inside the Activity
*/

public interface ConnectionListener {

    String TAG = "Listener";

    void onConnMessageReceived(String message);

    void onConnError(String type, Exception e);

    void onConnSuccess();

    void onConnAuthenticationFailed();

    void onConnClose();

    void onConnStart();

    void onConnAuthentication();

    void onConnAuthenticationSuccess();
}
