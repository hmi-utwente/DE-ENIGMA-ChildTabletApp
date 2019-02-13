package nl.utwente.hmi.deenigmachildtabletapp.command;


import android.graphics.Color;

public class TextButton {
    private final String id;
    private final String text;
    private final String color;

    public TextButton(String id, String text, String color){
        this.id = id;
        this.text = text;
        this.color = color;
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
}