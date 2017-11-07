package com.wifi_color_light;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;


/**
 * Created by chenguihui on 2017/11/7.
 * Email: 494723324@qq.com
 */

public class WIFIStateChangedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BroadcastReceiver", "onReceive: " + intent.getAction().toString());
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state== NetworkInfo.State.CONNECTED;//当然，这边可以更精确的确定状态
                Log.i(this.getClass().getSimpleName(), "isConnected"+isConnected);
                if(isConnected){
                }else{

                }
            }
        }


    }
}
