package com.wifi_color_light;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.seekBar5)
    SeekBar seekBar5;
    @BindView(R.id.seekBar6)
    SeekBar seekBar6;
    @BindView(R.id.seekBar7)
    SeekBar seekBar7;
    private AlertDialog dialog;
    @BindView(R.id.routerNameText)
    EditText routerNameText;
    @BindView(R.id.routerPassText)
    EditText routerPassText;
    @BindView(R.id.router)
    Button router;
    @BindView(R.id.deviceAPname)
    EditText deviceAPname;
    @BindView(R.id.deviceAPpass)
    EditText deviceAPpass;
    @BindView(R.id.scanBt)
    Button scanBt;
    @BindView(R.id.configBt)
    Button configBt;
    @BindView(R.id.button2)
    Button button2;

    private WifiAdmin mwifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mwifi = new WifiAdmin(this);
    }

    @OnClick({R.id.router, R.id.scanBt, R.id.configBt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.router:
                routerNameText.setText(mwifi.getSSID());
                Log.e("TAG", "onClick: ssid=" + mwifi.getSSID());
                break;
            case R.id.scanBt:
                deviceAPname.setText("Mywifi");
                deviceAPpass.setText("cyd123456");
                break;
            case R.id.configBt:
                mwifi.disconnectWifi(mwifi.getNetworkId());
                mwifi.addNetwork(mwifi.CreateWifiInfo("MyWifi", "cyd123456", 3));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
