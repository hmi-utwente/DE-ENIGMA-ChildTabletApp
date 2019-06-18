package nl.utwente.hmi.deenigmatabletapp.command;

import java.util.List;

public class ShowImageButtonGrid implements Command {

	private String text;
	private List<ImgButton> buttons;

	public ShowImageButtonGrid(String text, List<ImgButton> buttons) {
		this.setText(text);
		this.setButtons(buttons);
	}

	public String getId(){
		return "NOID";
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}

	public List<ImgButton> getButtons(){
		return buttons;
	}
	
	public void setButtons(List<ImgButton> buttons) {
		this.buttons = buttons;
	}

}
