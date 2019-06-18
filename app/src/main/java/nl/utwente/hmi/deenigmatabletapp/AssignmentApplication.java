package nl.utwente.hmi.deenigmatabletapp;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AssignmentApplication extends Application {

	//private Middleware middleware;
	private PendingIntent intent;



	@Override
	public Context getApplicationContext() {
		return super.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		//this lets us restart after a serious crash
		intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT);

//		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
//			@Override
//			public void uncaughtException(Thread thread, Throwable ex) {
//				AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//				mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, intent);//give wifi some time to reconnect
//				System.exit(2);
//			}
//		});


	}

}
