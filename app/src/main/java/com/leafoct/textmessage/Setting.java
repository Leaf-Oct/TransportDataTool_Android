package com.leafoct.textmessage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Setting extends AppCompatActivity {
    public static Activity main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
//        设置标题栏
        getSupportActionBar().setTitle("端口设置");
//        获取输入框并设置成端口号
        final EditText input_port=findViewById(R.id.port_number);
        input_port.setText(String.valueOf(MainActivity.port));
        Button confirm=findViewById(R.id.confirm_change);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int port=0;
                try{
                    port=Integer.valueOf(input_port.getText().toString());
                } catch (NumberFormatException e){
                    Toast.makeText(Setting.this,"输个整数啊可恶",Toast.LENGTH_LONG).show();
                    return;
                }
                MainActivity.port=port;
                main.finish();
                finish();
            }
        });
//        删除线
        TextView delete=(TextView)findViewById(R.id.to_be_deleted);
        delete.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
