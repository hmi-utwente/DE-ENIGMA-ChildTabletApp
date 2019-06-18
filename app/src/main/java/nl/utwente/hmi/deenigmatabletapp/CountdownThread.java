package nl.utwente.hmi.deenigmatabletapp;

import android.widget.TextView;

/**
 * Created by davisond on 10/03/17.
 */


public class CountdownThread extends Thread {

    private TextView countdownDisplay;
    private String timer;

    public CountdownThread(TextView countdownDisplay, int timer){
        this.countdownDisplay = countdownDisplay;
        this.timer = ""+timer;
    }

    @Override
    public void run() {
        super.run();
        countdownDisplay.setText(timer);
    }

}
