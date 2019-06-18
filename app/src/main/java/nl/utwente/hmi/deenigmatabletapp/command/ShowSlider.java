package nl.utwente.hmi.deenigmatabletapp.command;

public class ShowSlider implements Command {

	private String id;
	private String text;
	private String buttonText;
	private String orientation;
	private String lowText;
	private String highText;
	private int steps;
	private int defaultStep;

	public ShowSlider(String id, String text, String buttonText, String orientation, String lowText, String highText, int steps, int defaultStep) {
		this.id = id;
		this.text = text;
		this.buttonText = buttonText;

		//set default value
		if(!"horizontal".equals(orientation) && !"vertical".equals(orientation)) {
			this.orientation = "vertical";
		} else {
			this.orientation = orientation;
		}

		this.lowText = lowText;
		this.highText = highText;
		this.steps = steps;
		this.defaultStep = defaultStep;
	}

	private void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}
	
	public String getButtonText(){
		return buttonText;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}


	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public String getLowText() {
		return lowText;
	}

	public void setLowText(String lowText) {
		this.lowText = lowText;
	}

	public String getHighText() {
		return highText;
	}

	public void setHighText(String highText) {
		this.highText = highText;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public int getDefaultStep() {
		return defaultStep;
	}

	public void setDefaultStep(int defaultStep) {
		this.defaultStep = defaultStep;
	}
}
