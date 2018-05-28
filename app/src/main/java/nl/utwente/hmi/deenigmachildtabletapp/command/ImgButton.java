package nl.utwente.hmi.deenigmachildtabletapp.command;


public class ImgButton {
    private final String id;
    private final String text;
    private final String img;

    public ImgButton(String id, String text, String img){
        this.id = id;
        this.text = text;
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getImg() {
        return img;
    }
}