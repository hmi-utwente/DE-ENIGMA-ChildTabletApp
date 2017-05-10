package nl.utwente.hmi.tabletmindsetapp.command;

import java.util.Map;
import java.util.Set;

public class ShowButtons implements Command {

	private String text;
	private Map<String, String> buttons;

	public ShowButtons(String text, Map<String, String> buttons) {
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

	public Map<String, String> getButtons(){
		return buttons;
	}
	
	public void setButtons(Map<String, String> buttons) {
		this.buttons = buttons;
	}

	
}
