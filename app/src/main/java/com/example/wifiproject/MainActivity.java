package com.example.wifiproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;




public class MainActivity extends Activity {
    private final String TAG = "WifiSoftAP";
    public static final String WIFI_AP_STATE_CHANGED_ACTION =
            "android.net.wifi.WIFI_AP_STATE_CHANGED";

    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;


    TextView result;
    TextView testInfo;
    WifiManager wifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    //网络连接列表
    private List<WifiConfiguration> wifiConfiguration;
    StringBuilder resultList = new StringBuilder();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setTitle("");
        result = (TextView) findViewById(R.id.result);
        testInfo = (TextView) findViewById(R.id.testInfo);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        Button btnOpenAP = (Button)this.findViewById(R.id.btnOpenAP);
        btnOpenAP.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final EditText SSIDText = (EditText)findViewById(R.id.SSID);
                final EditText wifiPWDText = (EditText)findViewById(R.id.wifiPWD);
                String wifiPWD = wifiPWDText.getText().toString();
                String SSID = SSIDText.getText().toString();
                testInfo.setText(wifiPWD);
                if (!isApEnabled()){
                    setWifiApEnabled(true, SSID, wifiPWD);
                  //  result.setText("开启wifi成功！");
                }
            }
        });


        Button btnCloseAP = (Button)this.findViewById(R.id.btnCloseAP);
        btnCloseAP.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (isApEnabled()){
                    result.append("已打开！");
                    final EditText SSIDText = (EditText)findViewById(R.id.SSID);
                    final EditText wifiPWDText = (EditText)findViewById(R.id.wifiPWD);
                    String wifiPWD = wifiPWDText.getText().toString();
                    String SSID = SSIDText.getText().toString();
                    setWifiApEnabled(false, wifiPWD, SSID);
                }else{
                    result.append("未打开！");
                }
            }
        });


        Button btnScan = (Button)this.findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                StartScan();
            }
        });


        Button btnConnectAP = (Button)this.findViewById(R.id.btnConnectAP);
        btnConnectAP.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                connectAP();
            }
        });


        Button btnGetConnectedIP = (Button)this.findViewById(R.id.btnGetConnectedIP);
        btnGetConnectedIP.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ArrayList<String> connectedIP = getConnectedIP();
                resultList = new StringBuilder();
                for(String ip : connectedIP){
                    resultList.append(ip);
                    resultList.append("\n");
                }
                result.setText(resultList);

            }
        });



		/* 获取原先便携式热点的配置信息
		try {
			Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
			WifiConfiguration apConfig = (WifiConfiguration) method.invoke(wifiManager);

			Log.v(TAG, "allowedAuthAlgorithms is " + apConfig.allowedAuthAlgorithms.toString());
			Log.v(TAG, "allowedProtocols is " + apConfig.allowedProtocols.toString());
			Log.v(TAG, "allowedGroupCiphers is " + apConfig.allowedGroupCiphers.toString());
			Log.v(TAG, "allowedKeyManagement is " + apConfig.allowedKeyManagement.toString());
			Log.v(TAG, "allowedPairwiseCiphers is " + apConfig.allowedPairwiseCiphers.toString());
			Log.v(TAG, "BSSID is " + apConfig.BSSID);
			Log.v(TAG, "SSID is " + apConfig.SSID);
			Log.v(TAG, "hiddenSSID is " + apConfig.hiddenSSID);
			Log.v(TAG, "networkId is " + apConfig.networkId);
			Log.v(TAG, "preSharedKey is " + apConfig.preSharedKey);
			Log.v(TAG, "priority is " + apConfig.priority);
			Log.v(TAG, "status is " + apConfig.status);
			Log.v(TAG, "wepKeys is " + apConfig.wepKeys);
			Log.v(TAG, "wepTxKeyIndex is " + apConfig.wepTxKeyIndex);

		} catch(Exception e) {
			Log.e(TAG, "Cannot get WiFi AP Configuration", e);
		}*/

    }

/*	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		wifiManager.startScan();
		result.setText("Starting Scan");
		return super.onMenuItemSelected(featureId, item);
	}*/

    protected void onPause() {
        if (receiverWifi != null)
            unregisterReceiver(receiverWifi);

        super.onPause();
    }

    protected void onResume() {
        if (receiverWifi != null)
            registerReceiver(receiverWifi, new IntentFilter(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        super.onResume();
    }


    public void StartScan() {
        //打开wifi
        wifiManager.setWifiEnabled(true);

        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        result.setText("\nScaning...\n");
    }


    public boolean setWifiApEnabled(boolean enabled, String SSID, String PWD) {
            try {
                WifiConfiguration apConfig = new WifiConfiguration();
                apConfig.SSID = SSID;
                apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                apConfig.preSharedKey = PWD;

                if (isHtcPhone()) {
                    setHTCSSID(apConfig);
                }

                Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
               //s method.invoke(wifiManager, apConfig, false);


                if (enabled) {
                    result.setText("打开");
                } else {
                    result.setText("关闭");
                }

                return (Boolean) method.invoke(wifiManager, apConfig, enabled);


            } catch (Exception e) {
                Log.e(TAG, "Cannot set WiFi AP state", e);
                return false;
            }
        }






//根据mWifiApProfile field是否存在判断手机是否为HTC，从而特殊处理
    public boolean isHtcPhone(){
        boolean isHtcFlag = false;
        try {
            isHtcFlag = WifiConfiguration.class
                    .getDeclaredField("mWifiApProfile") != null;
        } catch (java.lang.NoSuchFieldException e) {
            isHtcFlag = false;
        }
        return isHtcFlag;
    }
//对于HTC手机的SSID设置需要特殊处理
    public void setHTCSSID(WifiConfiguration config) {
        try {
            Field mWifiApProfileField = WifiConfiguration.class
                    .getDeclaredField("mWifiApProfile");
            mWifiApProfileField.setAccessible(true);
            Object hotSpotProfile = mWifiApProfileField.get(config);
            mWifiApProfileField.setAccessible(false);


            if (hotSpotProfile != null) {
                Field ssidField = hotSpotProfile.getClass().getDeclaredField(
                        "SSID");
                ssidField.setAccessible(true);
                ssidField.set(hotSpotProfile, config.SSID);
                ssidField.setAccessible(false);


                Field localField3 = hotSpotProfile.getClass().getDeclaredField(
                        "key");
                localField3.setAccessible(true);
                localField3.set(hotSpotProfile, config.preSharedKey);
                localField3.setAccessible(false);


                Field localField6 = hotSpotProfile.getClass().getDeclaredField(
                        "dhcpEnable");
                localField6.setAccessible(true);
                localField6.setInt(hotSpotProfile, 1);
                localField6.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }










    public int getWifiApState() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            return (Integer) method.invoke(wifiManager);
        } catch (Exception e) {
            Log.e(TAG, "Cannot get WiFi AP state", e);
            return WIFI_AP_STATE_FAILED;
        }
    }

    public boolean isApEnabled() {
        int state = getWifiApState();
        result.setText("state = " + state + "   ");
        return WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state;
    }


    //连接GossipDog
    public void connectAP() {
        WifiConfiguration gossipDog = new WifiConfiguration();
        for (WifiConfiguration ap : wifiConfiguration) {
            if (ap.SSID == "GossipDog") {
                gossipDog = ap;
            }
        }

        if (gossipDog != null) {
            gossipDog.preSharedKey = "abcdefgh";
            gossipDog.networkId = wifiManager.addNetwork(gossipDog);
            wifiManager.enableNetwork(gossipDog.networkId, true);
            result.setText("连接AP成功");
        }

    }


    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connectedIP;
    }

    class WifiReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            resultList = new StringBuilder();
            wifiList = wifiManager.getScanResults();
            for (int i = 0; i < wifiList.size(); i++) {
                resultList.append(new Integer(i + 1).toString() + ".");
                resultList.append((wifiList.get(i)).toString());
                resultList.append("\n\n");
            }
            result.setText(resultList);

            //得到配置好的网络连接
            wifiConfiguration = wifiManager.getConfiguredNetworks();
        }
    }

}
