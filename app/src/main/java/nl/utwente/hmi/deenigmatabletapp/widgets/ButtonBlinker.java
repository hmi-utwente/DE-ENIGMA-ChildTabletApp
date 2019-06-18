package nl.utwente.hmi.deenigmatabletapp.widgets;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.widget.Button;

import java.util.List;

public class ButtonBlinker implements Runnable {

    private final Button button;
    private final int blinkDuration;
    private final int totalDuration;
    private final List<ColorStateList> colors;
    private final ColorStateList originalColor;
    private Handler handler;

    private int currentColorIndex;
    private long startTimeMillis;

    public ButtonBlinker(Button button, int blinkDuration, int totalDuration, ColorStateList originalColor, List<ColorStateList> colors){
        this.button = button;
        this.blinkDuration = blinkDuration;
        this.totalDuration = totalDuration;
        this.originalColor = originalColor;
        this.colors = colors;
        currentColorIndex = 0;
        this.handler = new Handler();
    }

    public void start(){
        this.startTimeMillis = System.currentTimeMillis();
        handler.postDelayed(this, blinkDuration/colors.size());
    }

    @Override
    public void run() {
        if(totalDuration > 0 && startTimeMillis + totalDuration < System.currentTimeMillis()){
            //Wrap it up boys, we're done!
            button.setBackgroundTintList(originalColor);
            return;
        }

        //set color in animation
        button.setBackgroundTintList(colors.get(currentColorIndex));

        //increase index, reset to 0 if out of bounds
        currentColorIndex++;
        if(currentColorIndex >= colors.size()){
            currentColorIndex = 0;
        }

        //plan the next refresh
        handler.postDelayed(this, blinkDuration/colors.size());
    }
}
