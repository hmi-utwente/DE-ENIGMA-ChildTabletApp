package nl.utwente.hmi.tabletmindsetapp.command;

public class ShowAssignment implements Command {

	private String id;
	private String text;
	private String buttonText;
	private String imageFile;
	private boolean showHelp;

	public ShowAssignment(String id, String text, String imageFile, String buttonText, boolean showHelp) {
		this.setId(id);
		this.setText(text);
		this.setImageFile(imageFile);
		this.setButtonText(buttonText);
		this.setShowHelp(showHelp);
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

	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}


	public boolean isShowHelp() {
		return showHelp;
	}

	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}
}
