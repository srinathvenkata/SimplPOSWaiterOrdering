package com.simplpos.waiterordering;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PushMessagesActivity extends AppCompatActivity {

    TextView title,message;
    ImageView close;
    String str_push_title, str_push_msg;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_messages);

        bundle = getIntent().getExtras();
        try{
            str_push_title = bundle.getString("title");
            str_push_msg = bundle.getString("message");
            System.out.println("Meskhf :: "+str_push_msg);
        }catch(Exception e){ }


        title = (TextView) findViewById(R.id.title);
        message = (TextView) findViewById(R.id.message);
        close = (ImageView) findViewById(R.id.close);
        title.setText(str_push_title);
        message.setText(str_push_msg);
        close.setOnClickListener((View.OnClickListener) this);
        Log.v("MyFirebaseMsgService","Push messages activity has loaded");
    }
}