package kr.method.papaya;

import org.apache.cordova.LOG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartService extends BroadcastReceiver {
	public static final String ACTION_RESTART_CHATDSERVICE  = "ACTION.RESTART.ChatdService";
	
	@Override
	public void onReceive(Context context, Intent intent) {LOG.d("RestartService", "onReceive");
		//서비스 죽일때 알람으로 다시 서비스 등록
		if (intent.getAction().equals(ACTION_RESTART_CHATDSERVICE)) {
			Intent i = new Intent(context, ChatdService.class);
			context.startService(i);
		}
		
		//폰 재부팅할때 서비스 등록
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, ChatdService.class);
			context.startService(i);
		}
	}

}
