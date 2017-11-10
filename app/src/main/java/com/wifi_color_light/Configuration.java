package com.wifi_color_light;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.LoginFilter;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by chenguihui on 2017/11/5.
 * Email: 494723324@qq.com
 */

public class Configuration {

    WifiManager wifiManager = null;

    Configuration(Context context) {
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }


    /**
     * 设置设备需要连接的wifi名称和密码
     *
     * @param ssid     名称
     * @param password 密码
     * @return 成功返回 0；-1 配置失败；-2 获取设备热点信息失败，-3 名称参数为空
     */
    public int setRouterInfo(final String ssid, final String password) {
        final int[] result = new int[1];
        result[0] = -1;
        if (ssid == null) {
            Log.i("setRouterInfo", "run: wifi名称不能为空");
            return -3;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getBSSID() == null)
            return -2;
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        final String IP = (dhcpInfo.gateway & 0xff) + "." + ((dhcpInfo.gateway >> 8) & 0xff) + "."
                + ((dhcpInfo.gateway >> 16) & 0xff) + "." + ((dhcpInfo.gateway >> 24) & 0xff);
        Log.i("setRouterInfo", "run routerIP: " + IP);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                URL url = null;
                URL url2 = null;

                try {
                    url = new URL("http://" + IP + "/ssid:" + ssid + "password:" + password + "\r");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                try {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    Log.i("setRouterInfo", "run: " + sb.toString());
                    if (sb.toString().indexOf("configuration successful") > -1) {
                        Log.i("setRouterInfo", "run: 配置路由信息成功");
                        result[0] = 0;
                    } else {
                        Log.i("setRouterInfo", "run: 配置路由信息失败");
                        result[0] = -1;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
        while (!(thread.getState() == Thread.State.TERMINATED))
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return result[0];
    }






}
