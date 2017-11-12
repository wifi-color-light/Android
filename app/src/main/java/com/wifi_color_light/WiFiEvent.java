package com.wifi_color_light;

import android.net.NetworkInfo;

/**
 * Created by chenguihui on 2017/11/12.
 * Email: 494723324@qq.com
 */

public class WiFiEvent {
    private NetworkInfo.State state;

    public WiFiEvent(NetworkInfo.State state) {
        this.state = state;
    }

    public NetworkInfo.State getState() {
        return state;
    }
}
