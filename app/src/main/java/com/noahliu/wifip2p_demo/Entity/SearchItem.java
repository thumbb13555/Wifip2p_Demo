package com.noahliu.wifip2p_demo.Entity;

import android.net.wifi.p2p.WifiP2pDevice;

public class SearchItem {

    private String title;
    private String deviceAddress;
    private int status;
    private WifiP2pDevice p2pDevice;

    public SearchItem(String title, String deviceAddress, int status, WifiP2pDevice p2pDevice) {
        this.title = title;
        this.deviceAddress = deviceAddress;
        this.status = status;
        this.p2pDevice = p2pDevice;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public WifiP2pDevice getP2pDevice() {
        return p2pDevice;
    }

    public void setP2pDevice(WifiP2pDevice p2pDevice) {
        this.p2pDevice = p2pDevice;
    }

    public static String getDeviceStatus(int deviceStatus){
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
}
