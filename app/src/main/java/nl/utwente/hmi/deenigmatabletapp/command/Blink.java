package nl.utwente.hmi.deenigmatabletapp.command;

import java.util.List;

public class Blink {

    private final int blinkDuration;
    private final int totalDuration;
    private final List<String> colors;

    public Blink(int blinkDuration, int totalDuration, List<String> colors){
        this.blinkDuration = blinkDuration;
        this.totalDuration = totalDuration;
        this.colors = colors;
    }

    public int getBlinkDuration() {
        return blinkDuration;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public List<String> getColors() {
        return colors;
    }

}
