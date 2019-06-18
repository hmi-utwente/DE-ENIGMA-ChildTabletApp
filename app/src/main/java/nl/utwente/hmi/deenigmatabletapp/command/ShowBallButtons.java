package nl.utwente.hmi.deenigmatabletapp.command;

public class ShowBallButtons implements Command {


	private String id;
	private boolean multipleSelect;
	private boolean rank;

	private String description;
	private String buttonText;


	public ShowBallButtons(String id, boolean multipleSelect, boolean rank, String description, String buttonText) {
		setId(id);
		setMultipleSelect(multipleSelect);
		setRank(rank);
		setDescription(parseBoldText(description));
		setButtonText(buttonText);
	}

	public String parseBoldText(String text){
		String ret = text;
		ret = ret.replace("_BOLD_","<b>");
		ret = ret.replace("_/BOLD_","</b>");
		return ret;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isMultipleSelect() {
		return multipleSelect;
	}

	public void setMultipleSelect(boolean multipleSelect) {
		this.multipleSelect = multipleSelect;
	}

	public String getButtonText() {
		return buttonText;
	}

	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public boolean isRank() {
		return rank;
	}

	public void setRank(boolean rank) {
		this.rank = rank;
	}
}
