package nl.utwente.hmi.tabletmindsetapp.command;

public class ShowCountdown implements Command {


	private String id;
	private String description;
	private int time;

	public ShowCountdown(String id, String description, int time) {
		setId(id);
		setDescription(description);
		setTime(time);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
