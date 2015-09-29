package com.example.traffic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

/*
 * 问题点：
 * 1.如果服务被强制关闭，则不能统计
 * 2.开机自动启动服务
 * 3.如果要做某天的流量统计，则需要定时在每晚十二点前进行一次已用流量统计
 * 4.数据库相关操作比较耗时，要考虑异步
 * 
 */
public class TrafficService extends Service {

	private static final String TAG = TrafficService.class.getSimpleName();

	private TrafficReceiver tReceiver;
	private ConnectivityManager connManager;
	private DbManager dbManager;

	private MyBinder binder = new MyBinder();

	public class MyBinder extends Binder {
		TrafficService getService() {
			return TrafficService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return binder;
	}

	public void onCreate() {
		Log.d(TAG, "onCreate");
		// 获得数据库连接服务
		dbManager = new DbManager(this);
		// 获得网络连接服务
		connManager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 注册TrafficReceiver
		tReceiver = new TrafficReceiver();
		IntentFilter filter = new IntentFilter();
		// filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(tReceiver, filter);
		super.onCreate();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		unregisterReceiver(tReceiver);
		logRecord();
		dbManager.close();
		super.onDestroy();
	}

	public void logRecord() {
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		String networkType = null;
		if (networkInfo == null) {
			networkType = null;
		} else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			networkType = DbConstants.NETWORK_TYPE_WIFI;
		} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			networkType = DbConstants.NETWORK_TYPE_MOBILE;
		}

		List<PackageInfo> packinfos = getPackageManager().getInstalledPackages(
				PackageManager.GET_UNINSTALLED_PACKAGES
						| PackageManager.GET_PERMISSIONS);
		for (PackageInfo info : packinfos) {
			String[] premissions = info.requestedPermissions;
			if (premissions != null && premissions.length > 0) {
				for (String premission : premissions) {
					if ("android.permission.INTERNET".equals(premission)) {
						int uid = info.applicationInfo.uid;
						long rx = TrafficStats.getUidRxBytes(uid);
						long tx = TrafficStats.getUidTxBytes(uid);
						dbManager.updateEnd(info.packageName,
								System.currentTimeMillis(), rx, tx);
						dbManager
								.insertStart(
										info.packageName,
										info.applicationInfo.loadLabel(
												getPackageManager()).toString(),
										System.currentTimeMillis(),
										networkType, rx, tx);
					}
				}
			}
		}
	}

	private class TrafficReceiver extends BroadcastReceiver {
		private final String TAG = TrafficReceiver.class.getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "网络状态改变");
			logRecord();
		}

	}

}
