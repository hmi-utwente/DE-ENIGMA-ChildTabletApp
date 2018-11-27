package nl.utwente.hmi.deenigmachildtabletapp.command;

import java.util.Map;

public class SetBackgroundColor implements Command {

	private String color;

	public SetBackgroundColor(String color) {
		this.setColor(color);
	}

	public String getId() {
		return "NOID";
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
