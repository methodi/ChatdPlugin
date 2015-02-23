package kr.method.papaya;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.webkit.CookieManager;

public class ChatdService extends Service {
	private static final String TAG = "ChatdService";
	private static final int REBOOT_DELAY_TIMER = 10 * 1000;
	
	ChatdWebsocketClient websocket;
	ChatdPlugin chatdPlugin;
	
	public class LocalBinder extends Binder {
		public ChatdService getService() {
            return ChatdService.this;
        }
    }
	private final IBinder mBinder = new LocalBinder();
	
	  private static final Map<String, String> draftMap = new HashMap<String, String>();
	  static {
	    draftMap.put("draft10", "org.java_websocket.drafts.Draft_10");
	    draftMap.put("draft17", "org.java_websocket.drafts.Draft_17");
	    draftMap.put("draft75", "org.java_websocket.drafts.Draft_75");
	    draftMap.put("draft76", "org.java_websocket.drafts.Draft_76");
	  }
	  
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
		if(websocket==null){
			connect();
		}
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
			Log.d("ChatdService", "onMessage OPEN"+message);
		break;
		case ChatdWebsocketClient.CLOSE:
			Log.d("ChatdService", "onMessage CLOSE"+message);
		break;
		case ChatdWebsocketClient.MESSAGE:
			Log.d("ChatdService", "onMessage MESSAGE"+message);
		break;
		case ChatdWebsocketClient.ERROR:
			Log.d("ChatdService", "onMessage ERROR"+message);
		break;
		}
		if(chatdPlugin!=null){
			chatdPlugin.onMessage(action, message);
		}
	}
	
	public void bindPlugin(ChatdPlugin chatdPlugin){
		this.chatdPlugin = chatdPlugin;
	}
	
	public void unbindPlugin(){
		this.chatdPlugin = null;
	}
	
	public void connect(String id,String password,String deviceId){
		
	}
	
	/**
	 * 웹소켓 프로토콜로 Papaya chatd 서버에 접속한다.
	 */
	public void connect(){
		String url = getMetadata("kr.method.papaya.chatd.URL");
		LOG.d("ChatdService", url);
		if(websocket!=null){disconnect();}
		try {
			Map<String, String> headers = new HashMap<String, String>();
			CookieManager cookieManager = CookieManager.getInstance();
			URI uri = new URI(url);
			headers.put("cookie", cookieManager.getCookie(uri.getHost()));
			websocket =  new ChatdWebsocketClient(uri, getDraft(new JSONObject()), headers, this);
			websocket.connect();
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
	public void disconnect(){
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
    
    public Draft getDraft(JSONObject options) {

      String draftName;
      Draft draft = new Draft_17();
          
      try {
        draftName = options.getString("draft");
      } 
      catch (JSONException e1) {
        return draft;
      }
     
      if (draftName != null) {
        String draftClassName = draftMap.get(draftName);
        
        if (draftClassName != null) {
          try {
            Class<?> clazz = Class.forName(draftClassName);
            Constructor<?> ctor = clazz.getConstructor();
            draft = (Draft) ctor.newInstance();
          }
          catch (Exception e) {
            //callbackContext.error("Draft not found.");
          }
        }
      }
      
      return draft;
    }
}
