package com.wztechs.remo.service;

import com.wztechs.remo.GraphicalUI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class BroadCastReceiver {
    private final static int PORT = 6913;

    private DatagramSocket socket;

    public BroadCastReceiver(GraphicalUI ui){

        startUDPReceiver();

        try {
            this.socket = new DatagramSocket();

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void returnMessage(String ip) throws IOException {
        String msg = "hi";
        byte[] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), PORT);
        socket.send(packet);
    }

    private void startUDPReceiver(){
        (new Thread(() -> {
            try {

            byte[] buf = new byte[64];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            DatagramSocket receiver = new DatagramSocket(PORT);
                do {
                    receiver.receive(packet);

                    String data = new String(packet.getData(), 0, packet.getLength());
                    if(data.equals("hello")){
                        returnMessage(packet.getAddress().getHostAddress());
                    }
                } while (true);

            } catch (IOException e) {
                e.printStackTrace();
            }

        })).start();
    }

}
