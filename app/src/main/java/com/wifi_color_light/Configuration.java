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
    private Context context;
    WifiManager wifiManager = null;

    Configuration(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 获取当前已连接的wifi名称
     *
     * @return 如果已经连接上wifi则返回wifi名称，如果未连接则返回null
     */
    public String connectedWifiName() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getBSSID() == null)
            return null;
        return wifiInfo.getSSID();
    }

    public List<ScanResult> scanAccessPoint() {
        if (Build.VERSION.SDK_INT >= 24) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (ContextCompat.checkSelfPermission(context, permissions[0])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i("setRouterInfo", "changeWifiConnected: 请求开启定位权限");
                ActivityCompat.requestPermissions((Activity) context, permissions, 1);
            }
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Log.i("setRouterInfo", "changeWifiConnected: 定位服务提供者：" + locationManager.getProviders(true));

            if (locationManager.getProviders(true).size() < 3) {
                Log.i("setRouterInfo", "changeWifiConnected: 定位功能关闭，正在打开");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                ((Activity) context).startActivityForResult(intent, 10);
            }
            if ((ContextCompat.checkSelfPermission(context, permissions[0])
                    != PackageManager.PERMISSION_GRANTED) ||
                    (locationManager.getProviders(true).size() < 3))
                return null;
        }
        /**
         *
         */
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        return scanResults;
    }

    /**
     * @param ssid
     * @param password
     * @return
     */
    public boolean changeWifiConnected(String ssid, String password) {
        Log.i("Configuration", "changeWifiConnected: ssid: " + ssid + "\tpassword: " + password);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // if (wifiInfo.getSSID().substring(1,wifiInfo.getSSID().length()-1).equals(ssid)) return true;
        List<WifiConfiguration> wifiConfigList = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration c: wifiConfigList) {
            wifiManager.disableNetwork(c.networkId);
        }
        //wifiManager.disconnect();
        WifiConfiguration config = isExist(ssid);
        if (config != null && password == null)
            return wifiManager.enableNetwork(config.networkId, true);
        wifiManager.removeNetwork(config.networkId);

        int wcgID = wifiManager.addNetwork(createWifiConfig(ssid, password,WIFICIPHER_WPA));
        Log.i("Configuration", "changeWifiConnected: 准备启动新的wifi连接");
        wifiManager.enableNetwork(wcgID, true);
        wifiManager.reconnect();
        int i;
        for (i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WifiManager wifiManager = (WifiManager) (context.getSystemService(Context.WIFI_SERVICE));
            wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1).equals(ssid) ){
                break;
            }
        }
     /*   for (WifiConfiguration c: wifiConfigList) {
            wifiManager.enableNetwork(c.networkId,true);
        }*/
        if (i >= 10 )return false;
        return true;
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
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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

    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

    private WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        //初始化WifiConfiguration
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        //如果之前有类似的配置
        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            //则清除旧有配置
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        //不需要密码的场景
        if (type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //以WEP加密的场景
        } else if (type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    private WifiConfiguration isExist(String ssid) {

        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }

}
