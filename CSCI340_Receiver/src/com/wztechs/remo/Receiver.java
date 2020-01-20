package com.wztechs.remo;

import com.wztechs.remo.service.ActionHandler;
import com.wztechs.remo.service.ActionListener;
import com.wztechs.remo.service.Encryptor;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver implements Runnable{

    private final static int PORT = 6912;
    private GraphicalUI ui;

    Receiver(GraphicalUI ui) throws IOException {
        this.ui = ui;
        InetAddress ip = InetAddress.getLocalHost();
        ui.setIP(ip.getHostAddress());
    }

    @Override
    public void run() {

        final ActionHandler actionHandler = new ActionHandler();
        Encryptor encryptor;

        try {
            ServerSocket socket = new ServerSocket(PORT);

            //start receiver client connection
            while (true) {
                Socket connectionSocket = socket.accept();

                //define reader and writer
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                PrintWriter mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream())), true);

                if(ui.requireAuthentication()){
                    //start a new threat to retrieve current client message
                    (new Thread(new ActionListener(inFromClient, mBufferOut, actionHandler, ui.getEcypt(), ui))).start();
                    //send the require password message to client end
                    mBufferOut.println("require password");
                    mBufferOut.flush();
                }
                else{
                    encryptor = new Encryptor();
                    (new Thread(new ActionListener(inFromClient, mBufferOut, actionHandler, encryptor, ui))).start();
                    //send the encryption key to client
                    mBufferOut.println(encryptor.getKeyString());
                }

                mBufferOut.flush();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
