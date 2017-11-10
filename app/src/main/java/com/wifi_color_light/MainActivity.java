package com.wifi_color_light;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import static android.R.attr.button;
import static android.R.attr.id;

public class MainActivity extends AppCompatActivity {
    private AlertDialog dialog;
    Button routerBt ;
    EditText routerName;
    EditText routerPass;
    EditText deviceAPname;
    EditText deviceAPpass;
    Button configBt;
    Button scanBt;
    Configuration config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        routerBt = (Button) findViewById(R.id.router);
        routerName = (EditText)findViewById(R.id.routerNameText);
        routerPass = (EditText)findViewById(R.id.routerPassText);
        deviceAPname = (EditText)findViewById(R.id.deviceAPname);
        deviceAPpass = (EditText)findViewById(R.id.deviceAPpass);
        configBt = (Button)findViewById(R.id.configBt) ;
        scanBt = (Button)findViewById(R.id.scanBt);
        config = new Configuration(MainActivity.this);
        routerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String wifiName = config.connectedWifiName();
                routerName.setText(wifiName.substring(1,wifiName.length()-1));
            }
        });
        scanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<ScanResult> scanResults = config.scanAccessPoint();
                deviceAPname.setText("LAPTOP-K9P8O50M");
                deviceAPpass.setText("01P7r9@5");
            }
        });
        configBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if( config.changeWifiConnected(deviceAPname.getText().toString(),deviceAPpass.getText().toString()))
               {

                   //config.setRouterInfo(routerName.getText().toString(),routerPass.getText().toString());
               }

            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    new DynamicPermissions(MainActivity.this).showDialogTipUserGoToAppSettting();
                    ;
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
