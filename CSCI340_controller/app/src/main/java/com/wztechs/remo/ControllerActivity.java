package com.wztechs.remo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.*;
import android.text.Editable;
import android.text.InputType;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.example.csci340_controller.R;
import com.wztechs.remo.service.ActionType;
import com.wztechs.remo.service.connection.ConnectTask;
import com.wztechs.remo.service.connection.ConnectionListener;
import com.wztechs.remo.service.connection.Connector;


public class ControllerActivity extends AppCompatActivity implements ConnectionListener, ActionType {

    private final static int SHOW_DIALOG = 1;
    private final static int HIDE_LOADING = 2;

    private String errorMessage;
    private View.OnTouchListener touchScrollEvent, touchPadEvent;
    private Connector conn;
    private AlertDialog authDialog;
    private Handler mHandler;
    private EditText keyboard;

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        errorMessage = "";
        mHandler = constructMessageHandler();
        authDialog = constructAuthDialog();
        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        new ConnectTask(( conn = new Connector(ip, this))).execute(); // makes new connection
    }

    @Override
    public void onStop() {
        super.onStop();
        conn.stop();
        conn = null;
        authDialog = null;
        mHandler = null;
        touchPadEvent = null;
        touchScrollEvent = null;
    }


    @Override
    public void onConnMessageReceived(String message) {
        try {
            String[] args = message.split("&");
            switch (args[0]) {
                case KEY_FOCUS:
                    keyboard.setText(args[1]);
                    break;
            }
        }
        catch(Exception e){}
    }

    @Override
    public void onConnError(String type, Exception e) {
        errorMessage = type;
    }

    @Override
    public void onConnSuccess() {
        //hide progress bar
        Message message = mHandler.obtainMessage(HIDE_LOADING);
        message.sendToTarget();

        //register event
        registerEvents();
    }

    @Override
    public void onConnClose() {
        backToMain();
    }

    @Override
    public void onConnStart() {
        //no implementation needed
    }

    @Override
    public void onConnAuthentication() {
        Message message = mHandler.obtainMessage(SHOW_DIALOG, "");
        message.sendToTarget();
    }

    @Override
    public void onConnAuthenticationSuccess() {
        authDialog = null;
    }

    @Override
    public void onConnAuthenticationFailed() {
        Message message = mHandler.obtainMessage(SHOW_DIALOG, "Invalid password, please retry!");
        message.sendToTarget();
    }


    //register event
    private void registerEvents(){

        //set scroll event
        setTouchPadEvent(super.findViewById(R.id.pad));
        setTouchScrollEvent(super.findViewById(R.id.scroll));
        setKeyboardEvent(keyboard = super.findViewById(R.id.keyboard));

        final TextView rightMouse = super.findViewById(R.id.lcBtn);
        final TextView leftMouse = super.findViewById(R.id.rcBtn);

        View.OnTouchListener touchMouseEvent = new View.OnTouchListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onTouch(View view, MotionEvent e) {

                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setPressed(true);
                        conn.sendAction(view.getId() == R.id.rcBtn ? MOUSE_RIGHT_PRESS :  MOUSE_LEFT_PRESS);
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.setPressed(false);
                        conn.sendAction(view.getId() == R.id.rcBtn ? MOUSE_RIGHT_RELEASE :  MOUSE_LEFT_RELEASE);
                        return true;
                }
                return false;
            }
        };
        rightMouse.setOnTouchListener(touchMouseEvent);
        leftMouse.setOnTouchListener(touchMouseEvent);

    }

    private void setKeyboardEvent(EditText keyboard){
        keyboard.setImeOptions(EditorInfo.IME_ACTION_DONE);
        keyboard.setRawInputType(InputType.TYPE_CLASS_TEXT);
        keyboard.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    conn.sendAction(KEY_FOCUS);
                }
            }
        });
        keyboard.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //on done event
                if(actionId==EditorInfo.IME_ACTION_DONE){
                    //send modified text to other end and clear keyboard focus and text
                    conn.sendAction(KEY_INSERT, keyboard.getText().toString());
                    keyboard.setText("");
                    keyboard.clearFocus();
                }
                return false;
            }
        });
    }

    private void setTouchPadEvent(TextView pad){
        if(touchPadEvent == null) {
            touchPadEvent = new View.OnTouchListener() {

                private Point prevP = new Point();
                private int sense = 1;

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onTouch(View view, MotionEvent e) {
                    int x = (int) e.getRawX();
                    int y = (int) e.getRawY();
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.setPressed(true);
                            prevP.set(x, y);
                            return true;
                        case MotionEvent.ACTION_UP:
                            view.setPressed(false);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            int xDiff = (prevP.x - x) * sense;
                            int yDiff = (prevP.y - y) * sense;
                            conn.sendAction(MOUSE_MOVE, Integer.toString(xDiff), Integer.toString(yDiff));
                            prevP.set(x, y);
                            return true;
                    }
                    return false;
                }
            };
        }
        pad.setOnTouchListener(touchPadEvent);
    }

    private void setTouchScrollEvent(TextView scroll){
        if(touchScrollEvent == null) {
            touchScrollEvent = new View.OnTouchListener() {

                private int prevY;
                private double sense = 0.15;

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onTouch(View view, MotionEvent e) {
                    int y = (int) e.getRawY();
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            prevY = y;
                            view.setPressed(true);
                            return true;
                        case MotionEvent.ACTION_UP:
                            view.setPressed(false);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            int yDiff = (int)((y - prevY) * sense);
                            conn.sendAction(MOUSE_SCROLL, Integer.toString(yDiff));
                            prevY = y;
                            return true;
                    }
                    return false;
                }
            };
        }
        scroll.setOnTouchListener(touchScrollEvent);
    }

    //ui thread message handler
    private Handler constructMessageHandler(){
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch(message.what){
                    case SHOW_DIALOG:
                        authDialog.setMessage((String)message.obj);
                        authDialog.show();
                        break;
                    case HIDE_LOADING:
                       findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        break;
                }
            }
        };
    }

    //authentication dialog
    private AlertDialog constructAuthDialog()
    {
        //construct container
        final LinearLayout dialogContainer = new LinearLayout(this);
        dialogContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 0, 50, 0);
        final EditText passwordField = new EditText(this);
        passwordField.setLayoutParams(lp);
        passwordField.setGravity(android.view.Gravity.TOP | android.view.Gravity.LEFT);
        passwordField.setLines(1);
        passwordField.setMaxLines(1);
        dialogContainer.addView(passwordField);

        return new AlertDialog.Builder(this)
                .setTitle("Password Required")
                .setView(dialogContainer)
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(conn.setEncryptionKey(passwordField.getText().toString())){
                            conn.testConnection();
                        }else{
                            Message message = mHandler.obtainMessage(SHOW_DIALOG, "Invalid password, please retry!");
                            message.sendToTarget();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                backToMain();
            }
        }).create();
    }

    private void backToMain(){
        //set status to canceled and put error message
        if(!errorMessage.isEmpty() )
            setResult(RESULT_CANCELED,  new Intent().putExtra("error",errorMessage));

        finish();
    }
}

