package kr.method.papaya;

import org.apache.cordova.LOG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class ChatdService extends Service {
	private static final String TAG = "ChatdService";
	private static final int REBOOT_DELAY_TIMER = 5 * 1000;
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("ChatdService", "onBind()");
		return null;
	}
	
	@Override
	public void onCreate() {LOG.d("ChatdService", "onCreate");
		unregisterRestartAlarm();
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {LOG.d("ChatdService", "onDestroy");
		registerRestartAlarm();
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId){LOG.d("ChatdService", "onStart");
		super.onStart(intent, startId);
	}
	
	private void registerRestartAlarm(){
		Intent intent = new Intent(ChatdService.this, RestartService.class);
		intent.setAction(RestartService.ACTION_RESTART_ChatdService);
		PendingIntent sender = PendingIntent.getBroadcast(ChatdService.this, 0, intent, 0);
		
		long firstTime = SystemClock.elapsedRealtime();
		firstTime += REBOOT_DELAY_TIMER;
		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,REBOOT_DELAY_TIMER, sender);
	}

	private void unregisterRestartAlarm(){
		Intent intent = new Intent(ChatdService.this, RestartService.class);
		intent.setAction(RestartService.ACTION_RESTART_ChatdService);
		PendingIntent sender = PendingIntent.getBroadcast(ChatdService.this, 0, intent, 0);
		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}
}
