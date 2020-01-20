package com.wztechs.remo.service;


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class ActionHandler implements ActionType{

    private final static int TYPE = 0;
    private final static String SEPARATOR = "&";
    //awt robot use to perform mouse click, move, scroll, and key typing
    private Robot robot;

    public ActionHandler(){
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    void performAction(String actionCode, ActionCallback cb){
        if(actionCode == null) return;

        try {
            String[] args = actionCode.split(SEPARATOR);
            //start to perform all type of actions
            switch (args[TYPE]) {
                case MOUSE_MOVE:
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    int xs = Integer.parseInt(args[1]);
                    int ys = Integer.parseInt(args[2]);
                    robot.mouseMove((int)p.getX()-xs, (int)p.getY()-ys);
                    break;
                case MOUSE_SCROLL:
                    robot.mouseWheel(Integer.parseInt(args[1]));
                    break;
                case MOUSE_LEFT_PRESS:
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    break;
                case MOUSE_RIGHT_PRESS:
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    break;
                case MOUSE_LEFT_RELEASE:
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    break;
                case MOUSE_RIGHT_RELEASE:
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                    break;
                case KEY_FOCUS:
                    //copy
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_C);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.keyRelease(KeyEvent.VK_C);
                    Thread.sleep(300);
                    String str = getClipboardContents();
                    cb.KeyFocusCallBack(KEY_FOCUS+"&"+str);
                    break;
                case KEY_INSERT:
                    setClipboardContents(args[1]);
                    //paste
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.keyRelease(KeyEvent.VK_V);
                    break;
                case SYSTEM_QUIT:
                    System.exit(0);
                    break;
                default:
                    // System.out.println(args[TYPE]);
            }
        }
        catch(IndexOutOfBoundsException | NumberFormatException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private void setClipboardContents(String string){
        StringSelection stringSelection = new StringSelection(string);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private String getClipboardContents() {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText =
                (contents != null) &&
                        contents.isDataFlavorSupported(DataFlavor.stringFlavor)
                ;
        if (hasTransferableText) {
            try {
                result = (String)contents.getTransferData(DataFlavor.stringFlavor);
            }
            catch (UnsupportedFlavorException | IOException ex){
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
        if(result.length() > 400)
            return result.substring(0, 400);
        else
            return result;
    }
}

