package com.wifi_color_light;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.text.LoginFilter;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * Created by chenguihui on 2017/11/5.
 * Email: 494723324@qq.com
 */

public class Configuration {
    private  Context context;
    Configuration(Context context){
        this.context = context;
    }

    /**
     * 获取当前已连接的wifi名称
     * @return 如果已经连接上wifi则返回wifi名称，如果未连接则返回null
     */
    public String connectedWifiName(){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo =  wifiManager.getConnectionInfo();
        if (wifiInfo.getBSSID() == null)
            return null;
        return wifiInfo.getSSID();
    }
    public int setRouterInfo(String ssid,String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                URL url= null;
                URL url2 = null;
                try {
                    url = new URL("http://192.168.0.15/gpio/0\r");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    url2=  new URL("http://192.168.0.15/gpio/1\r");

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        connection.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        connection = (HttpURLConnection) url2.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        connection.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        if(connection!=null)
                            connection.disconnect();
                    }
                }
            }
        }).start();
        return 0;
    }
}
