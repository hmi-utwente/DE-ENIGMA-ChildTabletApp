package nl.utwente.hmi.deenigmatabletapp.command;

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
