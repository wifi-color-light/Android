package com.wifi_color_light;

import android.net.wifi.WifiInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static android.R.attr.button;
import static android.R.attr.id;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt = (Button)findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Configuration config = new Configuration(getApplicationContext());
                Log.i("MainActivity", "onCreate: 已连接的wifi名称" + config.connectedWifiName());
                if (config.setRouterInfo("360WiFi-D9B905","guihui1104.") == 0)
                {
                    Log.i("MainActivity", "onCreate: 配置路由器信息成功");
                }else {
                    Log.i("MainActivity", "onCreate: 配置路由器信息失败");
                }
            }
        });

    }
}
