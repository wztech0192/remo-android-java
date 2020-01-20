package com.wztechs.remo;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;
import androidx.core.content.ContextCompat;
import com.example.csci340_controller.R;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("test",""+android.os.Build.VERSION.SDK_INT);
        setContentView(R.layout.activity_main);

        // get or create SharedPreferences
        sharedPref = getSharedPreferences("state", MODE_PRIVATE);

        //set status bar background color
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary));

        final EditText ipField = (EditText)super.findViewById(R.id.ip);
        ipField.setText(sharedPref.getString("ip", ""));
        ((Button)super.findViewById(R.id.connBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = ipField.getText().toString();
                if(ip.equals("")) return;
                gotoControlPage(ip);
                sharedPref.edit().putString("ip", ip).apply();
            }
        });
        ((Button)super.findViewById(R.id.searchBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSearchPage();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_CANCELED && data!=null) {
            String msg = data.getStringExtra("error");
            Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);

            toast.setGravity(Gravity.TOP, 0, 200);

            ViewGroup group = (ViewGroup) toast.getView();
            group.setBackgroundColor(Color.parseColor("#fcf8e3ed"));
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(18);
            toast.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void gotoControlPage(String ip){
        Intent intent = new Intent(this, ControllerActivity.class);
        intent.putExtra("ip", ip);
        startActivityForResult(intent, RESULT_CANCELED);
    }

    private void gotoSearchPage(){
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}

