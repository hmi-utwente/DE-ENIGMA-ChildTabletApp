package nl.utwente.hmi.deenigmachildtabletapp.command;

import java.util.Map;

public class ShowPersistentButtons implements Command {

	private Map<String, TextButton> buttons;

	public ShowPersistentButtons(Map<String, TextButton> buttons) {
		this.setButtons(buttons);
	}

	public String getId(){
		return "NOID";
	}

	public Map<String, TextButton> getButtons(){
		return buttons;
	}
	
	public void setButtons(Map<String, TextButton> buttons) {
		this.buttons = buttons;
	}

	
}
