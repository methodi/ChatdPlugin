package kr.method.papaya;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

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
	private final String CHATD_SEND = "send";
	private BroadcastReceiver receiver;
	private Intent intentMyService;
    private Context context;
    private ChatdService mBoundService;

	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		//서비스 시작
		if (action.equals(START_SERVICE)) {
			this.startService();
		//서비스 종료
		} else if (action.equals(STOP_SERVICE)){
			this.stopService();
		} else if (action.equals(CHATD_SEND)){
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
            //context.startService(intentMyService);
            context.bindService(intentMyService, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 서비스 종료
	 */
	private void stopService(){
		
	}
	
	public void send(String message){
		if(mBoundService!=null){
			mBoundService.send(message);
		}
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
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((ChatdService.LocalBinder)service).getService();
            mBoundService.bindPlugin(ChatdPlugin.this);
        }

        public void onServiceDisconnected(ComponentName className) {
        	mBoundService.unbindPlugin();
            mBoundService = null;
        }
    };
}
