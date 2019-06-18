package nl.utwente.hmi.deenigmatabletapp.command;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A simple JSON message parser intended to parse and create the Command based on the provided JSON
 *
 * @author davisond
 *
 */
public class JSONCommandParser {

	public JSONCommandParser(){
	}

	/**
	 * Parse the given JSON to output an instantiated Command. Expects the following JSON format:
	 *
	 * {
	 * 		"showButtons" :
	 * 		{
	 * 			"text":"some explanation",
	 * 			"buttons":
	 * 				[
	 * 					{"id":"btnid","value":"btnvalue"},
	 * 					...,
	 * 					...
	 * 				]
	 * 		}
	 * }
	 *
	 * or
	 *
	 * {
	 *     "showAssignment":
	 *     {
	 *         "id":"assignmentid",
	 *         "text":"the text to show on the page",
	 *         "imageFile":"the image to show on the page"
	 *         "buttonText":"text shown on button"
	 *     }
	 * }
	 * or
	 *
	 * {
	 *     "showSlider":
	 *     {
	 *         "id":"sliderid",
	 *         "text":"the text to show on the page",
	 *         "buttonText":"text shown on button",
	 *         "orientation":"horizontal/vertical"
	 *         "lowText":"text shown at low end of slider"
	 *         "highText":"text shown at high end of slider"
	 *         "steps":"amount of steps to show in slider"
	 *         "default":"default selected step in slider"
	 *     }
	 * }
	 * @param jn the JsonNode structure
	 * @return an instantiated Command object
	 */
	public Command parseJSON(JsonNode jn) throws CommandParseException {
		Command retCommand = null;

		//try to parse either of two possible command types: showButtons or showAssignment
		if(!jn.path("showButtons").isMissingNode()){
			retCommand = parseShowButtons(jn.path("showButtons"));
		} else if(!jn.path("showPersistentButtons").isMissingNode()){
			retCommand = parseShowPersistentButtons(jn.path("showPersistentButtons"));
		} else if(!jn.path("showImageButtonGrid").isMissingNode()){
			retCommand = parseShowImageButtonGrid(jn.path("showImageButtonGrid"));
		} else if(!jn.path("showAssignment").isMissingNode()){
			retCommand = parseAssignment(jn.path("showAssignment"));
		} else if(!jn.path("showClickablePicture").isMissingNode()){
			retCommand = parseClickablePicture(jn.path("showClickablePicture"));
		} else if(!jn.path("setBackgroundColor").isMissingNode()){
			retCommand = parseBackgroundColor(jn.path("setBackgroundColor"));
		} else if(!jn.path("showTimer").isMissingNode()){
            retCommand = parseTimer(jn.path("showTimer"));
        } else if(!jn.path("showCountdown").isMissingNode()){
            retCommand = parseCountdown(jn.path("showCountdown"));
        } else if(!jn.path("showBallButtons").isMissingNode()){
            retCommand = parseBallButtons(jn.path("showBallButtons"));
        } else {
			throw new CommandParseException("Unable to parse command: "+jn.toString());
		}

		return retCommand;
	}



    /**
     * Parses the parseBallButtons command. Creates the Command if succesful, but throws an exception if the JSON format is incorrect
     * @param showBallButtons the JSON that contains the properties for this command
     * @return an instantiated Command object
     * @throws CommandParseException if the format does not match
     */
    private Command parseBallButtons(JsonNode showBallButtons) throws CommandParseException {
        if(!showBallButtons.path("id").isTextual()){
            throw new CommandParseException("showBallButtons command should have a 'id' property of type string");
        }
        String id = showBallButtons.get("id").asText();

        boolean multipleSelect = showBallButtons.path("multipleSelect").asBoolean(false);

		boolean rank = showBallButtons.path("rank").asBoolean(false);

        if(!showBallButtons.path("description").isTextual()){
            throw new CommandParseException("showBallButtons command should have a 'description' property of type string");
        }
        String description = showBallButtons.get("description").asText("Welke bal heb je neergelegd?");

        if(!showBallButtons.path("buttonText").isTextual()){
            throw new CommandParseException("showBallButtons command should have a 'buttonText' property of type string");
        }
        String buttonText = showBallButtons.get("buttonText").asText("Doorgaan");

        return new ShowBallButtons(id, multipleSelect, rank, description, buttonText);
    }


    /**
     * Parses the showTimer command. Creates the Command if succesful, but throws an exception if the JSON format is incorrect
     * @param showTimer the JSON that contains the properties for this command
     * @return an instantiated Command object
     * @throws CommandParseException if the format does not match
     */
    private Command parseTimer(JsonNode showTimer) throws CommandParseException {
        if(!showTimer.path("id").isTextual()){
            throw new CommandParseException("showTimer command should have a 'id' property of type string");
        }
        String id = showTimer.get("id").asText();

		boolean reset = showTimer.path("reset").asBoolean(true);

        if(!showTimer.path("description").isTextual()){
            throw new CommandParseException("showTimer command should have a 'description' property of type string");
        }
        String description = showTimer.get("description").asText("Stopwatch");

		if(!showTimer.path("redoButtonText").isTextual()){
			throw new CommandParseException("showTimer command should have a 'redoButtonText' property of type string");
		}
		String redoButtonText = showTimer.get("redoButtonText").asText("Opnieuw");

		if(!showTimer.path("continueButtonText").isTextual()){
			throw new CommandParseException("showTimer command should have a 'continueButtonText' property of type string");
		}
		String continueButtonText = showTimer.get("continueButtonText").asText("Doorgaan");

        if(!showTimer.path("timer1").path("description").isTextual()){
            throw new CommandParseException("showTimer command should have a 'timer1' property of with a 'description' property of type String");
        }
        String timer1Descr = showTimer.path("timer1").path("description").asText("Helling 1");

        if(!showTimer.path("timer1").path("time").isInt()){
            throw new CommandParseException("showTimer command should have a 'timer1' property of with a 'time' property of type int");
        }
        int timer1Time = showTimer.path("timer1").path("time").asInt(0);

        if(!showTimer.path("timer2").path("description").isTextual()){
            throw new CommandParseException("showTimer command should have a 'timer2' property of with a 'description' property of type String");
        }
        String timer2Descr = showTimer.path("timer2").path("description").asText("Helling 2");

        if(!showTimer.path("timer2").path("time").isInt()){
            throw new CommandParseException("showTimer command should have a 'timer2' property of with a 'time' property of type int");
        }
        int timer2Time = showTimer.path("timer2").path("time").asInt(0);

        return new ShowTimer(id, reset, description, redoButtonText, continueButtonText, timer1Descr, timer1Time, timer2Descr, timer2Time);
    }

    /**
     * Parses the showCountdown command. Creates the Command if succesful, but throws an exception if the JSON format is incorrect
     * @param showCountdown the JSON that contains the properties for this command
     * @return an instantiated Command object
     * @throws CommandParseException if the format does not match
     */
    private Command parseCountdown(JsonNode showCountdown) throws CommandParseException {
        if(!showCountdown.path("id").isTextual()){
            throw new CommandParseException("showCountdown command should have a 'id' property of type string");
        }
        String id = showCountdown.get("id").asText();

        if(!showCountdown.path("description").isTextual()){
            throw new CommandParseException("showCountdown command should have a 'description' property of type string");
        }
        String description = showCountdown.get("description").asText("Stopwatch");

        if(!showCountdown.path("time").isInt()){
            throw new CommandParseException("showCountdown command should have a 'time' property of type int");
        }
        int time = showCountdown.get("time").asInt(3);

        return new ShowCountdown(id, description, time);
    }

	/**
	 * Parses the showAssignment command. Creates the Command if succesful, but throws an exception if the JSON format is incorrect
	 * @param showAssignment the JSON that contains the properties for this command
	 * @return an instantiated Command object
	 * @throws CommandParseException if the format does not match
	 */
	private Command parseAssignment(JsonNode showAssignment) throws CommandParseException {
		//retrieve the id of this assignment
		if(!showAssignment.path("id").isTextual()){
			throw new CommandParseException("showAssignment command should have a 'id' property of type string");
		}
		String id = showAssignment.get("id").asText();

		//retrieve the text of this assignment
		if(!showAssignment.path("text").isTextual()){
			throw new CommandParseException("showAssignment command should have a 'text' property of type string");
		}
		String text = showAssignment.get("text").asText();

		//retrieve the imageFile of this assignment
		if(!showAssignment.path("imageFile").isTextual()){
			throw new CommandParseException("showAssignment command should have a 'imageFile' property of type string");
		}
		String imageFile = showAssignment.get("imageFile").asText();

		//retrieve the buttonText of this assignment
		if(!showAssignment.path("buttonText").isTextual()){
			throw new CommandParseException("showAssignment command should have a 'buttonText' property of type string");
		}
		String buttonText = showAssignment.get("buttonText").asText();

		//OPTIONAL: should we show the help button?
		boolean showHelp = showAssignment.path("showHelp").asBoolean(false);

		return new ShowAssignment(id, text, imageFile, buttonText, showHelp);
	}


	/**
	 * Parses the showClickablePicture command. Creates the Command if succesful, but throws an exception if the JSON format is incorrect
	 * @param showClickablePicture the JSON that contains the properties for this command
	 * @return an instantiated Command object
	 * @throws CommandParseException if the format does not match
	 */
	private Command parseClickablePicture(JsonNode showClickablePicture) throws CommandParseException {
		//retrieve the id of this assignment
		if(!showClickablePicture.path("id").isTextual()){
			throw new CommandParseException("showClickablePicture command should have a 'id' property of type string");
		}
		String id = showClickablePicture.get("id").asText();

		//retrieve the text of this assignment
		if(!showClickablePicture.path("text").isTextual()){
			throw new CommandParseException("showClickablePicture command should have a 'text' property of type string");
		}
		String text = showClickablePicture.get("text").asText();

		//retrieve the imageFile of this assignment
		if(!showClickablePicture.path("imageFile").isTextual()){
			throw new CommandParseException("showClickablePicture command should have a 'imageFile' property of type string");
		}
		String imageFile = showClickablePicture.get("imageFile").asText();

		//retrieve the imageFileDisabled of this assignment (optional
		String imageFileDisabled = showClickablePicture.get("imageFileDisabled").asText("empty");

		//retrieve the buttons to be shown
		if(!showClickablePicture.path("clickableAreas").isArray() || showClickablePicture.path("clickableAreas").size() == 0){
			throw new CommandParseException("showClickablePicture command should have a 'clickableAreas' property of type array with at least 1 entry");
		}

		//we use a LinkedHashMap here, because it preserves the order in which the buttons were inserted
		Map<String, String> clickableAreas = new LinkedHashMap<String, String>();

		//add all clickable areas from the array
		// clickable area format: x;y;h;v;label
		// x, y : top-left position value of the area; h, w : height and width; label: label of the area
		for(JsonNode btn : showClickablePicture.path("clickableAreas")){
			if(!btn.path("id").isTextual()){
				throw new CommandParseException("showClickablePicture command should have a 'clickableAreas' array containing objects with an 'id' of type string");
			}
			if(!btn.path("value").isTextual()){
				throw new CommandParseException("showClickablePicture command should have a 'clickableAreas' array containing objects with a 'value' of type string with the format: <x;y;h;v;label>");
			}
			clickableAreas.put(btn.get("id").asText(), btn.get("value").asText());
		}
		return new ShowClickablePicture(id, text, imageFile, imageFileDisabled, clickableAreas);
	}

	private Command parseBackgroundColor(JsonNode setBackgroundColor) throws CommandParseException {
		//retrieve the text of this assignment
		if(!setBackgroundColor.path("color").isTextual()){
			throw new CommandParseException("setBackgroundColor command should have a 'color' property of type string");
		}
		String color = setBackgroundColor.get("color").asText();

		return new SetBackgroundColor(color);
	}

	/**
	 * Parses the showSlider command. Creates the Command if succesful, but throws an exception if the JSON format is incorrect
	 * @param showSlider the JSON that contains the properties for this command
	 * @return an instantiated Command object
	 * @throws CommandParseException if the format does not match
	 */
	private Command parseSlider(JsonNode showSlider) throws CommandParseException {
		//retrieve the id of this slider
		if(!showSlider.path("id").isTextual()){
			throw new CommandParseException("showSlider command should have a 'id' property of type string");
		}
		String id = showSlider.get("id").asText();

		//retrieve the text of this slider
		if(!showSlider.path("text").isTextual()){
			throw new CommandParseException("showSlider command should have a 'text' property of type string");
		}
		String text = showSlider.get("text").asText();

		//retrieve the buttonText of this slider
		if(!showSlider.path("buttonText").isTextual()){
			throw new CommandParseException("showSlider command should have a 'buttonText' property of type string");
		}
		String buttonText = showSlider.get("buttonText").asText();

		//retrieve the orientation of this slider
		if(!showSlider.path("orientation").isTextual()){
			throw new CommandParseException("showSlider command should have a 'orientation' property of type string");
		}
		String orientation = showSlider.get("orientation").asText();

		//retrieve the lowText of this slider
		if(!showSlider.path("lowText").isTextual()){
			throw new CommandParseException("showSlider command should have a 'lowText' property of type string");
		}
		String lowText = showSlider.get("lowText").asText();

		//retrieve the highText of this slider
		if(!showSlider.path("highText").isTextual()){
			throw new CommandParseException("showSlider command should have a 'highText' property of type string");
		}
		String highText = showSlider.get("highText").asText();

		//retrieve the steps of this slider
		if(!showSlider.path("steps").canConvertToInt()){
			throw new CommandParseException("showSlider command should have a 'steps' property of type int");
		}
		int steps = showSlider.get("steps").asInt(8);

		//retrieve the defaultStep of this slider
		if(!showSlider.path("defaultStep").canConvertToInt()){
			throw new CommandParseException("showSlider command should have a 'defaultStep' property of type int: 0 <= defaultStep <= step ");
		}
		int defaultStep = showSlider.get("defaultStep").asInt(0);

		return new ShowSlider(id, text, buttonText, orientation, lowText, highText, steps, defaultStep);
	}

	/**
	 * Attempt to parse the showButtons command. Will create a ShowButtons if succesful, or throw a CommandParseException if there is any mistake in the command format
	 * @param showButtons the JsonNode containing the buttons definitions
	 * @return an instantiated Command
	 * @throws CommandParseException if the JSON does not follow the correct format
	 */
	private Command parseShowButtons(JsonNode showButtons) throws CommandParseException {
		//retrieve the text to be shown on screen
		if(!showButtons.path("text").isTextual()){
			throw new CommandParseException("showButtons command should have a 'text' property of type string");
		}
		String text = showButtons.get("text").asText();

		//retrieve the buttons to be shown
		if(!showButtons.path("buttons").isArray() || showButtons.path("buttons").size() == 0){
			throw new CommandParseException("showButtons command should have a 'buttons' property of type array with at least 1 entry");
		}

		//we use a LinkedHashMap here, because it preserves the order in which the buttons were inserted
		List<TextButton> buttons = new LinkedList<TextButton>();

		//add all buttons from the array
		for(JsonNode btn : showButtons.path("buttons")){
			if(!btn.path("id").isTextual()){
				throw new CommandParseException("showButtons command should have a 'buttons' array containing objects with an 'id' of type string");
			}
			if(!btn.path("value").isTextual()){
				throw new CommandParseException("showButtons command should have a 'buttons' array containing objects with a 'value' of type string");
			}

			String id = btn.get("id").asText();
			if("".equals(btn.get("id").asText())){
				id = "rnd" + Math.floor(Math.random()*10000000);
			}

			String value = btn.get("value").asText();

			String color = "lightgrey";
			if(btn.path("color").isTextual()){
				color = btn.get("color").asText();
			}

			//"blink":{"blinkDuration":1,"totalDuration":-1,"colors":["#f47a42","#a2f241","#40bff1"]}
			Blink blink = null;
			if(!btn.path("blink").isMissingNode()){
				JsonNode b = btn.path("blink");

				int blinkDuration = b.path("blinkDuration").asInt(1);
				int totalDuration = b.path("totalDuration").asInt(0);

				List<String> colors = new ArrayList<String>();
				if(b.path("colors").isArray()){
					for(JsonNode c : b.path("colors")){
						if(c.isTextual()) {
							colors.add(c.asText());
						}
					}
				}

				blink = new Blink(blinkDuration, totalDuration, colors);
			}

			TextButton tBtn = new TextButton(id, value, color, blink);

			buttons.add(tBtn);
		}

		//finally, create
		return new ShowButtons(text, buttons);
	}
	/**
	 * Attempt to parse the showImageButtonGrid command. Will create a ShowImageButtonGrid if succesful, or throw a CommandParseException if there is any mistake in the command format
	 * @param showImageButtonGrid the JsonNode containing the buttons definitions
	 * @return an instantiated Command
	 * @throws CommandParseException if the JSON does not follow the correct format
	 */
	private Command parseShowImageButtonGrid(JsonNode showImageButtonGrid) throws CommandParseException {
		//retrieve the text to be shown on screen
		if(!showImageButtonGrid.path("text").isTextual()){
			throw new CommandParseException("showImageButtonGrid command should have a 'text' property of type string");
		}
		String text = showImageButtonGrid.get("text").asText();

		//retrieve the buttons to be shown
		if(!showImageButtonGrid.path("imageButtons").isArray() || showImageButtonGrid.path("imageButtons").size() == 0){
			throw new CommandParseException("showImageButtonGrid command should have a 'imageButtons' property of type array with at least 1 entry");
		}

		//we use a LinkedList here, because it preserves the order in which the buttons were inserted
		List<ImgButton> imageButtons = new LinkedList<ImgButton>();

		//add all buttons from the array
		for(JsonNode btn : showImageButtonGrid.path("imageButtons")){
			if(!btn.path("id").isTextual()){
				throw new CommandParseException("showImageButtonGrid command should have a 'imageButtons' array containing objects with an 'id' of type string");
			}
			if(!btn.path("value").isTextual()){
				throw new CommandParseException("showImageButtonGrid command should have a 'imageButtons' array containing objects with a 'value' of type string");
			}
			if(!btn.path("img").isTextual()){
				throw new CommandParseException("showImageButtonGrid command should have a 'imageButtons' array containing objects with an 'img' of type string");
			}

			String id = btn.get("id").asText();
			if("".equals(btn.get("id").asText())){
				id = "rnd" + Math.floor(Math.random()*10000000);
			}

			imageButtons.add(new ImgButton(id, btn.get("value").asText(), btn.get("img").asText()));
		}

		//finally, create
		return new ShowImageButtonGrid(text, imageButtons);
	}

	/**
	 * Attempt to parse the showPersistentButtons command. Will create a ShowPersistentButtons if succesful, or throw a CommandParseException if there is any mistake in the command format
	 * @param showPersistentButtons the JsonNode containing the buttons definitions
	 * @return an instantiated Command
	 * @throws CommandParseException if the JSON does not follow the correct format
	 */
	private Command parseShowPersistentButtons(JsonNode showPersistentButtons) throws CommandParseException {
		//retrieve the buttons to be shown
		if(!showPersistentButtons.path("buttons").isArray() || showPersistentButtons.path("buttons").size() == 0){
			throw new CommandParseException("showPersistentButtons command should have a 'buttons' property of type array with at least 1 entry");
		}

		//we use a LinkedHashMap here, because it preserves the order in which the buttons were inserted
		Map<String, TextButton> buttons = new LinkedHashMap<String, TextButton>();

		//add all buttons from the array
		for(JsonNode btn : showPersistentButtons.path("buttons")){
			if(!btn.path("id").isTextual()){
				throw new CommandParseException("showPersistentButtons command should have a 'buttons' array containing objects with an 'id' of type string");
			}
			if(!btn.path("value").isTextual()){
				throw new CommandParseException("showPersistentButtons command should have a 'buttons' array containing objects with a 'value' of type string");
			}

			String id = btn.get("id").asText();
			if("".equals(btn.get("id").asText())){
				id = "rnd" + Math.floor(Math.random()*10000000);
			}

			String value = btn.get("value").asText();

			String color = "lightgrey";
			if(btn.path("color").isTextual()){
				color = btn.get("color").asText();
			}

			//"blink":{"blinkDuration":1,"totalDuration":-1,"colors":["#f47a42","#a2f241","#40bff1"]}
			Blink blink = null;
			if(!btn.path("blink").isMissingNode()){
				JsonNode b = btn.path("blink");

				int blinkDuration = b.path("blinkDuration").asInt(1);
				int totalDuration = b.path("totalDuration").asInt(0);

				List<String> colors = new ArrayList<String>();
				if(b.path("colors").isArray()){
					for(JsonNode c : b.path("colors")){
						if(c.isTextual()) {
							colors.add(c.asText());
						}
					}
				}

				blink = new Blink(blinkDuration, totalDuration, colors);
			}

			TextButton tBtn = new TextButton(id, value, color, blink);

			buttons.put(id, tBtn);
		}

		//finally, create
		return new ShowPersistentButtons(buttons);
	}

	public class CommandParseException extends Exception {
		public CommandParseException(String s) {
			super(s);
		}
	}
}
