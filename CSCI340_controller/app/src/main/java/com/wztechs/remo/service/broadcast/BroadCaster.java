package com.wztechs.remo.service.broadcast;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.wztechs.remo.service.connection.Connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BroadCaster extends AsyncTask<String, String, BroadCaster> {
    private final static int PORT = 6913;

    private Boolean stop = false;
    private DatagramSocket socket, receiver;
    private BroadCastListener listener;

    public BroadCaster(BroadCastListener listener, WifiManager wm) {
        this.listener = listener;
        // Hack Prevent crash (sending should be done using an async task)

        WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
        multicastLock.acquire();



    }

    public void stop(){
        socket.close();
        receiver.close();
        stop = true;
    }

    public void sendMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buf = "hello".getBytes();
                DatagramPacket packet = null;
                try {
                    packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), PORT);
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startUDPReceiver(){
        (new Thread(() -> {
            try {

                byte[] buf = new byte[64];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receiver = new DatagramSocket(PORT);
                receiver.setReuseAddress(true);
                receiver.setBroadcast(true);
                while(!stop){
                    receiver.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength());
                    listener.OnReceivedMessage(data, packet.getAddress().getHostAddress());
                };
                receiver.close();

            } catch (IOException e) {
                listener.OnError(e);
            }
        })).start();
    }

    @Override
    protected BroadCaster doInBackground(String... strings) {
        startUDPReceiver();

        try {
            StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
