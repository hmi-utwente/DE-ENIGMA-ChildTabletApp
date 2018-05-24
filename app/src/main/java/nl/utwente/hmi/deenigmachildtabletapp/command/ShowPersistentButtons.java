package nl.utwente.hmi.deenigmachildtabletapp.command;

import java.util.Map;

public class ShowPersistentButtons implements Command {

	private Map<String, String> buttons;

	public ShowPersistentButtons(Map<String, String> buttons) {
		this.setButtons(buttons);
	}

	public String getId(){
		return "NOID";
	}

	public Map<String, String> getButtons(){
		return buttons;
	}
	
	public void setButtons(Map<String, String> buttons) {
		this.buttons = buttons;
	}

	
}
