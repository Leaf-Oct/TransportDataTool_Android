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
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
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
//        ??????????????????????????????ip?????????????????????????????????????????????
        Setting.main = this;
        Intent i = getIntent();
        String localip = i.getStringExtra("local_ip");
        getSupportActionBar().setTitle("??????:" + localip);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ip);
//        pc_ip might be null value
        String pcip = i.getStringExtra("pc_ip");
//        ??????????????????????????????
        try {
            udp_socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        initialAndStartReceiveMessageService();
//        ???????????????
        message_list = findViewById(R.id.message_list);
        ma = new MessageAdapter(this, R.layout.item_list, new ArrayList<MessageItem>());
        message_list.setAdapter(ma);
//        ?????????????????????IP?????????????????????????????????????????????IP????????????
        if (Pattern.matches("\\d+\\.\\d+\\.\\d+\\.\\d+", pcip)) {
//            ???????????????????????????
            try {
                target = InetAddress.getByName(pcip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
//            ??????????????????????????????
            send = findViewById(R.id.send);
            text_message = findViewById(R.id.text);
//            ?????????????????????????????????????????????
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message_to_send = text_message.getText().toString();
//                    ?????????????????????
                    if (message_to_send.equals("")) {
                        return;
                    }
                    text_message.getText().clear();
                    ma.add(new MessageItem(R.drawable.phone, message_to_send));
//                    ?????????????????????????????????????????????
                    message_list.setSelection(message_list.getBottom());
//                    ??????????????????(?????????????????????????????????????????????????????????RSA?????????????????????????????????)
//                    ????????????????????????????????????????????????2333?????????????????????
                    new SendMessageThread(message_to_send).start();
                }
            });
        } else {
//            ?????????????????????????????????
            findViewById(R.id.bottom_frame).setVisibility(View.INVISIBLE);
        }
//        ???????????????????????????????????????
        message_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageItem mi = ma.getItem(i);
                copyMessage(mi.getContent());
            }
        });

    }

    //    ???????????????????????????
    private void initialAndStartReceiveMessageService() {
        rmt = new ReceiveMessageThread();
        rmt.start();
    }

    //    ??????????????????????????????
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //    ????????????????????????????????????????????????????????????
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
//        ????????????????????????????????????????????????????????????
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
            try {
                byte[] b = Security.encrypt(text);
                DatagramPacket data = new DatagramPacket(b, b.length, target, port);
                udp_socket.send(data);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
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
                    Bundle bundle = new Bundle();
                    bundle.putString("data", Security.decrypt(Arrays.copyOf(b,pack.getLength())));
                    Message m = new Message();
                    m.setData(bundle);
                    handler.sendMessage(m);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (GeneralSecurityException e){
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
