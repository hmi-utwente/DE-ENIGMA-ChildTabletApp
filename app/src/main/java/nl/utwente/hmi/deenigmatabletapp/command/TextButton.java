package nl.utwente.hmi.deenigmatabletapp.command;


public class TextButton {
    private final String id;
    private final String text;
    private final String color;
    private Blink blink;

    public TextButton(String id, String text, String color, Blink blink){
        this.id = id;
        this.text = text;
        this.color = color;
        this.blink = blink;
    }

    public TextButton(String id, String text){
        this.id = id;
        this.text = text;
        this.color = "lightgrey";
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getColor() {
        return color;
    }

    public Blink getBlink() {return blink;}
}