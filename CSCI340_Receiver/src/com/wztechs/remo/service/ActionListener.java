package com.wztechs.remo.service;

import com.wztechs.remo.GraphicalUI;
import javafx.concurrent.Worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ActionListener implements Runnable, ActionCallback{

    private BufferedReader inFromClient;
    private PrintWriter mBufferOut;
    private ActionHandler handler;
    private Encryptor ecryt;
    private GraphicalUI ui;

    public ActionListener(BufferedReader inFromClient,  PrintWriter mBufferOut, ActionHandler handler, Encryptor ecryt, GraphicalUI ui){
        this.ecryt = ecryt;
        this.mBufferOut = mBufferOut;
        this.inFromClient = inFromClient;
        this.handler = handler;
        this.ui = ui;
    }

    @Override
    public void run() {
        try{

            boolean isAuthenticated = false;
            //first received message from client should be decrypted pass message
            String msg;
            String decryptMsg;


            while(true){
                msg = inFromClient.readLine();
                //exit if msg is null
                if(msg == null ) break;

                if(isAuthenticated){
                    //do this if the user is is authenticated
                    decryptMsg = ecryt.decrypt(msg);
                    ui.addEncryptedMessage(msg);
                    ui.addDecryptedMessage(decryptMsg);
                    //decrypt the msg and let handler to perform the action.
                    handler.performAction(decryptMsg, this);
                }
                //else check if the user is authenticated by decrypt the passcode
                else if(ecryt.isPasscodeValid(msg)){
                    //pass authentication and toggle connection status to ON;
                    isAuthenticated = true;
                    ui.updateStatus(true);
                    mBufferOut.println(msg);
                    mBufferOut.flush();
                }else{
                    //send authentication failed message back to user
                    mBufferOut.println("password failed");
                    mBufferOut.flush();
                }
            }

            if(isAuthenticated){
                //toggle connection status to false;
                ui.updateStatus(false);
                mBufferOut.println("disconnected");
                mBufferOut.flush();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void KeyFocusCallBack(String str) {
        mBufferOut.println(ecryt.encrypt(str));
    }
}
