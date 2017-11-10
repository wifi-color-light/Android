package com.wifi_color_light;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by chenguihui on 2017/11/10.
 * Email: 494723324@qq.com
 */

public class WifiAdmin {

    private WifiManager wifiManager;
    private Context context;
    private WifiInfo wifiInfo;
    private List<ScanResult> scanResults;
    private List<WifiConfiguration> wifiConfigurations;
    private WifiManager.WifiLock wifiLock;

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public WifiAdmin(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
    }

    /**
     * 打开WiFi
     *
     * @return true: 打开成功 flase: 打开失败
     */
    public boolean openWifi() {
        if (wifiManager.isWifiEnabled())
            return true;
        return wifiManager.setWifiEnabled(true);
    }

    /**
     * 关闭Wifi
     *
     * @return true: 关闭成功 flase： 关闭失败
     */
    public boolean closeWifi() {
        if (!wifiManager.isWifiEnabled())
            return true;
        return wifiManager.setWifiEnabled(false);
    }

    /**
     * 查看当前Wifi的连接状态
     *
     * @return wifi连接的状态
     */
    public int checkStatus() {
        return wifiManager.getWifiState();
    }

    /**
     * 锁定WifiLock
     */
    public void acquireWifiLock() {
        wifiLock.acquire();
    }

    /**
     * 解锁WifiLock
     */
    public void releaseWifiLock() {
        // 判断时候锁定
        if (wifiLock.isHeld()) {
            wifiLock.acquire();
        }
    }

    /**
     *
     */
    public void creatWifiLock() {
        wifiLock = wifiManager.createWifiLock("Admin");
    }

    /**
     * 获取已保存的wifi热点配置列表
     *
     * @return
     */
    public List<WifiConfiguration> getWifiConfigurations() {
        return wifiConfigurations;
    }

    /**
     * 指定连接配置列表中某个序号的网络
     *
     * @param index 需要连接的网络序号
     * @return 使能网络的结果
     */
    public boolean connectConfigListIndex(int index) {
        return wifiManager.enableNetwork(wifiConfigurations.get(index).networkId, true);
    }

    /**
     * 扫描可连接的网络热点
     *
     * @return 返回当前环境可连接的热点信息
     */
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
        wifiManager.startScan();
        scanResults = wifiManager.getScanResults();
        wifiConfigurations = wifiManager.getConfiguredNetworks();
        return scanResults;
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

    /**
     * @param ssid
     * @param password
     * @return
     */
    public boolean changeWifiConnected(final String ssid, final String password) {
        Log.i("Configuration", "changeWifiConnected: ssid: " + ssid + "\tpassword: " + password);
        wifiInfo = wifiManager.getConnectionInfo();
        //判断当前连接的wifi是否是指定连接的WiFi
        if (wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1).equals(ssid))
            return true;
        //断开当前连接
        wifiManager.disconnect();

        //获取wifi热点配置列表，并将列表中的wifi热点禁止连接
        wifiConfigurations = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration c : wifiConfigurations) {
            wifiManager.disableNetwork(c.networkId);
        }
        //判断指定连接的热点信息是否存在于配置列表中；
        WifiConfiguration config = isExist(ssid);
        if (config != null && password == null)
            return wifiManager.enableNetwork(config.networkId, true);
        else if (config != null) wifiManager.removeNetwork(config.networkId);

        //创建新的热点配置信息，并添加至配置列表中
        int wcgID = wifiManager.addNetwork(createWifiConfig(ssid, password, WIFICIPHER_WPA));
        wifiManager.enableNetwork(wcgID, true);
        //创建线程，规定时间查询wifi连接是否切换成功
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i;
                for (i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1).equals(ssid)) {
                        break;
                    }
                }
                if (i >= 10)
                    context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                else Log.i("changeWifiConnected", "run: 已成功连接至指定wifi热点");
            }
        });
        thread.start();

        for (WifiConfiguration c : wifiConfigurations) {
            wifiManager.enableNetwork(c.networkId, true);
        }


        return true;
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
