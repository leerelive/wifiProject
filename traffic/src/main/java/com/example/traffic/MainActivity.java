package com.example.traffic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.example.trafficstats.R;

import java.util.Map;

public class MainActivity extends Activity {
	private TextView txtView;

	private TrafficService trafficService;
	private DbManager dbManager;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			trafficService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			trafficService = ((TrafficService.MyBinder) service).getService();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtView = (TextView) findViewById(R.id.textView1);

		Intent intent = new Intent(MainActivity.this, TrafficService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		dbManager = new DbManager(this);
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (trafficService == null) {
					txtView.setText("服务未绑定");
				} else {
					trafficService.logRecord();
					Map<String, TrafficInfo> list = dbManager.queryTotal();
					StringBuilder sb = new StringBuilder();
					for (TrafficInfo info : list.values()) {
						sb.append(info.appName + " - 流量信息:\r\n");
						sb.append(
								"移动网络接收的流量"
										+ Formatter.formatFileSize(
												MainActivity.this,
												info.mobileRx)).append("\r\n");
						sb.append(
								"移动网络发送的流量"
										+ Formatter.formatFileSize(
												MainActivity.this,
												info.mobileTx)).append("\r\n");
						sb.append(
								"WIFI接收的流量"
										+ Formatter.formatFileSize(
												MainActivity.this, info.wifiRx))
								.append("\r\n");
						sb.append(
								"WIFI发送的流量"
										+ Formatter.formatFileSize(
												MainActivity.this, info.wifiTx))
								.append("\r\n");
						sb.append("--------------------").append("\r\n");
						txtView.setText(sb);
					}
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

}
