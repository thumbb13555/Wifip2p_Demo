package com.noahliu.wifip2p_demo;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.noahliu.wifip2p_demo.adapter.SearchDeviceAdapter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifiP2pChannel;
    private TextView tvInfo,tvRespond;
    private ConstraintLayout connectedView;
    private SearchDeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(this, getMainLooper(), this);
        tvInfo = findViewById(R.id.textView_Info);
        //TODO: 使用tvRespond作為字串回傳的顯示
        tvRespond = findViewById(R.id.textView_Res);
        connectedView = findViewById(R.id.constraintLayout_FunctionView);
        EditText edMessage = findViewById(R.id.edittext_Message);
        //廣播過濾器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
        /**點擊'搜索裝置'*/
        Button btSearch = findViewById(R.id.button_Search);
        btSearch.setOnClickListener(view -> {
            deviceAdapter.clearList();
            wifiP2pManager.discoverPeers(wifiP2pChannel,null);
        });
        /**點擊'斷線'*/
        Button btDisconnect = findViewById(R.id.button_Disconnect);
        btDisconnect.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "發起斷線", Toast.LENGTH_SHORT).show();
            wifiP2pManager.cancelConnect(wifiP2pChannel,null);
            wifiP2pManager.removeGroup(wifiP2pChannel,null);
            deviceAdapter.clearList();
        });
        /**點擊'發送'*/
        Button btSend = findViewById(R.id.button_Send);
        btSend.setOnClickListener(view -> {
            String message = edMessage.getText().toString();
            if (message.length() == 0)return;
            //TODO: 此處開始做字串發送

        });
        /**設置搜尋列表*/
        RecyclerView recyclerView = findViewById(R.id.recyclerview_ListOfDevice);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        deviceAdapter = new SearchDeviceAdapter();
        recyclerView.setAdapter(deviceAdapter);
        /**點擊搜索到的物件*/
        deviceAdapter.setClick((device, address) -> {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = address;
            config.wps.setup = WpsInfo.PBC;
            wifiP2pManager.connect(wifiP2pChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "發起連線", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    Toast.makeText(MainActivity.this, "連線失敗", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: 連線失敗");
                }
            });
        });
    }

    //獲取權限
    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100
            );
        }
    }
    //廣播接收器
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: "+intent.getAction() );

            switch (intent.getAction()) {
                // 偵測該設備是否可用
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {

                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.e(TAG, "P2P 可用" );
                    } else {
                        Log.e(TAG, "P2P 不可用" );
                    }
                    break;
                }
                // 搜尋設備的回傳
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                        wifiP2pManager.requestPeers(wifiP2pChannel, new WifiP2pManager.PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                                deviceAdapter.setList(wifiP2pDeviceList.getDeviceList());
                            }
                        });
                    break;
                }
                // 偵測P2P的連線狀態
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.isConnected()) {
                        Toast.makeText(context, "已連線", Toast.LENGTH_SHORT).show();
                        connectedView.setVisibility(View.VISIBLE);
                    }else if(!networkInfo.isConnected()){
                        Toast.makeText(context, "已斷線", Toast.LENGTH_SHORT).show();
                        connectedView.setVisibility(View.GONE);
                    }
                    break;

                }
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
                    WifiP2pDevice wifiP2pDevice =
                            (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                    if (wifiP2pDevice == null)return;
                    StringBuilder info = new StringBuilder();
                    info.append("裝置名稱： ").append(wifiP2pDevice.deviceName).append("\n");
                    info.append("裝置地址： ").append(wifiP2pDevice.deviceAddress).append("\n");
                    info.append("裝置狀態： ").append(getDeviceStatus(wifiP2pDevice.status)).append("\n");
                    tvInfo.setText(info);

                    break;
                }
            }
        }
    };
    private String getDeviceStatus(int deviceStatus){
        switch (deviceStatus){
            case WifiP2pDevice.AVAILABLE:
                return "可用";
            case WifiP2pDevice.INVITED:
                return "邀請中";
            case WifiP2pDevice.CONNECTED:
                return "已連線";
            case WifiP2pDevice.FAILED:
                return "失敗";
            default:
                return "未知";
        }
    }


    @Override
    public void onChannelDisconnected() {

    }
}