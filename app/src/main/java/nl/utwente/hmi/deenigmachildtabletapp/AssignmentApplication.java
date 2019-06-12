package nl.utwente.hmi.deenigmachildtabletapp;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.stomp.STOMPMiddleware;

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

		//allow network communication (stomp) in main thread
		//TODO: move stomp communication to separate thread
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		//StrictMode.setThreadPolicy(policy);

		//TODO: I couldnt get the dynamic loading through GenericMiddlewareLoader to work (it gave NoClassDefFound errors) so we load the STOMPMIddleware here directly instead :(
		//TODO: make the ip and port configurable from within the app
		//TODO: we actually need some kind of reconnection-daemon, if the connection fails for some reason... however this will probably require quite a bit of redesign
		//load and initiate the middleware we use for communication throughout this app
		//for our mindset experiments the PC is given static ip 192.168.0.10
		// IP Pauline's computer: 192.168.1.62
		// IP snozzle computer : 192.168.1.132
		//middleware = new STOMPMiddleware("192.168.0.22",61613,"/topic/adult_tablet.command","/topic/adult_tablet.feedback");
		//middleware = new STOMPMiddleware("192.168.0.22",61613,"/topic/child_tablet.command","/topic/child_tablet.feedback");
		//middleware = new ROSMiddleware("ws://192.168.0.22:9090","/child_tablet_feedback","/child_tablet_command");
		//middleware = new ROSMiddleware("ws://192.168.0.22:9090","/adult_tablet_feedback","/adult_tablet_command");

	}

}
