package nl.utwente.hmi.deenigmachildtabletapp.command;

import java.util.List;
import java.util.Map;

public class ShowButtons implements Command {

	private String text;
	private List<TextButton> buttons;

	public ShowButtons(String text, List<TextButton> buttons) {
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

	public List<TextButton> getButtons(){
		return buttons;
	}
	
	public void setButtons(List<TextButton> buttons) {
		this.buttons = buttons;
	}

	
}
