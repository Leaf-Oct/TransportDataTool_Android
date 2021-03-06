package com.leafoct.textmessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;

public class Bootloader extends AppCompatActivity {
    private ArrayList<String> all_ip_arraylist=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootloader);

        final String local_ip = getIpAddress();
        getSupportActionBar().setTitle(local_ip);
        Button enter = findViewById(R.id.enter);
        final EditText pc_ip = findViewById(R.id.pc_ip);
        final EditText secret_key=findViewById(R.id.secret_key);
        SharedPreferences sp=getSharedPreferences("bootData",MODE_PRIVATE);
        pc_ip.setText(sp.getString("ip",""));
        secret_key.setText(sp.getString("key","leafoct"));
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pcip = pc_ip.getText().toString();
                String secret=secret_key.getText().toString();
//                ???????????????ip????????????data?????????
                SharedPreferences.Editor editor=getSharedPreferences("bootData",MODE_PRIVATE).edit();
                editor.putString("ip",pcip);
                editor.putString("key",secret);
                editor.apply();
//                ??????key????????????
                try {
                    MessageDigest md=MessageDigest.getInstance("SHA-256");
                    md.update(secret.getBytes("UTF-8"));
                    Security.initial(md.digest());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                Intent i = new Intent(Bootloader.this, MainActivity.class);
                i.putExtra("pc_ip", pcip);
                i.putExtra("local_ip", local_ip);
                startActivity(i);
            }
        });
    }

//    private boolean wifiOrHotpoint() {
//        //judge whether wifi is connected
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (activeNetwork != null) {
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                return true;
//            }
//        }
//        //judge wifi ap
//        try {
//            WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            //?????????????????? getWifiApState()??????
//            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
//            //??????getWifiApState() ??????????????????
//            int state = (int) method.invoke(manager);
//            //?????????????????? WIFI_AP?????????????????????
//            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
//            //???????????????
//            int value = (int) field.get(manager);
//            //??????????????????
//            if (state == value) {
//                return true;
//            }
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }


//    code of this method from StackOverflow. ????????????~
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if(inetAddress instanceof Inet4Address){
//                    if (inetAddress.isSiteLocalAddress()) {
                        all_ip_arraylist.add(inetAddress.getHostAddress());
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip= "Something Wrong! " + e.toString();
        }
        for(String s:all_ip_arraylist){
            ip=s;
            if(s.startsWith("192.168.")){
                break;
            }
        }
        return ip;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ????????????????????????????????????????????????????????????
        android.os.Process.killProcess(android.os.Process.myPid());
        System.gc();
        System.exit(0);
//        ?????????????????????????????????????????????????????????????????????????????????app?????????????????????????????????????????????
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.boot,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.all_ip){
            AlertDialog.Builder show_ip_dialog=new AlertDialog.Builder(this);
            show_ip_dialog.setTitle("????????????ipv4");
            StringBuffer sb=new StringBuffer();
            for(String s:all_ip_arraylist){
                sb.append(s).append('\n');
            }
            show_ip_dialog.setMessage(sb);
            show_ip_dialog.setCancelable(false);
            show_ip_dialog.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            show_ip_dialog.show();
            return true;
        }
        return false;
    }
}
