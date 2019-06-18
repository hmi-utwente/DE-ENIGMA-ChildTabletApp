package nl.utwente.hmi.deenigmatabletapp.command;

import java.util.Map;

public class ShowClickablePicture implements Command {

	private String id;
	private String text;
	private String imageFile;
	private String imageFileDisabled;
	private Map<String, String> clickableAreas;

	public ShowClickablePicture(String id, String text, String imageFile, String imageFileDisabled, Map<String, String> clickableAreas) {
		this.setId(id);
		this.setImageFile(imageFile);
		this.setImageFileDisabled(imageFileDisabled);
		this.setClickableAreas(clickableAreas);
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

	public void setText(String text){
		this.text = text;
	}

	public String getImageFile() {
		return imageFile;
	}

	public void setImageFileDisabled(String imageFileDisabled) {
		this.imageFileDisabled = imageFileDisabled;
	}

	public String getImageFileDisabled() {
		return imageFileDisabled;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	public Map<String, String> getClickableAreas(){
		return clickableAreas;
	}

	public void setClickableAreas(Map<String, String> clickableAreas) {
		this.clickableAreas = clickableAreas;
	}

}
