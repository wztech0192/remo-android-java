package com.wztechs.remo.service.connection;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;


import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Connector{
    private final static String TAG = "CONNECTOR";
    private final static String CONNECTION_ERROR = "CONNECTION ERROR";
    private final static String SOCKET_ERROR = "SOCKET ERROR";
    private final static String SEPARATOR = "&";
    private final static int TIMEOUT = 5000;
    private final static int SERVER_PORT = 6912;

    private String SERVER_IP; //server IP address

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private ConnectionListener connectionListener;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    //use to encrypt and decrypt message
    private Encryptor ecrypt;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public Connector(String ip, ConnectionListener listener) {
        SERVER_IP = ip;
        connectionListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        if (mBufferOut != null && ecrypt != null) {
            (new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    mBufferOut.println(ecrypt.encrypt(message));
                    mBufferOut.flush();
                }
            })).start();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean setEncryptionKey(String key){
        try {
            ecrypt = new Encryptor(key);
            return true;
        }catch(IllegalArgumentException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e){
            ecrypt = null;
            return false;
        }
    }

    /**
     * Sends the message entered by client to the server
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendAction(String ...actions) {
        if(actions.length == 1)
            sendMessage(actions[0]);
        else
            sendMessage(String.join(SEPARATOR, actions ));
    }

    /**
     * Close the connection and release the members
     */
    public void stop() {
        mRun = false;
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    //test encrypted connection
    public void testConnection(){
        sendMessage("pass");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void run() {
        boolean isAuthenticated = false;
        mRun = true;

        try {
            //on starting of the connection
            connectionListener.onConnStart();

            //get ip address
            InetSocketAddress serverAddr = new InetSocketAddress(SERVER_IP, SERVER_PORT);

            //create a socket to make the connection with the server
            Socket socket = new Socket();
            socket.connect(serverAddr, TIMEOUT);

            try {
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                mServerMessage = mBufferIn.readLine();
                if(mServerMessage.equals("require password")){
                    connectionListener.onConnAuthentication();
                }
                else{
                    //define encryptor with the key retrieved from the receiver
                    setEncryptionKey(mServerMessage);
                    testConnection();
                }

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    mServerMessage = mBufferIn.readLine();
                    if(!isAuthenticated){
                        if(mServerMessage.equals("password failed")){
                            connectionListener.onConnAuthenticationFailed();
                        }else{
                            isAuthenticated = true;
                            connectionListener.onConnAuthenticationSuccess();
                            connectionListener.onConnSuccess();
                        }
                    }
                    else{
                        connectionListener.onConnMessageReceived(ecrypt.decrypt(mServerMessage));
                    }
                }
            } catch (IOException e) {
                connectionListener.onConnError(SOCKET_ERROR, e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (IOException e) {
            connectionListener.onConnError(CONNECTION_ERROR, e);
        }
        connectionListener.onConnClose();
        stop();
    }
}