package com.wztechs.remo;

import com.wztechs.remo.service.BroadCastReceiver;

import javax.swing.*;

public class Main {

    public static void main(String[] argv) {
        try {
            //set nimbus ui theme
            for (UIManager.LookAndFeelInfo info:UIManager.getInstalledLookAndFeels()){
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            //initialize the user interface engine and display
            GraphicalUI ui=new GraphicalUI();
            ui.display();

            //start receive and respond to broadcast
            new BroadCastReceiver(ui);

            //start the receiver
            (new Thread(new Receiver(ui))).start();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}