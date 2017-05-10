package nl.utwente.hmi.tabletmindsetapp.command;

public class ShowTimer implements Command {


	private String id;
	private boolean reset;

	private String description;
	private String continueButtonText;
	private String redoButtonText;

	//TODO: these should actually be some kind of Timer(String, Int) object I guess.. can't be bothered at the moment -- needs to be done quick
	private String timer1Description;
	private int timer1Time;
	private String timer2Description;
	private int timer2Time;

	public ShowTimer(String id, boolean reset, String description, String redoButtonText, String continueButtonText, String timer1Descr, int timer1Time, String timer2Descr, int timer2Time) {
		setId(id);
		setReset(reset);
		setDescription(description);
		setRedoButtonText(redoButtonText);
		setContinueButtonText(continueButtonText);
		setTimer1Description(timer1Descr);
		setTimer1Time(timer1Time);
		setTimer2Description(timer2Descr);
		setTimer2Time(timer2Time);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isReset() {
		return reset;
	}

	public void setReset(boolean reset) {
		this.reset = reset;
	}

	public String getContinueButtonText() {
		return continueButtonText;
	}

	public void setContinueButtonText(String buttonText) {
		this.continueButtonText = buttonText;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTimer1Description() {
		return timer1Description;
	}

	public void setTimer1Description(String timer1Description) {
		this.timer1Description = timer1Description;
	}

	public int getTimer1Time() {
		return timer1Time;
	}

	public void setTimer1Time(int timer1Time) {
		this.timer1Time = timer1Time;
	}

	public String getTimer2Description() {
		return timer2Description;
	}

	public void setTimer2Description(String timer2Description) {
		this.timer2Description = timer2Description;
	}

	public int getTimer2Time() {
		return timer2Time;
	}

	public void setTimer2Time(int timer2Time) {
		this.timer2Time = timer2Time;
	}

	public String getRedoButtonText() {
		return redoButtonText;
	}

	public void setRedoButtonText(String redoButtonText) {
		this.redoButtonText = redoButtonText;
	}
}
