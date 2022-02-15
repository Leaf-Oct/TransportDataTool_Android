package com.leafoct.textmessage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.InetAddresses;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Bootloader extends AppCompatActivity {
    //    private InetAddress ia=null;
//    private char way;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootloader);
        if (!wifiOrHotpoint()) {
            Toast.makeText(this, "貌似没有连接局域网", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        final String local_ip = getIpAddress();
        getSupportActionBar().setTitle(local_ip);
        Button enter = findViewById(R.id.enter);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText pc_ip = findViewById(R.id.pc_ip);
                String pcip = pc_ip.getText().toString();
                if(!local_ip.startsWith("192.168.")){
                    Toast.makeText(Bootloader.this, "手机IP不对劲", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                Intent i = new Intent(Bootloader.this, MainActivity.class);
                i.putExtra("pc_ip", pcip);
                i.putExtra("local_ip", local_ip);
                startActivity(i);
            }
        });
    }

    private boolean wifiOrHotpoint() {
        //judge whether wifi is connected
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        //judge wifi ap
        try {
            WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //通过反射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            if (state == value) {
                return true;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }


//    code of this method from StackOverflow. 感谢前辈~
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

                    if (inetAddress.isSiteLocalAddress()) {
                        ip=inetAddress.getHostAddress();
                        if(ip.startsWith("192.168."))
                        return ip;
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip= "Something Wrong! " + e.toString();
        }
        return ip;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        退出的时候杀掉自身进程，腾内存给别的应用
        android.os.Process.killProcess(android.os.Process.myPid());
        System.gc();
        System.exit(0);
//        看看，看看，什么叫业界良心，为他人着想。国内的一众垃圾app学着点，还想着挂后台，扫内存？
    }
}
