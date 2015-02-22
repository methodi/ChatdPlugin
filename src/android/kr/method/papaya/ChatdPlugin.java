package kr.method.papaya;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ChatdPlugin extends CordovaPlugin {
	private final String START_SERVICE = "start";
	private final String STOP_SERVICE = "stop";
	private BroadcastReceiver receiver;
	private Intent intentMyService;
    private Context context;

	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		//서비스 시작
		if (action.equals(START_SERVICE)) {
			this.startService();
		//서비스 종료
		} else if (action.equals(STOP_SERVICE)){
			this.stopService();
		}
		
		return false;
	}
	
	@Override
	public void onDestroy(){LOG.d("ChatdPlugin", "OnDestroy");
		if(context!=null && receiver!=null){
			context.unregisterReceiver(receiver);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 서비스 종료
	 */
	private void stopService(){
		
	}
}
