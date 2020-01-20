package com.wztechs.remo;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.csci340_controller.R;
import com.wztechs.remo.service.broadcast.BroadCastListener;
import com.wztechs.remo.service.broadcast.BroadCaster;

import java.util.ArrayList;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity implements  BroadCastListener{

    private final static String EMPTY_PLACEHOLDER = "No Device Found...";

    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    private BroadCaster broadCaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view);

        listItems = new ArrayList<String>();
        listItems.add(EMPTY_PLACEHOLDER);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        ListView list = super.findViewById(R.id.list);
        list.setAdapter(adapter);

        (broadCaster = new BroadCaster(this, (WifiManager)
                Objects.requireNonNull(this.getApplicationContext().getSystemService(Context.WIFI_SERVICE)))).execute();

        super.findViewById(R.id.searchBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadCaster.sendMessage();
                listItems.clear();
                listItems.add(EMPTY_PLACEHOLDER);
                adapter.notifyDataSetChanged();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String ip = listItems.get(i);
                if(!ip.equalsIgnoreCase(EMPTY_PLACEHOLDER))
                    gotoControlPage(ip);
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        broadCaster.stop();
    }

    private void gotoControlPage(String ip){
        Intent intent = new Intent(this, ControllerActivity.class);
        intent.putExtra("ip", ip);
        startActivity(intent);
        this.finish();
    }

    private void addItem(String name){
        if(!listItems.contains(name)) {
            if(!listItems.isEmpty() && listItems.get(0).equals(EMPTY_PLACEHOLDER )) {
                listItems.remove(0);
            }

            listItems.add(name);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void OnReceivedMessage(String msg, String address) {
        Log.d("udp",msg+", "+address);
        if(msg.equalsIgnoreCase("hi"))
            addItem(address);
    }

    @Override
    public void OnError(Exception e) {

    }
}
