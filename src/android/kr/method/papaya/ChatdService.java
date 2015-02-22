package kr.method.papaya;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.cordova.LOG;
import org.java_websocket.drafts.Draft_17;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class ChatdService extends Service {
	private static final String TAG = "ChatdService";
	private static final int REBOOT_DELAY_TIMER = 5 * 1000;
	
	ChatdWebsocketClient websocket;
	ChatdPlugin chatdPlugin;
	
	public class LocalBinder extends Binder {
		ChatdService getService() {
            return ChatdService.this;
        }
    }
	private final IBinder mBinder = new LocalBinder();
	/**
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("ChatdService", "onBind()");
		return mBinder;
	}
	
	/**
	 * 서비스가 만들어졌을 때
	 */
	@Override
	public void onCreate() {LOG.d("ChatdService", "onCreate");
		unregisterRestartAlarm();
		connect(getMetadata("kr.method.papaya.chatd.URL"));
		super.onCreate();
	}
	
	/**
	 * 서비스가 종료될 때
	 */
	@Override
	public void onDestroy() {LOG.d("ChatdService", "onDestroy");
		registerRestartAlarm();
		disconnect();
		super.onDestroy();
	}
	
	/**
	 * 서비스를 시작한다면
	 */
	@Override
	public void onStart(Intent intent, int startId){LOG.d("ChatdService", "onStart");
		super.onStart(intent, startId);
	}
	
	/**
	 * 서비스가 죽었을 때 다시 살리도록 알람설정
	 */
	private void registerRestartAlarm(){
		Intent intent = new Intent(ChatdService.this, RestartService.class);
		intent.setAction(RestartService.ACTION_RESTART_CHATDSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(ChatdService.this, 0, intent, 0);
		
		long firstTime = SystemClock.elapsedRealtime();
		firstTime += REBOOT_DELAY_TIMER;
		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,REBOOT_DELAY_TIMER, sender);
	}

	/**
	 * 알람설정 해제
	 */
	private void unregisterRestartAlarm(){
		Intent intent = new Intent(ChatdService.this, RestartService.class);
		intent.setAction(RestartService.ACTION_RESTART_CHATDSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(ChatdService.this, 0, intent, 0);
		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}
	
	public void onMessage(int action,String message){
		switch(action){
		case ChatdWebsocketClient.OPEN:
			
		break;
		case ChatdWebsocketClient.CLOSE:
			
		break;
		case ChatdWebsocketClient.MESSAGE:
			
		break;
		case ChatdWebsocketClient.ERROR:
			
		break;
		}
	}
	
	public void bindPlugin(ChatdPlugin chatdPlugin){
		this.chatdPlugin = chatdPlugin;
	}
	
	public void unbindPlugin(){
		this.chatdPlugin = null;
	}
	
	/**
	 * 웹소켓 프로토콜로 Papaya chatd 서버에 접속한다.
	 * @param url 웹소켓 접속서버 경로
	 * @param options 웹소켓 접속 설정
	 */
	private void connect(String url){
		if(websocket!=null){disconnect();}
		try {
			websocket =  new ChatdWebsocketClient(new URI(url), new Draft_17(), new HashMap<String, String>(), this);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String message){
		if(websocket!=null){
			websocket.send(message);
		}
	}
	
	/**
	 * Papaya chatd 서버 접속 해제한다.
	 * @param url 웹소켓 접속서버 경로
	 * @param options 웹소켓 접속 설정
	 */
	private void disconnect(){
		if(websocket!=null){
			websocket.close();
			websocket = null;
		}
	}

    public String getMetadata(final String key) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if(ai == null){
                return null;
            }else if(ai.metaData == null){
                return null;
            }else{
                return ai.metaData.getString(key);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
