package kr.method.papaya;

import kr.method.papaya.ChatdService.LocalBinder;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

public class ChatdPlugin extends CordovaPlugin {
	private final String START_SERVICE = "start";
	private final String STOP_SERVICE = "stop";
	private final String CHATD_CONNECT = "connect";
	private final String CHATD_SEND = "send";
	private BroadcastReceiver receiver;
	private Intent intentMyService;
    private Context context;
    private ChatdService mBoundService;
    private CallbackContext callbackContext;

	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		//서비스 시작
		if(action.equals(START_SERVICE)) {
			this.startService();
		//서비스 종료
		}else if (action.equals(STOP_SERVICE)){
			this.stopService();
		}else if (action.equals(CHATD_CONNECT)){
			this.callbackContext = callbackContext;
			String id = args.getString(0);
			String password = args.getString(1);
			String deviceId = args.getString(2);
			this.connect(id,password,deviceId);
		}else if (action.equals(CHATD_SEND)){
			String message = args.getString(0);
			this.send(message);
		}
		
		return false;
	}
	
	@Override
	public void onDestroy(){LOG.d("ChatdPlugin", "OnDestroy");
		if(context!=null && receiver!=null){
			context.unregisterReceiver(receiver);
		}
		if(mBoundService!=null){
			mBoundService.unbindPlugin();
		}
		super.onDestroy();
	}
	
	/**
	 * 서비스 시작
	 */
	private void startService(){
		LOG.d("ChatdPlugin", "startService");
		context = cordova.getActivity().getApplicationContext();
		intentMyService = new Intent(context, ChatdService.class);
		receiver = new RestartService();
		
		try 
        {
            IntentFilter mainFilter = new IntentFilter("kr.method.papaya.USER_ACTION");
            
            context.registerReceiver(receiver, mainFilter);
            context.startService(intentMyService);
            context.bindService(intentMyService, mConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 서비스 종료
	 */
	private void stopService(){
		
	}
	
	public void connect(String id,String password,String deviceId){
		if(mBoundService!=null){
			mBoundService.connect(id,password,deviceId);
		}
	}
	
	public void send(String message){
		if(mBoundService!=null){
			mBoundService.send(message);
		}
	}
	
	public void onMessage(int action,String message){
		JSONObject event = new JSONObject();
		switch(action){
		case ChatdWebsocketClient.OPEN:
			try {
				event.put("type","open");
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, event);
			    pluginResult.setKeepCallback(true);
				callbackContext.sendPluginResult(pluginResult);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		break;
		case ChatdWebsocketClient.CLOSE:
			try {
				event.put("type","close");
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, event);
			    pluginResult.setKeepCallback(true);
				callbackContext.sendPluginResult(pluginResult);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		break;
		case ChatdWebsocketClient.MESSAGE:
			try {
				event.put("type","message");
				event.put("data",message);
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, event);
			    pluginResult.setKeepCallback(true);
				callbackContext.sendPluginResult(pluginResult);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		break;
		case ChatdWebsocketClient.ERROR:
			try {
				event.put("type","error");
				PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, event);
			    pluginResult.setKeepCallback(true);
				callbackContext.sendPluginResult(pluginResult);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		break;
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	LocalBinder binder = (LocalBinder) service;
        	ChatdPlugin.this.mBoundService = binder.getService();
        	ChatdPlugin.this.mBoundService.bindPlugin(ChatdPlugin.this);
        }

        public void onServiceDisconnected(ComponentName className) {
        	ChatdPlugin.this.mBoundService.unbindPlugin();
        	ChatdPlugin.this.mBoundService = null;
        }
    };
}
