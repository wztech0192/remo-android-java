package com.wztechs.remo;

import com.wztechs.remo.service.Encryptor;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.net.URL;

public class GraphicalUI extends JFrame {
    private JLabel ipLabel;
    private JLabel statusLabel;
    private JTextField pwField;
    private JTextArea msgField;
    private JCheckBox pwCk;
    private JCheckBox enMsgCK;
    private JCheckBox deMsgCK;
    private JScrollPane scrollPane;
    private int connCount = 0;
    private Encryptor ecypt;

    public GraphicalUI(){
        URL iconURL = getClass().getResource("favicon.png");

        ImageIcon img = new ImageIcon(iconURL);

        this.setIconImage(img.getImage());

        final Container root = this.getContentPane();
        root.setFont(Font.getFont(Font.SANS_SERIF));
        root.setLayout(new BorderLayout(0,0));
        root.setBackground(Color.WHITE);
        root.add(createCenterPanel(), BorderLayout.CENTER);

        pwField.setEnabled(false);
        pwField.setEditable(false);
        pwCk.addItemListener(e->{
            if(pwCk.isSelected()){
                pwField.setEnabled(true);
                if(ecypt == null)
                    ecypt = new Encryptor();
                pwField.setText(ecypt.getKeyString());
            }else{
                pwField.setEnabled(false);
                pwField.setText("");
            }
        });

        msgField = new JTextArea("Message.......\n");
        msgField.setRows(4);
        msgField.setEditable(false);
        root.add((scrollPane = new JScrollPane(msgField)), BorderLayout.SOUTH);
    }

    public void display() {
        this.setSize(350, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setTitle("Remo Receiver");
        this.setVisible(true);
    }

    private JPanel createCenterPanel(){
        JPanel center = new JPanel();
        center.setFont(Font.getFont(Font.SANS_SERIF));
        center.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        center.setBackground(null);
        center.setLayout(new GridLayout(4,2, 10, 10));

        center.add(new JLabel("IP Address:"));
        center.add((ipLabel = new JLabel("None")));
        center.add(new JLabel("Connection Status:"));

        center.add((statusLabel = new JLabel("<html><font color='red'>OFF</font></html>")));


       // center.add(new JLabel("Require Password:"));
        center.add((pwCk = new JCheckBox("Require Authentication", false)));
      //  center.add(new JLabel("Password:"));
        center.add(( pwField = new JTextField()));

        center.add((enMsgCK = new JCheckBox("Show Encrypted Msg", false)));
        center.add((deMsgCK =new JCheckBox("Show Decrypted Msg", false)));

        return center;
    }

    public void setIP(String ip){
        ipLabel.setText(ip);
        this.validate();
    }

    public void updateStatus(boolean status){
        if(status){
            connCount++;
            statusLabel.setText("<html><font color='green'>ON ("+connCount+")</font></html>");
        }
        else{
            connCount--;
            if(connCount <= 0)
                statusLabel.setText("<html><font color='red'>OFF</font></html>");
        }
        this.validate();
    }

    public void addEncryptedMessage(String msg){
        if(enMsgCK.isSelected()){
            addMessage("Encrypted: "+msg);
        }
    };

    public void addDecryptedMessage(String msg){
        if(deMsgCK.isSelected()){
            addMessage("Decrypted: "+msg);
        }
    }

    public boolean requireAuthentication(){
        return pwCk.isSelected();
    }

    public Encryptor getEcypt(){
        return ecypt;
    };

    private void addMessage(String msg){
        msgField.append(msg+"\n");
        DefaultCaret caret = (DefaultCaret)msgField.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }
}
