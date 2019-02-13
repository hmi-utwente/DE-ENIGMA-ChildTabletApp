package nl.utwente.hmi.deenigmachildtabletapp.command;

import java.util.Map;

public class ShowButtons implements Command {

	private String text;
	private Map<String, TextButton> buttons;

	public ShowButtons(String text, Map<String, TextButton> buttons) {
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

	public Map<String, TextButton> getButtons(){
		return buttons;
	}
	
	public void setButtons(Map<String, TextButton> buttons) {
		this.buttons = buttons;
	}

	
}
