package com.leafoct.textmessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MainActivity extends AppCompatActivity {
    private ListView message_list;
    private Button send;
    private EditText text_message;
    private MessageAdapter ma;
    private InetAddress target;
    public static int port = 2333;
    private DatagramSocket udp_socket;
    public ReceiveMessageThread rmt;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Bundle b = message.getData();
            ma.add(new MessageItem(R.drawable.pc, b.getString("data")));
            message_list.setSelection(message_list.getBottom());
            b.clear();
            b = null;
            System.gc();
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        获取传来的本机和电脑ip，并给标题栏设置一个，醒目一些
        Setting.main = this;
        Intent i = getIntent();
        String localip = i.getStringExtra("local_ip");
        getSupportActionBar().setTitle("本机:" + localip);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ip);
//        pc_ip might be null value
        String pcip = i.getStringExtra("pc_ip");
//        初始化接收信息的服务
        try {
            udp_socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        initialAndStartReceiveMessageService();
//        初始化组件
        message_list = findViewById(R.id.message_list);
        ma = new MessageAdapter(this, R.layout.item_list, new ArrayList<MessageItem>());
        message_list.setAdapter(ma);
//        只有获得了电脑IP才可以发送。正则表达式简单判断IP是否合法
        if (Pattern.matches("192\\.168\\.\\d+\\.\\d+", pcip)) {
//            设置发送的目标地址
            try {
                target = InetAddress.getByName(pcip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
//            获取发送按钮和文本框
            send = findViewById(R.id.send);
            text_message = findViewById(R.id.text);
//            清空文本框，获取文本信息，发送
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message_to_send = text_message.getText().toString();
//                    避免空信息刷屏
                    if (message_to_send.equals("")) {
                        return;
                    }
                    text_message.getText().clear();
                    ma.add(new MessageItem(R.drawable.phone, message_to_send));
//                    每次发送消息后自动聚焦到最底部
                    message_list.setSelection(message_list.getBottom());
//                    发送文本信息(明文。怕不安全的话可以加上我上一次写的RSA加改进版凯撒加密的模块)
//                    不过应该是安全的，没人会随便监听2333这种鬼畜端口吧
                    new SendMessageThread(message_to_send).start();
                }
            });
        } else {
//            否则不显示底下的发送栏
            findViewById(R.id.bottom_frame).setVisibility(View.INVISIBLE);
        }
//        点击其中某项后复制文本内容
        message_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageItem mi = ma.getItem(i);
                copyMessage(mi.getContent());
            }
        });

    }

    //    初始化接收信息活动
    private void initialAndStartReceiveMessageService() {
        rmt = new ReceiveMessageThread();
        rmt.start();
    }

    //    标题栏右边的设置图标
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //    标题栏右边设置图标的活动。进入端口的设置
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.set_port) {
            startActivity(new Intent(this, Setting.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        停掉服务，会报的错不用管，不影响正常使用
        udp_socket.close();
    }

    class SendMessageThread extends Thread {
        private String text;

        public SendMessageThread(String t) {
            text = t;
        }

        @Override
        public void run() {
            super.run();
            byte[] b;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                b = text.getBytes(StandardCharsets.UTF_8);
            } else {
                b = text.getBytes();
            }
            DatagramPacket data = new DatagramPacket(b, b.length, target, port);
            try {
                udp_socket.send(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    class ReceiveMessageThread extends Thread {
        @Override
        public void run() {
            super.run();
            DatagramPacket pack = null;
            byte b[] = new byte[8192];
            pack = new DatagramPacket(b, b.length);
            while (true) {
                try {
                    if (udp_socket.isClosed()) {
                        break;
                    }
                    udp_socket.receive(pack);
                    String s = new String(pack.getData(), 0, pack.getLength(),"UTF-8");
                    Bundle bundle = new Bundle();
                    bundle.putString("data", s);
                    Message m = new Message();
                    m.setData(bundle);
                    handler.sendMessage(m);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    private void copyMessage(String s) {
        ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, s);
        clipboard.setPrimaryClip(clipData);
    }
}
