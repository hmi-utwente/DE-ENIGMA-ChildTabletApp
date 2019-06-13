package nl.utwente.hmi.deenigmachildtabletapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.fasterxml.jackson.databind.JsonNode;

import nl.utwente.hmi.deenigmachildtabletapp.command.*;
import nl.utwente.hmi.deenigmachildtabletapp.communication.CommunicationManager;
import nl.utwente.hmi.deenigmachildtabletapp.widgets.ButtonBlinker;
import nl.utwente.hmi.deenigmachildtabletapp.widgets.VerticalSeekBar;
import nl.utwente.hmi.deenigmachildtabletapp.widgets.VerticalSeekBarWrapper;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.worker.AbstractWorker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.object;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

	private MediaPlayer voicePlayer;
	private MediaPlayer noisePlayer;

	private View backgroundView;
	private View assignmentView;
	private View clickableView;
	private TextView assignmentTextView;
	private ImageView imageView;
	private ImageView clickableImage;
	private Button completeAssignmentButton;
	private Button readAloudButton;

	private RelativeLayout buttonsView;
	private RelativeLayout persistentButtonsView;
	private RelativeLayout imageButtonGridView;
	private RelativeLayout sliderView;

	private TextView timerDescription;
	private TextView timerLeftText;
	private TextView timerLeftValue;
	private TextView timerRightText;
	private TextView timerRightValue;
	private TextView timerOutcome;
	private Button timerRedoButton;
	private Button timerContinueButton;
	private RelativeLayout timerView;

	private TextView countdownDisplay;
	private Handler countdownHandler;
	private RelativeLayout countdownView;

	private TextView ballButtonsDescription;
	private Map<Integer, String> selectedBalls = new LinkedHashMap<Integer, String>();
	private List<Button> ballButtons = new ArrayList<Button>();
	private Button ballButtonsContinueButton;
	private RelativeLayout ballButtonsView;

	private LinearLayout settingsView;
	private TextView connectionStatus;

	private RadioGroup middlewareSelection;
	private RadioButton ROSSelection;
	private RadioButton APOLLOSelection;

	private RadioGroup modeSelection;
	private RadioButton childSelection;
	private RadioButton adultSelection;

	private EditText ipAddress;

	private Button connectBtn;


	private static final int BTN_NEUTRAL = Color.rgb(222,222,222);
	private static final int BTN_SELECTED = Color.rgb(22,222,0);

	private SeekBar seekBar;
	private Button continueBtn;
	
	private boolean buttonEnabled;
	private float buttonAlpha = 0.88f;

	private Command currentCommand;

	private static final int SEEK_BAR_SIZE = 750;

	public static final String STOMP_TABLET_COMMAND_POST = "/topic/tablet.command";
	public static final String STOMP_TABLET_FEEDBACK = "/topic/tablet.feedback";
	private JSONCommandParser jsonParser;

	/** the precision at which to display the timer values on the tablet */
	private static final int TIMER_PRECISION = 50;

	/** Some thresholds to indicae when two timers are a draw, or when one timer has "won"*/
	private static final int TIMER_DRAW_THRESHOLD = 50;
	private static final int TIMER_WIN_THRESHOLD = 100;

	private static final int DEFAULT_TEXT_SIZE = 22;
	private static final int TEXT_SIZE_VARIATION = 6;
	private TextView lowText;
	private TextView highText;
	private CommunicationManager commMngr;
	private boolean settingsActive;
	private CommunicationManager.AvailableModes selectedMode;
	private CommunicationManager.AvailableMiddlewares selectedMiddleware;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //allow network communication (stomp) in main thread
        //TODO: move stomp communication to separate thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        
		setContentView(R.layout.activity_main);

		//the topmost parent container
		backgroundView = (RelativeLayout) findViewById(R.id.background_view);

		//for the settings screen
		settingsView = (LinearLayout)findViewById(R.id.settings_view);
		connectionStatus = (TextView)findViewById(R.id.connectionStatus);

		middlewareSelection = (RadioGroup)findViewById(R.id.radio_middleware);
		ROSSelection = (RadioButton)findViewById(R.id.radio_ros);
		APOLLOSelection = (RadioButton)findViewById(R.id.radio_apollo);

		modeSelection = (RadioGroup)findViewById(R.id.radio_mode);
		childSelection = (RadioButton)findViewById(R.id.radio_child);
		adultSelection = (RadioButton)findViewById(R.id.radio_adult);

		ipAddress = (EditText)findViewById(R.id.ipaddress);

		connectBtn = (Button)findViewById(R.id.connect);


		//get the view elements we want to change for the assignment view
		assignmentView = (RelativeLayout) findViewById(R.id.assignment_view);
		assignmentTextView = (TextView) findViewById(R.id.assignment_text_view);
		imageView = (ImageView) findViewById(R.id.image_view);
		completeAssignmentButton = (Button) findViewById(R.id.complete_assignment_button);
		readAloudButton = (Button) findViewById(R.id.read_aloud_button);

		//get the view elements we want to change for the clickable picture view
		clickableView = (RelativeLayout) findViewById(R.id.clickable_view);
		clickableImage = (ImageView) findViewById(R.id.clickable_view_image);

		//everything we need for the button view
		buttonsView = (RelativeLayout) findViewById(R.id.buttons_view);

		//everything we need for the persistent button view
		persistentButtonsView = (RelativeLayout) findViewById(R.id.persistent_buttons_view);

		imageButtonGridView = (RelativeLayout) findViewById(R.id.image_button_grid_view);

		//what we need for the slider view
		sliderView = (RelativeLayout) findViewById(R.id.slider_view);

		//the stuff needed for displaying timer view
		timerDescription = (TextView) findViewById(R.id.timer_description_text);
		timerLeftText = (TextView) findViewById(R.id.timer_left_text);
		timerLeftValue = (TextView) findViewById(R.id.timer_left_value);
		timerRightText = (TextView) findViewById(R.id.timer_right_text);
		timerRightValue = (TextView) findViewById(R.id.timer_right_value);
		timerOutcome = (TextView) findViewById(R.id.timer_outcome);
		timerRedoButton = (Button) findViewById(R.id.timer_redo_button);
		timerContinueButton = (Button) findViewById(R.id.timer_continue_button);
		timerView = (RelativeLayout) findViewById(R.id.timer_view);

		//everything we need to display the countdown view
		countdownHandler = new Handler(Looper.getMainLooper());
		countdownDisplay = (TextView) findViewById(R.id.countdown_display);
		countdownView = (RelativeLayout) findViewById(R.id.countdown_view);

		//all the buttons in the ball button selection view
		ballButtonsDescription = (TextView) findViewById(R.id.ball_buttons_description);
		ballButtons.add((Button) findViewById(R.id.btn_bm));
		ballButtons.add((Button) findViewById(R.id.btn_sm));
		ballButtons.add((Button) findViewById(R.id.btn_sb));
		ballButtons.add((Button) findViewById(R.id.btn_bb));
		ballButtons.add((Button) findViewById(R.id.btn_wb));
		ballButtons.add((Button) findViewById(R.id.btn_pb));
		ballButtonsContinueButton = (Button) findViewById(R.id.ball_buttons_continue_button);
		ballButtonsView = (RelativeLayout) findViewById(R.id.ball_buttons_view);

		//we use this to parse incoming commands
		jsonParser = new JSONCommandParser();

		//create our communication manager
		this.commMngr = new CommunicationManager();
		//commMngr.start();

		//init the worker thread which will process new Data from the middleware
		CommandReceiver cr = new CommandReceiver();
		new Thread(cr).start();

		//finally, tell the middleware where to send incoming data
		commMngr.addListener(cr);
	}


	@Override
	public void onResume(){
		super.onResume();

		//go back to fullscreen
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}


	@Override
	public void onStart(){
		super.onStart();

		//go back to fullscreen
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		float score = ((float) progress / seekBar.getMax() - 0.5f) * 2.0f;
		float sizeCorrection = score * TEXT_SIZE_VARIATION;
		Log.d("daniel", "Max: "+seekBar.getMax()+" Progress: "+progress+ " Score: " + score + " sizeCorrection: "+sizeCorrection);
		highText.setTextSize(DEFAULT_TEXT_SIZE + sizeCorrection);
		lowText.setTextSize(DEFAULT_TEXT_SIZE - sizeCorrection);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		continueBtn.setEnabled(true);
	}


	/**
	 * Receive a command from the middleware and display the correct information on the screen
	 * @author davisond
	 *
	 */
	public class CommandReceiver extends AbstractWorker implements MiddlewareListener {

        @Override
		public void receiveData(JsonNode jsonNode) {
			if(settingsActive){
				return;
			}
			this.addDataToQueue(jsonNode);
		}

		@Override
		public void processData(JsonNode jsonNode) {

			Log.d("daniel", "Got message 2: "+jsonNode.toString());

			try {
				currentCommand = jsonParser.parseJSON(jsonNode);
			} catch (JSONCommandParser.CommandParseException e) {
				Log.e("daniel","invalid JSON command received: "+jsonNode.toString(),e);
			}

			//update the text to the screen and show buttonsetc. when required.. this needs to be executed on the main UI thread because of...reasons...
			//since we are now in a separate callback thread from stomp we need to make sure to execute this on the UI thread
			runOnUiThread(new Runnable() {
				private final Command c = currentCommand;

				@Override
				public void run() {
					if(c instanceof ShowAssignment) {
						showAssignment((ShowAssignment) c);
					} else if(c instanceof ShowClickablePicture){
						showClickablePicture((ShowClickablePicture)c);
					} else if(c instanceof SetBackgroundColor){
						setBackgroundColor((SetBackgroundColor)c);
					} else if(c instanceof ShowButtons){
						showButtons((ShowButtons)c);
					} else if(c instanceof ShowPersistentButtons){
						showPersistentButtons((ShowPersistentButtons)c);
					} else if(c instanceof ShowImageButtonGrid){
						showImageButtonGrid((ShowImageButtonGrid)c);
					} else if(c instanceof ShowSlider){
						showSlider((ShowSlider)c);
					} else if(c instanceof ShowTimer){
						showTimer((ShowTimer)c);
					} else if(c instanceof ShowCountdown){
						showCountdown((ShowCountdown)c);
					} else if(c instanceof ShowBallButtons){
						showBallButtons((ShowBallButtons)c);
					}

					//try to go fullscreen
					setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
							| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
				}
			});

		}
	}


	/**
	 * Displays a large countdown timer to the user in the middle of the screen
	 * @param sc the countdown values
	 */
	private void showCountdown(ShowCountdown sc){
		hideAllViews();

		//add screen update tasks for each second in our countdown
		for(int i = 0; i <= sc.getTime(); i++){
			countdownHandler.postDelayed(new CountdownThread(countdownDisplay, i), (sc.getTime() * 1000) - (i * 1000));
		}

		countdownView.setVisibility(View.VISIBLE);
	}



	/**
	 * Displays a timer to the user for both sides of the ramp.
	 * @param st the timer values
	 */
	private void showTimer(ShowTimer st){
		hideAllViews();

		if(st.isReset()){
			resetTimer();
		}

		timerDescription.setText(st.getDescription());

		int t1 = st.getTimer1Time();
		int t2 = st.getTimer2Time();
		int t1Round = floor(st.getTimer1Time(), TIMER_PRECISION);
		int t2Round = floor(st.getTimer2Time(), TIMER_PRECISION);

		//here we calculate the difference between the two timers, to see if they are "equal", "almost equal" or if there is a clear "winner"
		if(st.getTimer1Time() > 0 && st.getTimer2Time() > 0){
			String outcomeText = "";
			long diff = Math.abs(st.getTimer1Time() - st.getTimer2Time());
			if(diff < TIMER_DRAW_THRESHOLD){
				outcomeText = "Gelijkspel";
				t1Round = floor(st.getTimer1Time(), TIMER_PRECISION);
				t2Round = t1Round;
			} else if(diff >= TIMER_DRAW_THRESHOLD && diff < TIMER_WIN_THRESHOLD){
				outcomeText = "Bijna gelijkspel";
				t1Round = floor(st.getTimer1Time(), TIMER_PRECISION);
				t2Round = t1Round;
			} else if (diff >= TIMER_WIN_THRESHOLD && t1 < t2){
				outcomeText = st.getTimer1Description() + " heeft gewonnen!";
				t1Round = floor(st.getTimer1Time(), TIMER_PRECISION);
				t2Round = ceil(st.getTimer2Time(), TIMER_PRECISION);
			} else if (diff >= TIMER_WIN_THRESHOLD && t2 < t1){
				outcomeText = st.getTimer2Description() + " heeft gewonnen!";
				t1Round = ceil(st.getTimer1Time(), TIMER_PRECISION);
				t2Round = floor(st.getTimer2Time(), TIMER_PRECISION);
			}

			timerOutcome.setText(outcomeText);
		}

		if(st.getTimer1Time() > 0){
			timerLeftText.setText(st.getTimer1Description());
			timerLeftValue.setText(String.format("Tijd: %d milliseconden", t1Round));
		}

		if(st.getTimer2Time() > 0){
			timerRightText.setText(st.getTimer2Description());
			timerRightValue.setText(String.format("Tijd: %d milliseconden",t2Round));
		}

		if(!"".equals(st.getRedoButtonText())){
			timerRedoButton.setText(st.getRedoButtonText());
			timerRedoButton.setVisibility(View.VISIBLE);

			timerRedoButton.setAlpha(buttonAlpha);
		} else {
			timerRedoButton.setVisibility(View.INVISIBLE);
		}

		if(!"".equals(st.getContinueButtonText())){
			timerContinueButton.setText(st.getContinueButtonText());
			timerContinueButton.setVisibility(View.VISIBLE);

			//TODO: for now we always leave the button enabled, but we might make this optional in the future, like in the verbalisation study
			//timerContinueButton.setEnabled(false);
			timerContinueButton.setAlpha(buttonAlpha);
		} else {
			timerContinueButton.setVisibility(View.INVISIBLE);
		}



		timerView.setVisibility(View.VISIBLE);
	}

	/**
	 * This small helper function will round-down (floor) a given number i to the closest multiple of v. Example: i=1236, v=50 will return 1200 while 1299, v=50 will return 1250.
	 * @param i the number to floor
	 * @param v the closest multiple to round to
	 * @return the closest rounded multiple of v
	 */
	private int floor(double i, int v){
		return (int)Math.floor(i/v) * v;
	}

	/**
	 * This small helper function will round-up (ceil) a given number i to the closest multiple of v. Example: i=1236, v=50 will return 1250 while 1251, v=50 will return 1300.
	 * @param i the number to ceil
	 * @param v the closest multiple to round to
	 * @return the closest rounded multiple of v
	 */
	private int ceil(double i, int v){
		return (int)Math.ceil(i/v) * v;
	}

	/**
	 * Resets the timer displays on the tablet
	 */
	private void resetTimer(){
		timerLeftText.setText("");
		timerLeftValue.setText("");
		timerRightText.setText("");
		timerRightValue.setText("");
		timerOutcome.setText("");
	}

	/**
	 * Displays an assignment to the user. Might contain text, a button and an image
	 * @param sa the assignment to show
	 */
	private void showAssignment(ShowAssignment sa){
		hideAllViews();

		assignmentTextView.setText(sa.getText());

		if(!"".equals(sa.getImageFile())){
			int imageResourceID = getApplicationContext().getResources().getIdentifier(sa.getImageFile(), "drawable", getApplicationContext().getPackageName());
			imageView.setImageResource(imageResourceID);
			imageView.setVisibility(View.VISIBLE);
		} else {
			imageView.setVisibility(View.INVISIBLE);
		}

		if(!"".equals(sa.getButtonText())){
			completeAssignmentButton.setText(sa.getButtonText());
			completeAssignmentButton.setVisibility(View.VISIBLE);

			//TODO: for now we always leave the button enabled, but we might make this optional in the future, like in the verbalisation study 
			//completeAssignmentButton.setEnabled(false);
			completeAssignmentButton.setAlpha(buttonAlpha);
		} else {
			completeAssignmentButton.setVisibility(View.INVISIBLE);
		}

		if(sa.isShowHelp()){
			readAloudButton.setVisibility(View.VISIBLE);
			readAloudButton.setAlpha(buttonAlpha);
		} else {
			readAloudButton.setVisibility(View.INVISIBLE);
		}

		assignmentView.setVisibility(View.VISIBLE);
	}

	/**
	 * Displays a clickable picture to the user. Might contain a text
	 * @param scp
	 */
	@SuppressLint("ClickableViewAccessibility")
	private void showClickablePicture(final ShowClickablePicture scp){
		hideAllViews();

        if(!"".equals(scp.getImageFile())){
            int imageResourceID = getApplicationContext().getResources().getIdentifier(scp.getImageFile(), "drawable", getApplicationContext().getPackageName());
			clickableImage.setImageResource(imageResourceID);

			clickableImage.setVisibility(View.VISIBLE);
		} else {
			clickableImage.setVisibility(View.INVISIBLE);
		}
        clickableView.setVisibility(View.VISIBLE);

        // Define the clickable areas (pixel values: x coordinate, y coordinate, width, height) and assign a label to it
         final List<String> listClickableAreas =  new ArrayList<>();
		for(Entry<String, String> ca : scp.getClickableAreas().entrySet()){
            listClickableAreas.add(ca.getValue());
		}

//		clickableAreasImage.setClickableAreas(listClickableAreas);

        clickableImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    // we need the info on the original and new sizes of the image displayed
                    // the clickable areas must be taken from the original image but rescale in regard of the display
                    // keep the real size of the source image
                    Resources resources = clickableImage.getContext().getResources();
                    BitmapFactory.Options bounds = new BitmapFactory.Options();
                    bounds.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(resources, getApplicationContext().getResources().getIdentifier(scp.getImageFile(), "drawable", getApplicationContext().getPackageName()), bounds);
                    float sourceImageHeight = (float) bounds.outHeight;
                    float sourceImageWidth = (float) bounds.outWidth;

                    float displayedHeight = (float) clickableImage.getHeight();
                    float displayedWidth = (float) clickableImage.getWidth();

                    float scaleX = displayedWidth/sourceImageWidth;
                    float scaleY = displayedHeight/sourceImageHeight;

                    if (scaleX ==0 || scaleY ==0){
                        // if the image is not yet displayed on the screen
                        Log.i("clickable_debug", "the picture is not shown on the tablet yet, please wait");
                    }else{
						Log.i("clickable_debug","Touch coordinates : " + String.valueOf(event.getX()) + "x" + String.valueOf(event.getY()));

						float eventx = event.getX() / scaleX;
                        float eventy = event.getY() / scaleY;

                        for (int ii=0; ii < listClickableAreas.size(); ii++) {
                            String[] caFields = listClickableAreas.get(ii).split(";");
                            int areax = Integer.parseInt(caFields[0]);
                            int areay = Integer.parseInt(caFields[1]);
                            int areah = Integer.parseInt(caFields[2]);
                            int areaw = Integer.parseInt(caFields[3]);
                            String label = caFields[4];
							Log.i("clickable_debug","fields : " + String.valueOf(areax) + "; " + String.valueOf(areay) + "; " + String.valueOf(areah) + "; " + String.valueOf(areaw));

                            if (eventx >= areax && eventx < areax + areaw && eventy >= areay && eventy < areay + areah) {
                                Log.i("clickable_debug", "the clicked area is : " + label);

                                JsonNode jn = object(
                                        "clickableAreaPressed", object(
                                                "areaID", label
                                        ) //JSON: {"buttonPress" : {"buttonID" : "START_SYSTEM" }}
                                ).end();
								commMngr.sendData(jn);


								int imageResourceID = getApplicationContext().getResources().getIdentifier(scp.getImageFileDisabled(), "drawable", getApplicationContext().getPackageName());
								clickableImage.setImageResource(imageResourceID);
								clickableImage.setOnTouchListener(null);
                                // we only take in account one area. If there are overlaps, only the first one is returned.
                                break;
                            }
                        }
                    }

                }
                return true;
            }
        });


	}


	private void setBackgroundColor(final SetBackgroundColor sbc) {
		backgroundView.setBackgroundColor(Color.parseColor(sbc.getColor()));
	}

	/**
	 * Displays a slider to the user.
	 * @param ss the slider to show
	 */
	private void showSlider(ShowSlider ss){
		hideAllViews();

		//create a new vertical oriented layout, which will hold the text and buttons
		LinearLayout linView = new LinearLayout(this);
		linView.setOrientation(LinearLayout.VERTICAL);
		LayoutParams linViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		//create and add questionText and all answers to the interface
		TextView questionText = new TextView(this);
		questionText.setText(ss.getText());
		questionText.setTextSize(30);
		questionText.setPadding(0, 0, 0, 66);
		LayoutParams questionTextParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		//create the high and low texts to be shown above and below the slider
		lowText = new TextView(this);
		lowText.setText(ss.getLowText());
		lowText.setTextSize(22);
		lowText.setPadding(22, 22, 22, 22);
		LayoutParams lowTextParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		highText = new TextView(this);
		highText.setText(ss.getHighText());
		highText.setTextSize(22);
		highText.setPadding(22, 22, 22, 22);
		LayoutParams highTextParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		//make the continue button
		continueBtn = new Button(this);
		continueBtn.setText(ss.getButtonText());
		continueBtn.setTextSize(22);
		continueBtn.setPadding(0, 48, 0, 48);
		continueBtn.setTag(ss.getId());
		continueBtn.setOnClickListener(sliderSubmit);
		continueBtn.setEnabled(false);
		LinearLayout.LayoutParams continueBtnParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		continueBtnParams.setMargins(0, 142, 0, 0);

		//add the components to the view
		linView.addView(questionText, questionTextParams);

		//show slider differently based on preferred orientation
		LinearLayout sliderOrientation = new LinearLayout(this);
		sliderOrientation.setOrientation(LinearLayout.HORIZONTAL);
		sliderOrientation.setGravity(Gravity.CENTER_HORIZONTAL);
		LinearLayout.LayoutParams sliderOrientationParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		sliderOrientationParams.setMargins(0, 142, 0, 0);

		//make the slider object (we use a seekbar for this purpose)
		seekBar = new SeekBar(this);
		LayoutParams seekBarParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		//wrapper for seekbar..
		FrameLayout seekWrapper = new FrameLayout(this);
		LayoutParams seekWrapperParams = new LayoutParams(SEEK_BAR_SIZE, LayoutParams.WRAP_CONTENT);

		//should this be a vertical seekbar?
		if("vertical".equals(ss.getOrientation())) {
			sliderOrientation.setOrientation(LinearLayout.VERTICAL);
			seekWrapper = new VerticalSeekBarWrapper(this);
			seekBar = new VerticalSeekBar(this);
			seekBarParams = new LayoutParams(0, 0);
			seekWrapperParams = new LayoutParams(LayoutParams.WRAP_CONTENT, SEEK_BAR_SIZE);
		}

		//set some additional values for the slider
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setMax(ss.getSteps());
		seekBar.setProgress(ss.getDefaultStep());
		seekBar.setPadding(22,22,22,22);
		seekBar.setScaleY(2f);

		//tweak the size of the slider
		/*ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
		thumb.getPaint().setColor(Color.RED);
		thumb.setIntrinsicHeight(100);
		thumb.setIntrinsicWidth(30);
		seekBar.setThumb(thumb);*/

		//add the slider and texts
		seekWrapper.addView(seekBar, seekBarParams);

		sliderOrientation.addView(highText, highTextParams);
		sliderOrientation.addView(seekWrapper, seekWrapperParams);
		sliderOrientation.addView(lowText, lowTextParams);
		linView.addView(sliderOrientation, sliderOrientationParams);

		linView.addView(continueBtn, continueBtnParams);


		//now add the view to the app :)
		sliderView.removeAllViews();
		sliderView.addView(linView, linViewParams);
		sliderView.setVisibility(View.VISIBLE);
	}

	/**
	 * Shows the ball buttons and description
	 * @param sbb
	 */
	private void showBallButtons(ShowBallButtons sbb){
		hideAllViews();
		resetBallButtons();

		ballButtonsDescription.setText(Html.fromHtml(sbb.getDescription()));
		ballButtonsContinueButton.setText(sbb.getButtonText());

		ballButtonsView.setVisibility(View.VISIBLE);
	}

	/**
	 * Resets all ball buttons to their neutral selected state
	 */
	private void resetBallButtons(){
		selectedBalls.clear();
		for(Button b : ballButtons){
			b.setBackgroundColor(BTN_NEUTRAL);
		}
		removeBallButtonRank();
	}

	/**
	 * Displays buttons to the user. The ShowButtons command contains a list of button texts and their return values.
	 * //TODO: extend this to allow images, and multiple toggles, etc..
	 * @param sb
	 */
	private void showButtons(ShowButtons sb){
		hideAllViews();

		//create a new vertical oriented layout, which will hold the text and buttons
		LinearLayout linView = new LinearLayout(this);
		linView.setOrientation(LinearLayout.VERTICAL);
		LayoutParams linViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		//create and add question and all answers to the interface
		TextView q = new TextView(this);
		q.setText(sb.getText());
		q.setTextSize(22);
		q.setPadding(100, 0, 0, 33);
		LayoutParams qParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		linView.addView(q, qParams);

		for(TextButton tBtn : sb.getButtons()){
			String id = tBtn.getId();

			if("blankSpace".equals(tBtn.getText())){
				Space space = new Space(this);
				space.setMinimumHeight(32);
				linView.addView(space);
			} else if("textOnly".equals(id)) {
				TextView txt = new TextView(this);
				txt.setText(tBtn.getText());
				txt.setTextSize(16);
				txt.setGravity(Gravity.CENTER);
				txt.setPadding(0,0,0,20);
				txt.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
				linView.addView(txt);
			}else {
				//this is for color tints, instead of just setting the background color.. this retains the regular pressed animation events etc.
				int[][] states = new int[][] {
						new int[] { android.R.attr.state_pressed},
						new int[] { android.R.attr.state_enabled}
				};

				int[] colors = new int[] {
						Color.GREEN,
						Color.parseColor(tBtn.getColor())
				};

				ColorStateList colLst = new ColorStateList(states, colors);

				Button answerBtn = new Button(this);
				answerBtn.setText(tBtn.getText());
				answerBtn.setBackgroundTintList(colLst);
				answerBtn.setTextSize(16);
				answerBtn.setPadding(0, 26, 0, 26);
				answerBtn.setTag(id);
				answerBtn.setOnClickListener(buttonSubmit);
				LinearLayout.LayoutParams answerBtnParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				answerBtnParams.setMargins(100, 5, 100, 0);
				//answerBtn.setLayoutParams(answerBtnParams);
				linView.addView(answerBtn, answerBtnParams);


				Blink blink = tBtn.getBlink();
				if(blink != null && blink.getColors().size() > 0) {

					List<ColorStateList> blinkColors = new ArrayList<ColorStateList>();

					for(String blinkColor : blink.getColors()) {
						blinkColors.add(new ColorStateList(states, new int[] {Color.GREEN, Color.parseColor(blinkColor)}));
					}

					ButtonBlinker bb = new ButtonBlinker(answerBtn, blink.getBlinkDuration(), blink.getTotalDuration(), colLst, blinkColors);
					bb.start();
				}
			}
		}

		//now add the view to the app :)
		buttonsView.removeAllViews();
		buttonsView.addView(linView, linViewParams);
		buttonsView.setVisibility(View.VISIBLE);
	}


	/**
	 * Displays buttons to the user. The ShowButtons command contains a list of button texts and their return values.
	 * //TODO: extend this to allow images, and multiple toggles, etc..
	 * @param ibg
	 */
	private void showImageButtonGrid(ShowImageButtonGrid ibg){
		hideAllViews();

		//create a new vertical oriented layout, which will hold the text and buttons
		LinearLayout linView = new LinearLayout(this);
		linView.setOrientation(LinearLayout.VERTICAL);
		linView.setGravity(Gravity.CENTER_VERTICAL);
		LayoutParams linViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		GridLayout gridLayout = new GridLayout(this);
		List<ImgButton> buttons = ibg.getButtons();

		int total = buttons.size();
		int column =  2;
		int row = total / column;

		gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
		gridLayout.setColumnCount(column);
		gridLayout.setRowCount(row + 1);

		int i = 0;
		int c = 0;
		int r = 0;
		for(ImgButton button : buttons){
			if(c == column)
			{
				c = 0;
				r++;
			}
			int imageResourceID = getApplicationContext().getResources().getIdentifier(button.getImg(), "drawable", getApplicationContext().getPackageName());

			Drawable imgFile = ContextCompat.getDrawable(getApplicationContext(), imageResourceID);

			int oWidth = imgFile.getIntrinsicWidth();
			int oHeight = imgFile.getIntrinsicHeight();
			int nWidth = 650;
			double scaleFactor = (double) nWidth / oWidth;
			int nHeight = (int) Math.round(oHeight * scaleFactor);

			Button iBtn = new Button(this);
			iBtn.setBackground(imgFile);
			//iBtn.setText(button.getText());
			iBtn.setTextSize(16);
            iBtn.setPadding(0, 0, 0, 0);
			iBtn.setTag(button.getId());
			iBtn.setOnClickListener(buttonSubmit);

			GridLayout.LayoutParams iBtnParams = new GridLayout.LayoutParams();
			iBtnParams.setMargins(22, 142, 22, 22);
			iBtnParams.setGravity(Gravity.CENTER);
			iBtnParams.columnSpec = GridLayout.spec(c);
			iBtnParams.rowSpec = GridLayout.spec(r);
			iBtnParams.width = nWidth;
			iBtnParams.height = nHeight;

			iBtn.setLayoutParams(iBtnParams);

			gridLayout.addView(iBtn, i);

			i++;
			c++;
		}

		LayoutParams gridLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		//now add the view to the app :)
		linView.removeAllViews();
		linView.addView(gridLayout, gridLayoutParams);

		//create and add question and all answers to the interface
		TextView text = new TextView(this);
		text.setText(ibg.getText());
		text.setTextSize(42);
		text.setPadding(0, 100, 0, 33);
		text.setGravity(Gravity.CENTER);
		LayoutParams tParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		linView.addView(text, tParams);

		//now add the view to the app :)
        imageButtonGridView.removeAllViews();
        imageButtonGridView.addView(linView, linViewParams);
        imageButtonGridView.setVisibility(View.VISIBLE);
	}

	/**
	 * Displays persistent buttons to the user on the bottom of the screen. These do not reset when the rest of the screen is cleared! The ShowPersistentButtons command contains a list of button texts and their return values.
	 * //TODO: extend this to allow images, and multiple toggles, etc..
	 * @param spb
	 */
	private void showPersistentButtons(ShowPersistentButtons spb){
		//hideAllViews();
		GridLayout gridLayout = new GridLayout(this);
		Map<String, TextButton> buttons = spb.getButtons();

		int total = buttons.size();
		int column =  3;
		int row = total / column;

		gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
		gridLayout.setColumnCount(column);
		gridLayout.setRowCount(row + 1);
		int i = 0;
		int c = 0;
		int r = 0;
		for(Entry<String, TextButton> button : buttons.entrySet()){
			if(c == column)
			{
				c = 0;
				r++;
			}

			TextButton tBtn = button.getValue();

			//this is for color tints, instead of just setting the background color.. this retains the regular pressed animation events etc.
			int[][] states = new int[][] {
					new int[] { android.R.attr.state_pressed},
					new int[] { android.R.attr.state_enabled}
			};

			int[] colors = new int[] {
					Color.GREEN,
					Color.parseColor(tBtn.getColor())
			};

			ColorStateList colLst = new ColorStateList(states, colors);

			Button answerBtn = new Button(this);
			answerBtn.setText(tBtn.getText());
			answerBtn.setBackgroundTintList(colLst);
			answerBtn.setTextSize(16);
			answerBtn.setPadding(0, 26, 0, 26);
			answerBtn.setTag(button.getKey());
			answerBtn.setOnClickListener(buttonSubmit);

			gridLayout.addView(answerBtn, i);

			GridLayout.LayoutParams answerBtnParams = new GridLayout.LayoutParams();
			answerBtnParams.setMargins(5,5,5,5);
			answerBtnParams.setGravity(Gravity.CENTER);
			answerBtnParams.columnSpec = GridLayout.spec(c);
			answerBtnParams.rowSpec = GridLayout.spec(r);
			answerBtnParams.width = dp2px(200);

			answerBtn.setLayoutParams(answerBtnParams);

			Blink blink = tBtn.getBlink();
			if(blink != null && blink.getColors().size() > 0) {

				List<ColorStateList> blinkColors = new ArrayList<ColorStateList>();

				for(String blinkColor : blink.getColors()) {
					blinkColors.add(new ColorStateList(states, new int[] {Color.GREEN, Color.parseColor(blinkColor)}));
				}

				ButtonBlinker bb = new ButtonBlinker(answerBtn, blink.getBlinkDuration(), blink.getTotalDuration(), colLst, blinkColors);
				bb.start();
			}

			i++;
			c++;
		}

		LayoutParams gridLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		//now add the view to the app :)
		persistentButtonsView.removeAllViews();
		persistentButtonsView.addView(gridLayout, gridLayoutParams);
		persistentButtonsView.setVisibility(View.VISIBLE);
	}

	public int dp2px(int dp) {
		float density = getApplicationContext().getResources()
				.getDisplayMetrics()
				.density;
		return Math.round((float) dp * density);
	}

	/**
	 * Hides all views from the display. Can be used to reset the interface to a blank state.
	 */
	private void hideAllViews(){
		assignmentView.setVisibility(View.INVISIBLE);
		clickableView.setVisibility(View.INVISIBLE);
		// buttonsView.setVisibility(View.INVISIBLE);
		imageButtonGridView.setVisibility(View.INVISIBLE);
		sliderView.setVisibility(View.INVISIBLE);
		timerView.setVisibility(View.INVISIBLE);
		countdownView.setVisibility(View.INVISIBLE);
		ballButtonsView.setVisibility(View.INVISIBLE);
	}

	/**
	 * When a user clicks on a ball button.. this function will toggle the background color
	 * @param v the view on which was clicked
	 */
	public void ballButtonPress(View v){
		if(v.getTag() instanceof String){
			if(currentCommand instanceof ShowBallButtons){
				if(!((ShowBallButtons) currentCommand).isMultipleSelect()){
					resetBallButtons();
				}
			}
			int id = v.getId();
			String ballTag = (String) v.getTag();
			if(!selectedBalls.containsKey(id)) {
				v.setBackgroundColor(BTN_SELECTED);
				selectedBalls.put(id, ballTag);
			} else {
				//if we are doing ranked stuff, deselect everything to ensure it's not weird
				if(currentCommand instanceof ShowBallButtons){
					if(((ShowBallButtons) currentCommand).isRank()){
						resetBallButtons();
					}
				}
				v.setBackgroundColor(BTN_NEUTRAL);
				selectedBalls.remove(id);
			}
			if(currentCommand instanceof ShowBallButtons){
				if(((ShowBallButtons) currentCommand).isRank()){
					removeBallButtonRank();
					addBallButtonRank();
				}
			}
		}
	}

	/**
	 * Removes a ranking from depressed buttons.
	 */
	private void removeBallButtonRank(){
		//remove all existing ranks
		for(Button b : ballButtons){
			if(Character.isDigit(b.getText().charAt(0))){
				b.setText(b.getText().subSequence(3, b.getText().length()));
			}
		}
	}

	/**
	 * Adds a ranking to the selected balls in the order in which they were pressed.
	 */
	private void addBallButtonRank(){
		//redo ranks for all selected buttons, in the order which they were originally added to the map
		int i = 1;
		for(Entry<Integer,String> e : selectedBalls.entrySet()){
			Button b = (Button)findViewById(e.getKey());
			b.setText(i + ". " + b.getText());
			i++;
		}
	}

	public void submitBalls(View v){
		if(currentCommand instanceof ShowBallButtons){
			//pun intended :)
			String ballsString = TextUtils.join("+",selectedBalls.values());
			String id = currentCommand.getId();
			//String xml = String.format("<continueassignment><params><assignmentid><string>%s</string></assignmentid></params></continueassignment>", assID);
			//stomp.sendMessage(xml, STOMP_TABLET_FEEDBACK);
			JsonNode jn = object(
					"ballButtons", object()
							.with("ID", id)
							.with("selectedBalls", ballsString)
					).end();
			commMngr.sendData(jn);
			hideAllViews();
		}

	}

	/**
	 * The user presses the button to continue with an assignment
	 * @param v
	 */
	public void continueAssignment(View v){
		//at this moment, just send back the ID of the assignment which has been completed
		//maybe in the future we need some more info, but for now this will do
		if(currentCommand instanceof ShowAssignment || currentCommand instanceof ShowTimer){
			String assID = currentCommand.getId();
			//String xml = String.format("<continueassignment><params><assignmentid><string>%s</string></assignmentid></params></continueassignment>", assID);
			//stomp.sendMessage(xml, STOMP_TABLET_FEEDBACK);
			JsonNode jn = object(
					"continueAssignment", object(
							"assignmentID", assID
					)
			).end();
			commMngr.sendData(jn);
			hideAllViews();
		}

	}

	/**
	 * The user presses the button to redo an assignment from the beginning
	 * @param v
	 */
	public void redoAssignment(View v){
		//at this moment, just send back the ID of the assignment which has been completed
		//maybe in the future we need some more info, but for now this will do
		if(currentCommand instanceof ShowTimer){
			String assID = currentCommand.getId();
			//String xml = String.format("<continueassignment><params><assignmentid><string>%s</string></assignmentid></params></continueassignment>", assID);
			//stomp.sendMessage(xml, STOMP_TABLET_FEEDBACK);
			JsonNode jn = object(
					"redoAssignment", object(
							"assignmentID", assID
					)
			).end();
			commMngr.sendData(jn);
			hideAllViews();
		}

	}

	/**
	 * The user wants to have the assignment read out loud
	 * @param v
	 */
	public void readAloud(View v){
		//at this moment, just send back the ID of the assignment which should be read out loud
		//maybe in the future we need some more info, but for now this will do
		if(currentCommand instanceof ShowAssignment){
			String assID = ((ShowAssignment)currentCommand).getId();
			JsonNode jn = object(
					"readAloud", object(
							"assignmentID", assID
					)
			).end();
			commMngr.sendData(jn);
			//hideAllViews();
		}

	}

	/**
	 * Called when a user submits a slider value by pressing the continue button
	 */
	OnClickListener sliderSubmit = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(v.getTag() instanceof String){
				String id = (String)v.getTag();
				JsonNode jn = object(
						"slider", object()
								.with("ID", id)
								.with("value", seekBar.getProgress())
				).end();
				commMngr.sendData(jn);
				hideAllViews();
			}
		}
	};

	public void askHelp(View v){
		//stomp.sendMessage("askHelp", STOMP_TABLET_ASSIGNMENT_FEEDBACK);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Called when a user clicks one of the buttons in a ButtonCommand
	 */
	OnClickListener buttonSubmit = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof String) {
				String bID = (String) v.getTag();
				JsonNode jn = object(
						"buttonPress", object(
								"buttonID", bID
						) //JSON: {"buttonPress" : {"buttonID" : "START_SYSTEM" }}
				).end();
				commMngr.sendData(jn);
				hideAllViews();
			}
		}
	};


	/**
	 * Called when a user clicks the connect button to build a new connection with the middleware
	 */
	public void connect(View v){
		this.selectedMode = CommunicationManager.AvailableModes.ADULT;
		if(modeSelection.getCheckedRadioButtonId() == childSelection.getId()){
			selectedMode = CommunicationManager.AvailableModes.CHILD;
		}

		this.selectedMiddleware = CommunicationManager.AvailableMiddlewares.APOLLO;
		if(middlewareSelection.getCheckedRadioButtonId() == ROSSelection.getId()){
			selectedMiddleware = CommunicationManager.AvailableMiddlewares.ROS;
		}

		String ip = ipAddress.getText().toString();

		commMngr.updateConnectionSettings(selectedMode, selectedMiddleware, ip);

		connectBtn.setEnabled(false);
		commMngr.restart();

		if(!commMngr.isInitialized()){
			connectBtn.setEnabled(true);
			connectionStatus.setText("Connection failed...try again");
		} else {
			hideKeyboard(this);
			settingsActive = false;
			settingsView.setVisibility(View.INVISIBLE);
			connectBtn.setEnabled(true);
			showAssignment(new ShowAssignment("init", "Connected to "+selectedMiddleware.toString() +" in "+selectedMode+" mode", "", "", false));
		}

		//go back to fullscreen
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	public void toggleConnectionSettings(View v){
		if(!settingsActive) {
			if(commMngr.isInitialized()){
				connectionStatus.setText("Connected to "+selectedMiddleware.toString() +" in "+selectedMode+" mode");
			}
			hideAllViews();
			setBackgroundColor(new SetBackgroundColor("#ffffff"));
			buttonsView.setVisibility(View.INVISIBLE);
			persistentButtonsView.setVisibility(View.INVISIBLE);
			this.settingsActive = true;
			settingsView.setVisibility(View.VISIBLE);

		} else {
			this.settingsActive = false;
			settingsView.setVisibility(View.INVISIBLE);
			if(commMngr.isInitialized()) {
				showAssignment(new ShowAssignment("init", "Connected to " + selectedMiddleware.toString() + " in " + selectedMode + " mode", "", "", false));
			}
		}
	}

	public static void hideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) {
			view = new View(activity);
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
