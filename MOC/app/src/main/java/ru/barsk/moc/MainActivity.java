package ru.barsk.moc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;
import java.util.Random;

import ru.barsk.databases.*;
import ru.barsk.util.*;
import ru.barsk.view.AlertView;
import ru.barsk.view.StarTextView;

/*
================
TO-DO list розовой пантеры:
1.TODO,
2.TODO,
3.TODO, TODO, TODO, TODO, TODOOOOoooo
================
TO-DO list barsk'а
0. TODO: Адекватное изображение для ship41, ship42 итп.
1. TODO: звуки.
10. TODO: Оптимизировать науку, удалить LongDouble,
 хранить уровень и IT до слудующего уровня а не общее IT.
11. TODO: Использовать фрагменты.
================
Баги:
================
Что можно реализовать в далёком будущем:
0. Заменить простые бонусоы по уровням древом технологий.
1. Реализовать исследование галактики.
10. Добавить артефакты.
11. Улучшить ИИ.
100. Добавить возможность изменять % налога и влияние этого % на мораль.
101. Сделать интерфейс красивее и удобнее.
110. Ввести планетарные бои и бомбардировку планет.
111. Поддержать IOS.
================
*/
 
public final class MainActivity extends Activity {
	
	public static final String KEY_VOLUME = "volume";
	public static final String KEY_VIBRATION = "vibration";
	public static final String KEY_GRAPHICS = "graphics";
	public static final String KEY_SHOW_DAMAGE = "show_damage";
	public static final String KEY_SHOW_EVENTS = "do_show_turn_events";
	public static final String KEY_SLOT_NAME = "slot_";
	public static final String KEY_TURN_NUMBER = "turn_slot_";
	public static final String LOG_TAG = "MainActivity";
	public static final int ID_LAUNCH_SHIPS = 0xfffffffe; //= -2. No real layout may have such id

	public static String[] saveNames;
	public static LinearLayout view_main;
	private int previousLayoutId; /*only used by "coming soon" window*/
	private int currentLayoutId; /*only uses by onBackPressed() method*/
	public static Handler delayer;
	public static long turnLong;
	public static byte galaxySize;
	public static Coordinates selectedStarCoordinates;
	public static Coordinates destinationCoordinates;
	private View[] backup;
	public static StarTextView[][] screenTexts;
	private FrameLayout[][] screenStars;
	private FrameLayout[][] screenShips;
	/* Each star looks like:
	 * <FrameLayout id="screenStar">
	 * * <FrameLayout id="screenShip">
	 * * * <TextView id="screenText"/>
	 * * </FrameLayout>
	 * </FrameLayout>
	 * ScreenText also listens clicks and shows selection.
	 */
	public static Star[][] galaxy;
	public static MainActivity context;
	public static Animation appearance;
	public static Animation disappearance;
	public static Animation appearance_left;
	public static Animation disappearance_left;
	public static Animation appearance_right;
	public static Animation disappearance_right;
	public static Animation appearance_inverted;
	public static Animation disappearance_inverted;
	public static Vibrator vibrator;
	public static byte currentSize;
	public static AbstractAI[] AIs;
	public static ArrayList<TurnEvent> events;
	public static ArrayList<TurnEvent> nextTurnEvents;//?
	private static boolean willPlayAfterGameOver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		delayer = new Handler();
		context = MainActivity.this;
		willPlayAfterGameOver = false;
		appearance = AnimationUtils.loadAnimation(context, R.anim.appearance);
		disappearance = AnimationUtils.loadAnimation(context, R.anim.disappearance);
		appearance_left = AnimationUtils.loadAnimation(context, R.anim.appearance_left);
		disappearance_left = AnimationUtils.loadAnimation(context, R.anim.disappearance_left);
		appearance_right = AnimationUtils.loadAnimation(context, R.anim.appearance_right);
		disappearance_right = AnimationUtils.loadAnimation(context, R.anim.disappearance_right);
		appearance_inverted = AnimationUtils.loadAnimation(context, R.anim.appearance_inverted);
		disappearance_inverted = AnimationUtils.loadAnimation(context, R.anim.disappearance_inverted);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		events = new ArrayList<>();
		nextTurnEvents = new ArrayList<>();
		loadSettings();
		createTables();
		toMainMenu();
		saveNames = new String[5];
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		for (int i = 0; i < saveNames.length; i++) {
			saveNames[i] = preferences.getString(KEY_SLOT_NAME + i, getString(R.string.free_slot));
		}
	}

	@Override
	protected void onStop() {
		//TODO remove this
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		for (int i = 0; i < saveNames.length; i++) {
			editor.putString(KEY_SLOT_NAME + i, saveNames[i]);
		}
		editor.apply();
		super.onStop();
	}

	private void loadSettings() {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		int newVolume = preferences.getInt(KEY_VOLUME, 100);
		boolean newVibration = preferences.getBoolean(KEY_VIBRATION, true);
		byte newGraphics = (byte) preferences.getInt(KEY_GRAPHICS, 3);
		boolean newDoShowDamage = preferences.getBoolean(KEY_SHOW_DAMAGE, true);
		boolean newDoShowTurnEvents = preferences.getBoolean(KEY_SHOW_EVENTS, true);
		Settings.setVolume(newVolume);
		Settings.doVibrate = newVibration;
		Settings.setCombatLength(newGraphics);
		Settings.doShowDamage = newDoShowDamage;
		Settings.doShowTurnEvents = newDoShowTurnEvents;
	}

	public void saveSettings() {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_VOLUME, Settings.getVolume());
		editor.putBoolean(KEY_VIBRATION, Settings.doVibrate);
		editor.putInt(KEY_GRAPHICS, Settings.getCombatLength());
		editor.putBoolean(KEY_SHOW_DAMAGE, Settings.doShowDamage);
		editor.putBoolean(KEY_SHOW_EVENTS, Settings.doShowTurnEvents);
		editor.apply();
	}

	public void toMainMenu() {
		setContentView(R.layout.activity_main);
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		currentLayoutId = R.layout.activity_main;
		final Button singleplayer = (Button) findViewById(R.id.singleplayer);
		final Button help = (Button) findViewById(R.id.help);
		final Button multiplayer = (Button) findViewById(R.id.multiplayer);
		final Button options = (Button) findViewById(R.id.options);
		final Button other_games = (Button) findViewById(R.id.other_games);
		final Button exit = (Button) findViewById(R.id.exit);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				view_main = (LinearLayout) findViewById(R.id.layout_main);
				view_main.startAnimation(disappearance);
				delayer.postDelayed(new Runnable() {
					public void run() {
						switch (v.getId()) {
							case R.id.exit:
								finish();
								break;
							case R.id.options:
								toSettings();
								break;
							case R.id.other_games:
								toOtherGames();
								break;
							case R.id.help:
								toHelpMain();
								break;
							case R.id.singleplayer:
								toNewOrContinueSingleplayer();
								break;
							default:
								previousLayoutId = R.layout.activity_main;
								toComingSoon();
						}
					}
				}, 500);
			}
		};
		singleplayer.setOnClickListener(onClickListener);
		help.setOnClickListener(onClickListener);
		multiplayer.setOnClickListener(onClickListener);
		options.setOnClickListener(onClickListener);
		other_games.setOnClickListener(onClickListener);
		exit.setOnClickListener(onClickListener);
	}

	@Override
	public void onBackPressed() {
		view_main.startAnimation(disappearance);
		delayer.postDelayed(new Runnable() {
			public void run() {
				switch (currentLayoutId) {
					case R.layout.activity_settings:
						toMainMenu();
						break;
					case R.layout.activity_loading:
						toMainMenu();
						break;
					case R.layout.activity_other_games:
						toMainMenu();
						break;
					case R.layout.activity_help_main:
						toMainMenu();
						break;
					case R.layout.activity_load_game:
					case R.layout.activity_new_game:
						toNewOrContinueSingleplayer();
						break;
					case R.layout.activity_save_game:
						toSingleplayer();
						break;
					case R.layout.activity_new_or_continue_singleplayer:
						toMainMenu();
						break;
					case R.layout.activity_help:
						toHelpMain();
						break;
					case R.layout.activity_singleplayer:
						toMainMenu();
						break;
					case R.layout.activity_coming_soon:
						switch (previousLayoutId) {
							case R.layout.activity_help_main:
								toHelpMain();
								break;
							case R.layout.activity_other_games:
								toOtherGames();
								break;
							default:
								toMainMenu();
						}
						break;
					case R.layout.activity_game_over:
						toMainMenu();
						break;
					case ID_LAUNCH_SHIPS:
						findViewById(R.id.turn).setEnabled(true);
						setScreenTextDefaultListeners();
						ViewGroup bottomView = (ViewGroup) findViewById(R.id.bottomView);
						bottomView.removeAllViewsInLayout();
						for (View view : backup) {
							bottomView.addView(view);
						}
						destinationCoordinates = null;
						currentLayoutId = R.layout.activity_singleplayer;
						showGalaxy();
						break;
					default:
						MainActivity.super.onBackPressed();
				}
			}
		}, 500);
	}

	public void toSettings() {
		setContentView(R.layout.activity_settings);
		currentLayoutId = R.layout.activity_settings;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		final Button save = (Button) findViewById(R.id.save_settings);
		final Button back = (Button) findViewById(R.id.back);
		final EditText volumeEditText = (EditText) findViewById(R.id.volumeEditText);
		final SeekBar volumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
		final CheckBox doVibrateCheckBox = (CheckBox) findViewById(R.id.doVibrateCheckBox);
		final CheckBox doShowDamageCheckBox = (CheckBox) findViewById(R.id.doShowDamageCheckBox);
		final CheckBox doShowTurnEventsCheckBox = (CheckBox) findViewById(R.id.doShowTurnEventsCheckBox);
		final RadioButton lengthRadioButton1 = (RadioButton) findViewById(R.id.lengthRadioButton1);
		final RadioButton lengthRadioButton2 = (RadioButton) findViewById(R.id.lengthRadioButton2);
		final RadioButton lengthRadioButton3 = (RadioButton) findViewById(R.id.lengthRadioButton3);
		volumeEditText.setText(String.valueOf(Settings.getVolume()));
		volumeSeekBar.setProgress(Settings.getVolume());
		doVibrateCheckBox.setChecked(Settings.doVibrate);
		if (Settings.getCombatLength() == 3) {
			lengthRadioButton1.setChecked(true);
		}
		else {
			if (Settings.getCombatLength() == 2) {
				lengthRadioButton2.setChecked(true);
			}
			else {
				lengthRadioButton3.setChecked(true);
			}
		}
		doShowDamageCheckBox.setChecked(Settings.doShowDamage);
		doShowTurnEventsCheckBox.setChecked(Settings.doShowTurnEvents);
		OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					volumeEditText.setText(String.valueOf(progress));
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		};
		volumeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		OnEditorActionListener onEditorActionListener = new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				int newProgress;
				try {
					newProgress = Integer.parseInt(volumeEditText.getText().toString());
				}
				catch (NumberFormatException e) {
					newProgress = (int) Double.parseDouble(volumeEditText.getText().toString());
				}
				volumeSeekBar.setProgress(newProgress);
				InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				return true;
			}
		};
		volumeEditText.setOnEditorActionListener(onEditorActionListener);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (v.getId() == R.id.save_settings) {
					Settings.setVolume(volumeEditText.getText().toString());
					Settings.doVibrate = doVibrateCheckBox.isChecked();
					Settings.doShowTurnEvents = doShowTurnEventsCheckBox.isChecked();
					if (lengthRadioButton1.isChecked()) {
						Settings.setCombatLength((byte) 3);
					}
					else {
						if (lengthRadioButton2.isChecked()) {
							Settings.setCombatLength((byte) 2);
						}
						else {
							Settings.setCombatLength((byte) 1);
						}
					}
					Settings.doShowDamage = doShowDamageCheckBox.isChecked();
					saveSettings();
				}
				view_main.startAnimation(disappearance);
				delayer.postDelayed(new Runnable() {
					public void run() {
						toMainMenu();
					}
				}, 500);
			}
		};
		save.setOnClickListener(onClickListener);
		back.setOnClickListener(onClickListener);
	}

	public void toComingSoon() {
		setContentView(R.layout.activity_coming_soon);
		currentLayoutId = R.layout.activity_coming_soon;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		final Button back = (Button) findViewById(R.id.back);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				view_main.startAnimation(disappearance);
				delayer.postDelayed(new Runnable() {
					public void run() {
						switch (previousLayoutId) {
							case R.layout.activity_help_main:
								toHelpMain();
								break;
							case R.layout.activity_other_games:
								toOtherGames();
								break;
							default:
								toMainMenu();
								break;
						}
					}
				}, 500);
			}
		};
		back.setOnClickListener(onClickListener);
	}

	public void toOtherGames() {
		setContentView(R.layout.activity_other_games);
		currentLayoutId = R.layout.activity_other_games;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		final Button MOG = (Button) findViewById(R.id.MOG);
		final Button starDrive = (Button) findViewById(R.id.starDrive);
		final Button back = (Button) findViewById(R.id.back);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				view_main.startAnimation(disappearance);
				delayer.postDelayed(new Runnable() {
					public void run() {
						switch (v.getId()) {
							case R.id.back:
								toMainMenu();
								break;
							default:
								previousLayoutId = R.layout.activity_other_games;
								toComingSoon();
						}
					}
				}, 500);
			}
		};
		MOG.setOnClickListener(onClickListener);
		starDrive.setOnClickListener(onClickListener);
		back.setOnClickListener(onClickListener);
	}

	public void toHelpMain() {
		setContentView(R.layout.activity_help_main);
		currentLayoutId = R.layout.activity_help_main;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		final Button introduction = (Button) findViewById(R.id.help_introduction);
		final Button stars = (Button) findViewById(R.id.help_stars);
		final Button ships = (Button) findViewById(R.id.help_ships);
		final Button economy = (Button) findViewById(R.id.help_economy);
		final Button back = (Button) findViewById(R.id.back);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				view_main.startAnimation(disappearance);
				delayer.postDelayed(new Runnable() {
					public void run() {
						switch (v.getId()) {
							case R.id.back:
								toMainMenu();
								break;
							default:
								toHelp(v.getId());
						}
					}
				}, 500);
			}
		};
		introduction.setOnClickListener(onClickListener);
		stars.setOnClickListener(onClickListener);
		ships.setOnClickListener(onClickListener);
		economy.setOnClickListener(onClickListener);
		back.setOnClickListener(onClickListener);
	}

	public void toHelp(int buttonId) {
		switch (buttonId) {
			case R.id.help_introduction: {
				setContentView(R.layout.activity_help);
				((TextView) findViewById(R.id.helpHeader)).setText(R.string.help_introduction);
				((TextView) findViewById(R.id.helpText)).setText(R.string.help_introduction);
				view_main = (LinearLayout) findViewById(R.id.layout_main);
				view_main.startAnimation(appearance);
				currentLayoutId = R.layout.activity_help;
				final Button back = (Button) findViewById(R.id.back);
				OnClickListener onClickListener = new OnClickListener() {
					@Override
					public void onClick(final View v) {
						view_main.startAnimation(disappearance);
						delayer.postDelayed(new Runnable() {
							public void run() {
								toHelpMain();
							}
						}, 500);
					}
				};
				back.setOnClickListener(onClickListener);
				break;
			}
			case R.id.help_stars: {
				setContentView(R.layout.activity_help);
				((TextView) findViewById(R.id.helpHeader)).setText(R.string.stars);
				((TextView) findViewById(R.id.helpText)).setText(R.string.help_colonies);
				view_main = (LinearLayout) findViewById(R.id.layout_main);
				view_main.startAnimation(appearance);
				currentLayoutId = R.layout.activity_help;
				final Button back = (Button) findViewById(R.id.back);
				OnClickListener onClickListener = new OnClickListener() {
					@Override
					public void onClick(final View v) {
						view_main.startAnimation(disappearance);
						delayer.postDelayed(new Runnable() {
							public void run() {
								toHelpMain();
							}
						}, 500);
					}
				};
				back.setOnClickListener(onClickListener);
				break;
			}
			default: {
				previousLayoutId = R.layout.activity_help_main;
				toComingSoon();
			}
		}
	}

	public void toNewOrContinueSingleplayer() {
		setContentView(R.layout.activity_new_or_continue_singleplayer);
		currentLayoutId = R.layout.activity_new_or_continue_singleplayer;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		final Button continue_game = (Button) findViewById(R.id.continue_game);
		if (galaxy == null || AIs == null || screenShips == null || screenStars == null || screenTexts == null) {
			view_main.removeView(continue_game);
		}
		final Button load_game = (Button) findViewById(R.id.load_game);
		final Button new_game = (Button) findViewById(R.id.new_game);
		final Button back = (Button) findViewById(R.id.back);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				view_main.startAnimation(disappearance);
				delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						switch (v.getId()) {
							case R.id.continue_game:
								toSingleplayer();
								break;
							case R.id.load_game:
								toLoadGame(true);
								break;
							case R.id.new_game:
								toNewGame();
								break;
							default:
								toMainMenu();
						}
					}
				}, 500);
			}
		};
		continue_game.setOnClickListener(onClickListener);
		load_game.setOnClickListener(onClickListener);
		new_game.setOnClickListener(onClickListener);
		back.setOnClickListener(onClickListener);
	}

	public void toNewGame() {
		setContentView(R.layout.activity_new_game);
		currentLayoutId = R.layout.activity_new_game;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		final byte[] size = {8};
		final byte[] opponents = {1};
		final byte[] density = {4};
		final byte[] color = {1};
		final Button create = (Button) findViewById(R.id.create);
		final Button back = (Button) findViewById(R.id.back);
		final TextView size_TextView = (TextView) findViewById(R.id.size_TextView);
		size_TextView.setText("" + size[0]);
		final TextView opponents_TextView = (TextView) findViewById(R.id.opponents_TextView);
		opponents_TextView.setText("" + opponents[0]);
		final TextView density_TextView = (TextView) findViewById(R.id.density_TextView);
		density_TextView.setText("" + density[0]);
		final byte opponentsMaxValue = 6; //Actual maxValue is determined on click.
		final byte opponentsMinValue = 1;
		final byte densityMaxValue = 16;
		final byte densityMinValue = 2;
		final byte densityStep = 2;
		final byte colorMaxValue = 6;
		final byte colorMinValue = 1;
	 	final byte sizeMaxValue = 32;
	 	final byte sizeMinValue = 8;
		final byte sizeStep = 8;
		final Button size_increment = (Button) findViewById(R.id.size_increment);
		final Button size_decrement = (Button) findViewById(R.id.size_decrement);
		final Button opponents_increment = (Button) findViewById(R.id.opponents_increment);
		final Button opponents_decrement = (Button) findViewById(R.id.opponents_decrement);
		final Button density_increment = (Button) findViewById(R.id.density_increment);
		final Button density_decrement = (Button) findViewById(R.id.density_decrement);
		final Button color_increment = (Button) findViewById(R.id.color_increment);
		final Button color_decrement = (Button) findViewById(R.id.color_decrement);
		final ImageView color_ImageView = (ImageView) findViewById(R.id.color_ImageView);
		final Button defaults = (Button) findViewById(R.id.defaults);
		final Button random = (Button) findViewById(R.id.random);
		size_decrement.setEnabled(false);
		opponents_decrement.setEnabled(false);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int id = v.getId();
				if (id == R.id.back || id == R.id.create) {
					view_main.startAnimation(disappearance);
					delayer.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (id == R.id.create) {
								Player.clear();
								Player.colorId = color[0];
								toSingleplayer(size[0], density[0], opponents[0]);
							}
							else {
								toNewOrContinueSingleplayer();
							}
						}
					}, 500);
				}
				else {
					switch (id) {
						case R.id.size_increment: {
							size[0] += sizeStep;
							size_TextView.setText("" + size[0]);
							size_decrement.setEnabled(true);
							size_increment.setEnabled(size[0] < sizeMaxValue);
							opponents_increment.setEnabled(opponents[0] < opponentsMaxValue);
							break;
						}
						case R.id.size_decrement: {
							size[0] -= sizeStep;
							size_TextView.setText("" + size[0]);
							size_decrement.setEnabled(size[0] > sizeMinValue);
							size_increment.setEnabled(true);
							if (size[0] == 8 && opponents[0] >= 3) {
								opponents[0] = 3;
								opponents_TextView.setText("3");
								opponents_increment.setEnabled(false);
							}
							break;
						}
						case R.id.density_increment: {
							density[0] += densityStep;
							density_TextView.setText("" + density[0]);
							density_increment.setEnabled(density[0] < densityMaxValue);
							density_decrement.setEnabled(true);
							break;
						}
						case R.id.density_decrement: {
							density[0] -= densityStep;
							density_TextView.setText("" + density[0]);
							density_increment.setEnabled(true);
							density_decrement.setEnabled(density[0] > densityMinValue);
							break;
						}
						case R.id.opponents_increment: {
							int actualOpponentsMaxValue;
							if (size[0] == sizeMinValue) {
								actualOpponentsMaxValue = 3;
							}
							else {
								actualOpponentsMaxValue = opponentsMaxValue;
							}
							opponents[0]++;
							opponents_TextView.setText("" + opponents[0]);
							opponents_increment.setEnabled(opponents[0] < actualOpponentsMaxValue);
							opponents_decrement.setEnabled(true);
							break;
						}
						case R.id.opponents_decrement: {
							opponents[0]--;
							opponents_TextView.setText("" + opponents[0]);
							opponents_increment.setEnabled(true);
							opponents_decrement.setEnabled(opponents[0] > opponentsMinValue);
							break;
						}
						case R.id.color_increment: {
							if (color[0] < colorMaxValue) {
								color[0]++;
							}
							else {
								color[0] = colorMinValue;
							}
							int colorValue = context.getColor(color[0]);
							color_ImageView.setBackgroundColor(colorValue);
							break;
						}
						case R.id.color_decrement: {
							if (color[0] > colorMinValue) {
								color[0]--;
							}
							else {
								color[0] = colorMaxValue;
							}
							int colorValue = context.getColor(color[0]);
							color_ImageView.setBackgroundColor(colorValue);
							break;
						}
						case R.id.defaults: {
							size[0] = 8;
							opponents[0] = 1;
							density[0] = 4;
							color[0] = 1;
							size_TextView.setText(String.valueOf(size[0]));
							opponents_TextView.setText(String.valueOf(opponents[0]));
							density_TextView.setText(String.valueOf(density[0]));
							color_ImageView.setBackgroundColor(getColor(color[0]));
							size_increment.setEnabled(true);
							size_decrement.setEnabled(false);
							opponents_increment.setEnabled(true);
							opponents_decrement.setEnabled(false);
							density_increment.setEnabled(true);
							density_decrement.setEnabled(true);
							break;
						}
						case R.id.random: {
							size[0] = (byte) ((int) (Math.random() * ((sizeMaxValue - sizeMinValue) / sizeStep + 1)) * sizeStep + sizeMinValue);
							size_TextView.setText(String.valueOf(size[0]));
							size_increment.setEnabled(size[0] < sizeMaxValue);
							size_decrement.setEnabled(size[0] > sizeMinValue);
							if (size[0] == sizeMinValue) {
								opponents[0] = (byte) ((int) (Math.random() * (3 - opponentsMinValue + 1)) + opponentsMinValue);
								opponents_increment.setEnabled(opponents[0] < 3);
							}
							else {
								opponents[0] = (byte) ((int) (Math.random() * (opponentsMaxValue - opponentsMinValue + 1)) + opponentsMinValue);
								opponents_increment.setEnabled(opponents[0] < opponentsMaxValue);
							}
							opponents_TextView.setText(String.valueOf(opponents[0]));
							opponents_decrement.setEnabled(opponents[0] > opponentsMinValue);
							density[0] = (byte) ((int) (Math.random() * ((densityMaxValue - densityMinValue) / densityStep + 1)) * densityStep + densityMinValue);
							density_TextView.setText(String.valueOf(density[0]));
							density_increment.setEnabled(density[0] < densityMaxValue);
							density_decrement.setEnabled(density[0] > densityMinValue);
							color[0] = (byte) ((int) (Math.random() * (colorMaxValue - colorMinValue + 1)) + colorMinValue);
							color_ImageView.setBackgroundColor(getColor(color[0]));
							break;
						}
						default: Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		size_increment.setOnClickListener(onClickListener);
		size_decrement.setOnClickListener(onClickListener);
		opponents_increment.setOnClickListener(onClickListener);
		opponents_decrement.setOnClickListener(onClickListener);
		density_increment.setOnClickListener(onClickListener);
		density_decrement.setOnClickListener(onClickListener);
		color_increment.setOnClickListener(onClickListener);
		color_decrement.setOnClickListener(onClickListener);
		create.setOnClickListener(onClickListener);
		back.setOnClickListener(onClickListener);
		defaults.setOnClickListener(onClickListener);
		random.setOnClickListener(onClickListener);
	}
	
	public void toLoadGame(boolean shouldAppear) {
		setContentView(R.layout.activity_load_game);
		currentLayoutId = R.layout.activity_load_game;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		if (shouldAppear) {
			view_main.startAnimation(appearance);
		}
		Button back = (Button) findViewById(R.id.back);
		Button slot1 = (Button) findViewById(R.id.slot1);
		Button slot2 = (Button) findViewById(R.id.slot2);
		Button slot3 = (Button) findViewById(R.id.slot3);
		Button slot4 = (Button) findViewById(R.id.slot4);
		Button slot5 = (Button) findViewById(R.id.slot5);
		Button slot1_info = (Button) findViewById(R.id.slot1_info);
		Button slot2_info = (Button) findViewById(R.id.slot2_info);
		Button slot3_info = (Button) findViewById(R.id.slot3_info);
		Button slot4_info = (Button) findViewById(R.id.slot4_info);
		Button slot5_info = (Button) findViewById(R.id.slot5_info);
		Button slot1_delete = (Button) findViewById(R.id.slot1_delete);
		Button slot2_delete = (Button) findViewById(R.id.slot2_delete);
		Button slot3_delete = (Button) findViewById(R.id.slot3_delete);
		Button slot4_delete = (Button) findViewById(R.id.slot4_delete);
		Button slot5_delete = (Button) findViewById(R.id.slot5_delete);
		View.OnClickListener onButtonClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int id = v.getId();
				if (id == R.id.back) {
					view_main.startAnimation(disappearance);
					delayer.postDelayed(new Runnable() {
						@Override
						public void run() {
							toNewOrContinueSingleplayer();
						}
					}, 500);
				}
				else {
					switch (id) {
						case R.id.slot1_info:
							makeInfo(1);
							break;
						case R.id.slot2_info:
							makeInfo(2);
							break;
						case R.id.slot3_info:
							makeInfo(3);
							break;
						case R.id.slot4_info:
							makeInfo(4);
							break;
						case R.id.slot5_info:
							makeInfo(5);
							break;
						case R.id.slot1_delete:
							clear(1, false);
							break;
						case R.id.slot2_delete:
							clear(2, false);
							break;
						case R.id.slot3_delete:
							clear(3, false);
							break;
						case R.id.slot4_delete:
							clear(4, false);
							break;
						case R.id.slot5_delete:
							clear(5, false);
							break;
						case R.id.slot1:
							load(1);
							break;
						case R.id.slot2:
							load(2);
							break;
						case R.id.slot3:
							load(3);
							break;
						case R.id.slot4:
							load(4);
							break;
						case R.id.slot5:
							load(5);
							break;
						default:
							Toast.makeText(context, "unknown button", Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		boolean isEmpty1 = isEmpty(1);
		boolean isEmpty2 = isEmpty(2);
		boolean isEmpty3 = isEmpty(3);
		boolean isEmpty4 = isEmpty(4);
		boolean isEmpty5 = isEmpty(5);
		if(isEmpty1) saveNames[0] = getString(R.string.free_slot);
		if(isEmpty2) saveNames[1] = getString(R.string.free_slot);
		if(isEmpty3) saveNames[2] = getString(R.string.free_slot);
		if(isEmpty4) saveNames[3] = getString(R.string.free_slot);
		if(isEmpty5) saveNames[4] = getString(R.string.free_slot);
		slot1.setText(saveNames[0]);
		slot2.setText(saveNames[1]);
		slot3.setText(saveNames[2]);
		slot4.setText(saveNames[3]);
		slot5.setText(saveNames[4]);
		back.setOnClickListener(onButtonClickListener);
		slot1.setOnClickListener(onButtonClickListener);
		slot2.setOnClickListener(onButtonClickListener);
		slot3.setOnClickListener(onButtonClickListener);
		slot4.setOnClickListener(onButtonClickListener);
		slot5.setOnClickListener(onButtonClickListener);
		slot1_info.setOnClickListener(onButtonClickListener);
		slot2_info.setOnClickListener(onButtonClickListener);
		slot3_info.setOnClickListener(onButtonClickListener);
		slot4_info.setOnClickListener(onButtonClickListener);
		slot5_info.setOnClickListener(onButtonClickListener);
		slot1_delete.setOnClickListener(onButtonClickListener);
		slot2_delete.setOnClickListener(onButtonClickListener);
		slot3_delete.setOnClickListener(onButtonClickListener);
		slot4_delete.setOnClickListener(onButtonClickListener);
		slot5_delete.setOnClickListener(onButtonClickListener);
		slot1.setEnabled(!isEmpty1);
		slot2.setEnabled(!isEmpty2);
		slot3.setEnabled(!isEmpty3);
		slot4.setEnabled(!isEmpty4);
		slot5.setEnabled(!isEmpty5);
		slot1_delete.setEnabled(!isEmpty1);
		slot2_delete.setEnabled(!isEmpty2);
		slot3_delete.setEnabled(!isEmpty3);
		slot4_delete.setEnabled(!isEmpty4);
		slot5_delete.setEnabled(!isEmpty5);
	}

	public void load(int index) {
		StarsSQLHelper starsHelper = new StarsSQLHelper(context, index);
		AIsSQLHelper aisHelper = new AIsSQLHelper(context, index);
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		turnLong = preferences.getLong(KEY_TURN_NUMBER + index, -1);
		galaxySize = (byte) starsHelper.getGalaxySize();
		selectedStarCoordinates = new Coordinates((byte)0, (byte) 0);
		galaxy = Tools.adaptGalaxy(starsHelper.selectAll(), galaxySize);
		Sectors.setGalaxy(galaxy);
		ArrayList<AbstractAI> savedAIs = aisHelper.selectAll();
		savedAIs.remove(0); //removes void
		AbstractAI player = savedAIs.get(0);
		Player.colorId = player.colorId;
		Player.treasury = player.treasury;
		Player.IT = player.IT;
		savedAIs.remove(0); //removes player
		AIs = savedAIs.toArray(new AbstractAI[savedAIs.size()]);
		events = new ArrayList<>();
		nextTurnEvents = new ArrayList<>();
		willPlayAfterGameOver = false;
		toSingleplayer();
	}
	
	/**
	 * @param type : type of star. Determines color and size.
	 * @param starSize : size of sector. Determines size of drawable.
	 * @return : id of drawable matching declared parameters.
	 */
	public static int getStarDrawableId(byte type, byte starSize) {
		//resource name is formed like this: type<colorCode><sizeCode>
		int starCode = type * 10 + starSize;
		switch (starCode) {
			case 0: return R.drawable.type00;
			case 1: return R.drawable.type01;
			case 2: return R.drawable.type02;
			case 10: return R.drawable.type10;
			case 11: return R.drawable.type11;
			case 12: return R.drawable.type12;
			case 20: return R.drawable.type20;
			case 21: return R.drawable.type21;
			case 22: return R.drawable.type22;
			case 30: return R.drawable.type30;
			case 31: return R.drawable.type31;
			case 32: return R.drawable.type32;
			case 40: return R.drawable.type40;
			case 41: return R.drawable.type41;
			case 42: return R.drawable.type42;
			case 50: return R.drawable.type50;
			case 51: return R.drawable.type51;
			case 52: return R.drawable.type52;
			case 60: return R.drawable.type60;
			case 61: return R.drawable.type61;
			case 62: return R.drawable.type62;
			case 70: return R.drawable.type70;
			case 71: return R.drawable.type71;
			case 72: return R.drawable.type72;
		}
		return -1;
	}

	/**
	 * @param colorId color of ship.
	 * @param nextToStar if the ship is on orbit.
	 * @param shipImageSize size of ship image.
	 * @return drawable matching named params.
	 * name is formed like this:
	 * shipImage[color_code][size_code][1, if nextToStar, 0 otherwise]
	 */
	public static int getShipImageById(byte colorId, byte shipImageSize, boolean nextToStar) {
		int shipId;
		shipId = colorId * 100 + shipImageSize * 10 + (nextToStar? 1: 0);
		switch (shipId) {
			case 0: return R.drawable.ship_image000;
			case 1: return R.drawable.ship_image001;
			case 10: return R.drawable.ship_image010;
			case 11: return R.drawable.ship_image011;
			case 20: return R.drawable.ship_image020;
			case 21: return R.drawable.ship_image021;
			case 100: return R.drawable.ship_image100;
			case 101: return R.drawable.ship_image101;
			case 110: return R.drawable.ship_image110;
			case 111: return R.drawable.ship_image111;
			case 120: return R.drawable.ship_image120;
			case 121: return R.drawable.ship_image121;
			case 200: return R.drawable.ship_image200;
			case 201: return R.drawable.ship_image201;
			case 210: return R.drawable.ship_image210;
			case 211: return R.drawable.ship_image211;
			case 220: return R.drawable.ship_image220;
			case 221: return R.drawable.ship_image221;
			case 300: return R.drawable.ship_image300;
			case 301: return R.drawable.ship_image301;
			case 310: return R.drawable.ship_image310;
			case 311: return R.drawable.ship_image311;
			case 320: return R.drawable.ship_image320;
			case 321: return R.drawable.ship_image321;
			case 400: return R.drawable.ship_image400;
			case 401: return R.drawable.ship_image401;
			case 410: return R.drawable.ship_image410;
			case 411: return R.drawable.ship_image411;
			case 420: return R.drawable.ship_image420;
			case 421: return R.drawable.ship_image421;
			case 500: return R.drawable.ship_image500;
			case 501: return R.drawable.ship_image501;
			case 510: return R.drawable.ship_image510;
			case 511: return R.drawable.ship_image511;
			case 520: return R.drawable.ship_image520;
			case 521: return R.drawable.ship_image521;
			case 600: return R.drawable.ship_image600;
			case 601: return R.drawable.ship_image601;
			case 610: return R.drawable.ship_image610;
			case 611: return R.drawable.ship_image611;
			case 620: return R.drawable.ship_image620;
			case 621: return R.drawable.ship_image621;
			case 700: return R.drawable.ship_image700;
			case 701: return R.drawable.ship_image701;
			case 710: return R.drawable.ship_image710;
			case 711: return R.drawable.ship_image711;
			case 720: return R.drawable.ship_image720;
			case 721: return R.drawable.ship_image721;
			default: return -1;
		}
	}

	public int getColor(byte colorId) {
		switch (colorId) {
			case 1: return ContextCompat.getColor(context, R.color.owner_color_1);
			case 2: return ContextCompat.getColor(context, R.color.owner_color_2);
			case 3: return ContextCompat.getColor(context, R.color.owner_color_3);
			case 4: return ContextCompat.getColor(context, R.color.owner_color_4);
			case 5: return ContextCompat.getColor(context, R.color.owner_color_5);
			case 6: return ContextCompat.getColor(context, R.color.owner_color_6);
			case 7: return ContextCompat.getColor(context, R.color.owner_color_7);
			default: return ContextCompat.getColor(context, R.color.owner_color_0);
		}
	}

	public static int getShipDrawableId(byte type, byte colorCode) {
		//Is it possible to use android:drawableTint="..."?
		byte shipCode = (byte) (type * 10 + colorCode);
		switch (shipCode) {
			case 0: return R.drawable.ship_clear;
			case 1: return R.drawable.ship01;
			case 2: return R.drawable.ship02;
			case 3: return R.drawable.ship03;
			case 4: return R.drawable.ship04;
			case 5: return R.drawable.ship05;
			case 6: return R.drawable.ship06;
			case 7: return R.drawable.ship07;
			case 11: return R.drawable.ship11;
			case 12: return R.drawable.ship12;
			case 13: return R.drawable.ship13;
			case 14: return R.drawable.ship14;
			case 15: return R.drawable.ship15;
			case 16: return R.drawable.ship16;
			case 17: return R.drawable.ship17;
			case 21: return R.drawable.ship21;
			case 22: return R.drawable.ship22;
			case 23: return R.drawable.ship23;
			case 24: return R.drawable.ship24;
			case 25: return R.drawable.ship25;
			case 26: return R.drawable.ship26;
			case 27: return R.drawable.ship27;
			case 31: return R.drawable.ship31;
			case 32: return R.drawable.ship32;
			case 33: return R.drawable.ship33;
			case 34: return R.drawable.ship34;
			case 35: return R.drawable.ship35;
			case 36: return R.drawable.ship36;
			case 37: return R.drawable.ship37;
			case 41: return R.drawable.ship41;
			case 42: return R.drawable.ship42;
			case 43: return R.drawable.ship43;
			case 44: return R.drawable.ship44;
			case 45: return R.drawable.ship45;
			case 46: return R.drawable.ship46;
			case 47: return R.drawable.ship47;
			case 51: return R.drawable.ship51;
			case 52: return R.drawable.ship52;
			case 53: return R.drawable.ship53;
			case 54: return R.drawable.ship54;
			case 55: return R.drawable.ship55;
			case 56: return R.drawable.ship56;
			case 57: return R.drawable.ship57;
		}
		return -1;
	}

	public static int getSelectionDrawableId(byte type, byte size) {
		/* type = {
		 * 0, if none
		 * 1, if from
		 * 2, if to
		 * }
		 */
		byte code = (byte) (10 * type + size);
		switch (code) {
			case 0: return R.drawable.target_no0;
			case 1: return R.drawable.target_no1;
			case 2: return R.drawable.target_no2;
			case 10: return R.drawable.target_from0;
			case 11: return R.drawable.target_from1;
			case 12: return R.drawable.target_from2;
			case 20: return R.drawable.target_to0;
			case 21: return R.drawable.target_to1;
			case 22: return R.drawable.target_to2;
			default: return -1;
		}
	}

	public void showGalaxy() {
		int newParam;
		int newTextSize;
		if (currentSize == 0) {
			newParam = 43;
			newTextSize = 8;
		}
		else {
			if (currentSize == 1) {
				newParam = 77;
				newTextSize = 10;
			}
			else {
				newParam = 143;
				newTextSize = 12;
			}
		}
		newParam = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newParam, getResources().getDisplayMetrics()));
		for(int x = 0; x < galaxySize; x++) {
			for (byte y = 0; y < galaxySize; y++) {
				byte starOwnerId = galaxy[x][y].getOwnerId();
				byte textColorId;
				byte shipOwnerId = galaxy[x][y].getShipsOnOrbitOwnerId();
				byte shipColorId;
				if (starOwnerId == Player.ID) {
					textColorId = Player.colorId;
				}
				else {
					if (starOwnerId == 0) {
						textColorId = 0;
					}
					else {
						textColorId = AIs[starOwnerId - 2].colorId;
					}
				}
				if (shipOwnerId == Player.ID) {
					shipColorId = Player.colorId;
				}
				else {
					if (shipOwnerId == 0) {
						shipColorId = 0;
					}
					else {
						shipColorId = AIs[shipOwnerId - 2].colorId;
					}
				}
				int starDrawableId = getStarDrawableId(galaxy[x][y].type, currentSize);
				int shipImageId = getShipImageById(shipColorId, currentSize, galaxy[x][y].type != 0);
				int selectionDrawableId = getSelectionDrawableId((byte) 0, currentSize);
				screenStars[x][y].setBackgroundResource(starDrawableId);
				screenShips[x][y].setBackgroundResource(shipImageId);
				screenTexts[x][y].setCompoundDrawablesWithIntrinsicBounds(0, selectionDrawableId, 0, 0);
				screenTexts[x][y].setWidth(newParam);
				screenTexts[x][y].setHeight(newParam);
				screenTexts[x][y].setTextSize(TypedValue.COMPLEX_UNIT_SP, newTextSize);
				int colorValue = getColor(textColorId);
				screenTexts[x][y].setTextColor(colorValue);
				screenTexts[x][y].setText(galaxy[x][y].name);
			}
		}
		screenTexts[selectedStarCoordinates.x][selectedStarCoordinates.y].setCompoundDrawablesWithIntrinsicBounds(0, getSelectionDrawableId((byte) 1, currentSize), 0, 0);
		if (destinationCoordinates != null) {
			screenTexts[destinationCoordinates.x][destinationCoordinates.y].setCompoundDrawablesWithIntrinsicBounds(0, getSelectionDrawableId((byte) 2, currentSize), 0, 0);
		}
	}

	public void toSingleplayer(byte size, byte density, byte opponents) {
		galaxySize = size;
		setContentView(R.layout.activity_loading);
		currentLayoutId = R.layout.activity_loading;
		events.clear();
		AsyncGenerator task = new AsyncGenerator(size, density, (byte) (opponents + 1));
		task.execute(
				getResources().getStringArray(R.array.star_names),
				getResources().getStringArray(R.array.ai_names),
				new String[] {String.valueOf(Player.colorId)}
		);
	}

	public void toSingleplayer() {
		currentLayoutId = R.layout.activity_singleplayer;
		setContentView(R.layout.activity_singleplayer);
		createScreen();
		selectedStarCoordinates = new Coordinates((byte) 0, (byte) 0);
		destinationCoordinates = null;
		currentSize = (byte) 0;
		showGalaxy();
		final Button turnButton = (Button) findViewById(R.id.turn);
		final Button menu = (Button) findViewById(R.id.menu);
		final TextView turnNumber = (TextView) findViewById(R.id.turnNumber);
		final Button increase = (Button) findViewById(R.id.buttonIncrease);
		final Button decrease = (Button) findViewById(R.id.buttonDecrease);
		final ImageView starView = (ImageView) findViewById(R.id.starView);
		final TextView money = (TextView) findViewById(R.id.money);
		final TextView science = (TextView) findViewById(R.id.science);
		final Button colonize = (Button) findViewById(R.id.colonize);
		final ImageView map = (ImageView) findViewById(R.id.map);
		final Button save = (Button) findViewById(R.id.save);
		final AlertView alerts = (AlertView) findViewById(R.id.alerts);
		invalidateScreen(true);
		final OnClickListener onButtonClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Только для тех кнопок, которые постоянно на экране.
				switch (v.getId()) {
					case R.id.turn: {
						i("starting turn " + String.valueOf(turnLong) + "...");
						turn();
						turnNumber.setText(getString(R.string.turn) + " " + turnLong);
						i("turn " + String.valueOf(turnLong - 1) + " is over!");
						break;
					}
					case R.id.menu: {
                        //TODO: toGameMenu();
						toMainMenu();
						break;
					}
					case R.id.buttonIncrease: {
						if (currentSize < 2) {
							currentSize++;
							showGalaxy();
						}
						break;
					}
					case R.id.buttonDecrease: {
						if (currentSize > 0) {
							currentSize--;
							showGalaxy();
						}
						break;
					}
					case R.id.money: {
						Intent intent = new Intent(context, EconomyActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.science: {
						Intent intent = new Intent(context, ScienceActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.colonize: {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						String title = getString(R.string.colonize2);
						String message = getString(R.string.colonize3);
						String yes = getString(R.string.yes);
						String no = getString(R.string.no);
						DialogInterface.OnClickListener onNoListener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						};
						DialogInterface.OnClickListener onYesListener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								byte ownerId = galaxy[selectedStarCoordinates.x][selectedStarCoordinates.y].getShipsOnOrbitOwnerId();
								galaxy[selectedStarCoordinates.x][selectedStarCoordinates.y].removeOneColonizer();
								galaxy[selectedStarCoordinates.x][selectedStarCoordinates.y].colonize(ownerId);
								invalidateScreen(true);
							}
						};
						builder.setTitle(title);
						builder.setMessage(message);
						builder.setPositiveButton(yes, onYesListener);
						builder.setNegativeButton(no, onNoListener);
						builder.setCancelable(true);
						AlertDialog alertDialog = builder.create();
						alertDialog.show();
						break;
					}
					case R.id.map: {
						Intent intent = new Intent(context, MapViewActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.save: {
						toSaveGame(true);
						break;
					}
					case R.id.alerts: {
						Intent intent = new Intent(context, EventsViewActivity.class);
						startActivity(intent);
						break;
					}
					default:
						Toast.makeText(context, "Unknown button.", Toast.LENGTH_SHORT).show();
				}
			}
		};
		turnButton.setOnClickListener(onButtonClickListener);
		menu.setOnClickListener(onButtonClickListener);
		increase.setOnClickListener(onButtonClickListener);
		decrease.setOnClickListener(onButtonClickListener);
		money.setOnClickListener(onButtonClickListener);
		colonize.setOnClickListener(onButtonClickListener);
		science.setOnClickListener(onButtonClickListener);
		map.setOnClickListener(onButtonClickListener);
		save.setOnClickListener(onButtonClickListener);
		alerts.setOnClickListener(onButtonClickListener);
		starView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				byte x = selectedStarCoordinates.x;
				byte y = selectedStarCoordinates.y;
				if (galaxy[x][y].type != 0 && galaxy[x][y].getOwnerId() == Player.ID) {
					Intent intent = new Intent(context, StarViewActivity.class);
					startActivity(intent);
				}
			}
		});
	}
	
	public void toSaveGame(boolean shouldAppear) {
		setContentView(R.layout.activity_save_game);
		currentLayoutId = R.layout.activity_save_game;
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		if (shouldAppear) {
			view_main.startAnimation(appearance);
		}
		Button back = (Button) findViewById(R.id.back);
		Button slot1 = (Button) findViewById(R.id.slot1);
		Button slot2 = (Button) findViewById(R.id.slot2);
		Button slot3 = (Button) findViewById(R.id.slot3);
		Button slot4 = (Button) findViewById(R.id.slot4);
		Button slot5 = (Button) findViewById(R.id.slot5);
		Button slot1_info = (Button) findViewById(R.id.slot1_info);
		Button slot2_info = (Button) findViewById(R.id.slot2_info);
		Button slot3_info = (Button) findViewById(R.id.slot3_info);
		Button slot4_info = (Button) findViewById(R.id.slot4_info);
		Button slot5_info = (Button) findViewById(R.id.slot5_info);
		Button slot1_delete = (Button) findViewById(R.id.slot1_delete);
		Button slot2_delete = (Button) findViewById(R.id.slot2_delete);
		Button slot3_delete = (Button) findViewById(R.id.slot3_delete);
		Button slot4_delete = (Button) findViewById(R.id.slot4_delete);
		Button slot5_delete = (Button) findViewById(R.id.slot5_delete);
		View.OnClickListener onButtonClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int id = v.getId();
				if (id == R.id.back) {
					onBackPressed();
				}
				else {
					switch (id) {
						case R.id.slot1:
							save(1);
							break;
						case R.id.slot2:
							save(2);
							break;
						case R.id.slot3:
							save(3);
							break;
						case R.id.slot4:
							save(4);
							break;
						case R.id.slot5:
							save(5);
							break;
						case R.id.slot1_delete:
							clear(1, true);
							break;
						case R.id.slot2_delete:
							clear(2, true);
							break;
						case R.id.slot3_delete:
							clear(3, true);
							break;
						case R.id.slot4_delete:
							clear(4, true);
							break;
						case R.id.slot5_delete:
							clear(5, true);
							break;
						case R.id.slot1_info:
							makeInfo(1);
							break;
						case R.id.slot2_info:
							makeInfo(2);
							break;
						case R.id.slot3_info:
							makeInfo(3);
							break;
						case R.id.slot4_info:
							makeInfo(4);
							break;
						case R.id.slot5_info:
							makeInfo(5);
							break;
						default:
							Toast.makeText(context, "Error: unknown button", Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		back.setOnClickListener(onButtonClickListener);
		slot1.setOnClickListener(onButtonClickListener);
		slot2.setOnClickListener(onButtonClickListener);
		slot3.setOnClickListener(onButtonClickListener);
		slot4.setOnClickListener(onButtonClickListener);
		slot5.setOnClickListener(onButtonClickListener);
		slot1_info.setOnClickListener(onButtonClickListener);
		slot2_info.setOnClickListener(onButtonClickListener);
		slot3_info.setOnClickListener(onButtonClickListener);
		slot4_info.setOnClickListener(onButtonClickListener);
		slot5_info.setOnClickListener(onButtonClickListener);
		slot1_delete.setOnClickListener(onButtonClickListener);
		slot2_delete.setOnClickListener(onButtonClickListener);
		slot3_delete.setOnClickListener(onButtonClickListener);
		slot4_delete.setOnClickListener(onButtonClickListener);
		slot5_delete.setOnClickListener(onButtonClickListener);
		boolean isEmpty1 = isEmpty(1);
		boolean isEmpty2 = isEmpty(2);
		boolean isEmpty3 = isEmpty(3);
		boolean isEmpty4 = isEmpty(4);
		boolean isEmpty5 = isEmpty(5);
		if(isEmpty1) saveNames[0] = getString(R.string.free_slot);
		if(isEmpty2) saveNames[1] = getString(R.string.free_slot);
		if(isEmpty3) saveNames[2] = getString(R.string.free_slot);
		if(isEmpty4) saveNames[3] = getString(R.string.free_slot);
		if(isEmpty5) saveNames[4] = getString(R.string.free_slot);
		slot1.setText(saveNames[0]);
		slot2.setText(saveNames[1]);
		slot3.setText(saveNames[2]);
		slot4.setText(saveNames[3]);
		slot5.setText(saveNames[4]);
		slot1.setEnabled(isEmpty1);
		slot2.setEnabled(isEmpty2);
		slot3.setEnabled(isEmpty3);
		slot4.setEnabled(isEmpty4);
		slot5.setEnabled(isEmpty5);
		slot1_delete.setEnabled(!isEmpty1);
		slot2_delete.setEnabled(!isEmpty2);
		slot3_delete.setEnabled(!isEmpty3);
		slot4_delete.setEnabled(!isEmpty4);
		slot5_delete.setEnabled(!isEmpty5);
	}
	
	public void clear(final int index, final boolean calledFromSaveGameMethod) {
		String title = saveNames[index - 1];
		String message = getString(R.string.delete2);
		String yes = getString(R.string.yes);
		String no = getString(R.string.no);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					StarsSQLHelper starsHelper = new StarsSQLHelper(context, index);
					starsHelper.deleteAll();
					AIsSQLHelper aisHelper = new AIsSQLHelper(context, index);
					try {
						aisHelper.deleteAll();
					}
					catch (Exception ignored) {}
					SharedPreferences preferences = getPreferences(MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.remove(KEY_TURN_NUMBER + index);
					editor.apply();
				}
				dialog.cancel();
				if (calledFromSaveGameMethod) {
					toSaveGame(false);
				}
				else {
					toLoadGame(false);
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(yes, listener);
		builder.setNegativeButton(no, listener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void makeInfo(int index) {
		StarsSQLHelper starsHelper = new StarsSQLHelper(context, index);
		String title = saveNames[index - 1];
		String message;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		int galaxySize = starsHelper.getGalaxySize();
		if (galaxySize != 0) {
			int numberOfStars = starsHelper.getStarCount();
			int enemyCount = starsHelper.getEnemyCount();
			message = getString(R.string.size) + " " + galaxySize + "\n" +
					getString(R.string.number_of_stars) + " " + numberOfStars + "\n" +
					getString(R.string.enemy_count) + " " + enemyCount;
			builder.setMessage(message);
		}
		String ok = getString(R.string.ok);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		};
		builder.setTitle(title);
		builder.setPositiveButton(ok, listener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void save(final int index) {
		String title = getString(R.string.save2);
		String message = getString(R.string.enter_name_hint);
		String yes = getString(R.string.yes);
		String no = getString(R.string.no);
		final EditText view = new EditText(context);
		view.setText(getString(R.string.slot) + " " + index);
		view.setId(R.id.save_name_editText);
		AlertDialog.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == AlertDialog.BUTTON_POSITIVE) {
					String name = view.getText().toString();
					if (name.equals("") || name.equals(getString(R.string.free_slot))) {
						Toast.makeText(context, "Invalid name!", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
					else {
						saveNames[index - 1] = name;
						SharedPreferences preferences = getPreferences(MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putLong(KEY_TURN_NUMBER + index, turnLong);
						editor.apply();
						StarsSQLHelper starsHelper = new StarsSQLHelper(context, index);
						AIsSQLHelper aisHelper = new AIsSQLHelper(context, index);
						starsHelper.insertAll(galaxy);
						aisHelper.insertVoid();
						aisHelper.insertPlayer();
						aisHelper.insertAll(AIs);
						dialog.cancel();
						view_main.startAnimation(disappearance);
						delayer.postDelayed(new Runnable() {
							@Override
							public void run() {
								toSingleplayer();
							}
						}, 500);
					}
				}
				else {
					dialog.cancel();
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setView(view);
		builder.setPositiveButton(yes, listener);
		builder.setNegativeButton(no, listener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public boolean isEmpty(int databaseIndex) {
		StarsSQLHelper starsHelper = new StarsSQLHelper(context, databaseIndex);
		return starsHelper.isEmpty();
	}

	@Deprecated
	public void createTables() {
		for (int i = 0; i <= 5; i++) {
			StarsSQLHelper h = new StarsSQLHelper(context, i);
			h.onCreate(h.getWritableDatabase());
		}
	}

	private void turn() {
		events.clear();
		events.addAll(nextTurnEvents);
		nextTurnEvents.clear();
		turnLong++;
		destinationCoordinates = null;
		for (AbstractAI ai: AIs) {
			if (isAlive(ai.ID)) {
				ai.manageGalaxy(galaxy);
			}
		}
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				star.onTurnPassed();
			}
		}
		//Move ships, newPositions have been determined in star.onTurnPassed().
		for (int x = 0; x < galaxySize; x++) {
			for (int y = 0; y < galaxySize; y++) {
				int orbitSize = galaxy[x][y].orbit.size();
				for (int i = 0; i < orbitSize; i++) {
					Ship ship = galaxy[x][y].orbit.get(i);
					if (!ship.position.equals(ship.newPosition)) {
						galaxy[x][y].orbit.set(i, null);
						byte newX = ship.newPosition.x;
						byte newY = ship.newPosition.y;
						galaxy[newX][newY].orbit.add(ship);
						ship.position = ship.newPosition;
					}
				}
				galaxy[x][y].simplifyOrbit();
			}
		}
		invalidateTreasuries();
		addITs();
		Random r = new Random();
		if (Player.treasury < 0) {
			Tools.notifyEconomyCollapsing();
			if (Player.treasury < -100 && r.nextBoolean()) {
				Tools.removeSomething(galaxy, Player.ID);
			}
		}
		invalidateScreen(true);
		callForBattles();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		String title = getString(R.string.turn) + " " + turnLong;
		String ok = getString(R.string.ok);
		DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!willPlayAfterGameOver) {
					if (!isAlive(Player.ID)) {
						onOnlyOnePlayerLeft(false);
					}
					else {
						if (!anyEnemyAlive()) {
							onOnlyOnePlayerLeft(true);
						}
					}
				}
				if (Settings.doShowTurnEvents) {
					Intent intent = new Intent(context, EventsViewActivity.class);
					startActivity(intent);
				}
				dialog.cancel();
			}
		};
		builder.setTitle(title);
		builder.setPositiveButton(ok, onClickListener);
		builder.setCancelable(false);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	/**
	 * Неявная рекурсия.
	 * Активность битв <-> этот метод.
	 * Рекурсия должна продолжаться,
	 * пока есть звёзды с кораблями
	 * разных игроков на орбите.
	 */
	public void callForBattles() {
		Star starWithCombat = getStarWithCombat();
		if (starWithCombat != null) {
			Intent intent = new Intent(context, CombatActivity.class);
			Buffer.star = starWithCombat;
			context.startActivity(intent);
		}
		else {
			conquerStars();
			invalidateScreen(true);
		}
	}

	public void conquerStars() {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() != 0 && star.type != 0 && star.getShipsOnOrbitOwnerId() != 0 && star.getOwnerId() != star.getShipsOnOrbitOwnerId()) {
					star.conquer(star.getShipsOnOrbitOwnerId());
				}
			}
		}
	}

	@Nullable
	public static Star getStarWithCombat() {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.hasDifferentShipsOnOrbit()) {
					return star;
				}
			}
		}
		return null;
	}

	public void onOnlyOnePlayerLeft(boolean won) {
		if (won) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			String title = getString(R.string.game_won);
			String message = getString(R .string.continue_playing);
			String yes = getString(R.string.yes);
			String no = getString(R.string.no);
			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {
						willPlayAfterGameOver = true;
						dialog.cancel();
					}
					else {
						toGameOver(true);
						dialog.cancel();
					}
				}
			};
			builder.setTitle(title);
			builder.setMessage(message);
			builder.setPositiveButton(yes, onClickListener);
			builder.setNegativeButton(no, onClickListener);
			builder.setCancelable(false);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		else {
			toGameOver(false);
		}
	}

	public void toGameOver(boolean won) {
		currentLayoutId = R.layout.activity_new_game;
		setContentView(R.layout.activity_game_over);
		view_main = (LinearLayout) findViewById(R.id.layout_main);
		view_main.startAnimation(appearance);
		galaxy = null;
		AIs = null;
		screenShips = null;
		screenStars = null;
		screenTexts = null;
		willPlayAfterGameOver = false;
		TextView header = (TextView) findViewById(R.id.header);
		TextView mainText = (TextView) findViewById(R.id.mainText);
		Button back = (Button) findViewById(R.id.back);
		if (won) {
			header.setText(R.string.game_won);
			mainText.setText(R.string.game_won2);
		}
		else {
			header.setText(R.string.game_lost);
			mainText.setText(R.string.game_lost2);
		}
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				view_main.startAnimation(disappearance);
				delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						toMainMenu();
					}
				}, 500);
			}
		});
	}

	public void focusOnStar(Coordinates coordinates) {
		focusOnStar(coordinates.x, coordinates.y);
	}

	public void focusOnStar(byte x, byte y) {
		ScrollView sv = (ScrollView) findViewById(R.id.singleplayerScrollView);
		HorizontalScrollView hsv = (HorizontalScrollView) findViewById(R.id.singleplayerHorizontalScrollView);
		sv.requestChildFocus(screenTexts[x][y], screenTexts[x][y]);
		hsv.requestChildFocus(screenTexts[x][y], screenTexts[x][y]);
	}

	public void invalidateScreen(boolean shouldShowGalaxy) {
		//Should only be called when current layout is R.layout.activity_singleplayer!
		//otherwise throws java.lang.NullPointerException
		final byte x = selectedStarCoordinates.x;
		final byte y = selectedStarCoordinates.y;
		TextView turnNumber = (TextView) findViewById(R.id.turnNumber);
		TextView money = (TextView) findViewById(R.id.money);
		ImageView starView = (ImageView) findViewById(R.id.starView);
		TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
		TextView ownerTextView = (TextView) findViewById(R.id.ownerTextView);
		TextView fertilityTextView = (TextView) findViewById(R.id.fertilityTextView);
		TextView richnessTextView = (TextView) findViewById(R.id.richnessTextView);
		TextView scienceTextView = (TextView) findViewById(R.id.scienceTextView);
		TextView max_populationTextView = (TextView) findViewById(R.id.max_populationTextView);
		TextView populationTextView = (TextView) findViewById(R.id.populationTextView);
		LinearLayout bottomView = (LinearLayout) findViewById(R.id.bottomView);
		TextView science = (TextView) findViewById(R.id.science);
		Button colonize = (Button) findViewById(R.id.colonize);
		invalidateIncomes();
		Tools.setColoredValue(Tools.round(Player.treasury), money, false, true);
		turnNumber.setText(getString(R.string.turn) + " " + turnLong);
		if (shouldShowGalaxy) {
			showGalaxy();
		}
		int drawableId = getStarDrawableId(galaxy[x][y].type, (byte) 2);
		starView.setImageResource(drawableId);
		boolean colonizeable = galaxy[x][y].isColonizeable();
		colonize.setEnabled(colonizeable);
		String name = galaxy[x][y].name;
		if (name.equals("")) {
			name = getString(R.string.empty_space);
		}
		nameTextView.setText(name);
		int ownerID = galaxy[x][y].getOwnerId();
		if (ownerID == 0) {
			ownerTextView.setText(getResources().getString(R.string.owner_0));
		}
		else {
			if (ownerID == 1) {
				ownerTextView.setText(getResources().getString(R.string.owner_player));
			}
			else {
				ownerTextView.setText(AIs[ownerID - 2].name);
			}
		}
		fertilityTextView.setText("" + galaxy[x][y].getFertility());
		richnessTextView.setText("" + galaxy[x][y].getRichness());
		scienceTextView.setText("" + galaxy[x][y].getScientificPotential());
		max_populationTextView.setText("" + galaxy[x][y].getMaxPopulation());
		populationTextView.setText("" + Tools.round(galaxy[x][y].getPopulation(), (byte) 1));
		Tools.setColoredValue(Tools.round(getIt(Player.ID)), science, true, false);
		try {bottomView.removeView(findViewById(R.id.noShipsMessage)); }catch (Exception ignored) {}
		try {bottomView.removeView(findViewById(R.id.shipListView));} catch (Exception ignored) {}
		try {bottomView.removeView(findViewById(R.id.launch));} catch (Exception ignored) {}
		// Эти исключения игнорируются, т.к. если не удаётся удалить View, это может означать только то, что эта View уже удалена.
		if (galaxy[x][y].getNumberOfShipsOnOrbit() == 0) {
			bottomView.addView(createNoShipsMessage());
		}
		else {
			bottomView.addView(createShipListView());
			Ship[][] adaptedArray = Tools.adaptArray(galaxy[x][y].orbit);
			ShipListAdapter shipListAdapter = new ShipListAdapter(context, adaptedArray, galaxy[x][y].orbit);
			ListView shipListView = (ListView) findViewById(R.id.shipListView);
			shipListView.setAdapter(shipListAdapter);
		}
		if (galaxy[x][y].getShipsOnOrbitOwnerId() == Player.ID) {
			//В классе Star прдусмотрен тот случай, когда орбита пуста.
			Button launchShipsButton = createLaunchShipsButton();
			OnClickListener onClickListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					toMoveShips();
				}
			};
			launchShipsButton.setOnClickListener(onClickListener);
			bottomView.addView(launchShipsButton);
		}
	}

	public TextView createNoShipsMessage() {
		TextView result;
		result = new TextView(context);
		result.setText(R.string.no_ships);
		result.setTextColor(ContextCompat.getColor(context, R.color.text_color_base));
		result.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 10, 10, 10);
		result.setLayoutParams(params);
		int color = ContextCompat.getColor(context, R.color.background_space);
		result.setBackgroundColor(color);
		result.setId(R.id.noShipsMessage);
		return result;
	}

	public Button createLaunchShipsButton() {
		Button result;
		result = new Button(context);
		result.setText(R.string.launch_ships);
		result.setTextColor(ContextCompat.getColor(context, R.color.text_color_base));
		result.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 10, 10, 10);
		result.setLayoutParams(params);
		result.setPadding(10, 10, 10, 10);
		result.setId(R.id.launch);
		return result;
	}

	public ListView createShipListView() {
		ListView result;
		result = new ListView(context);
		TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
		params.setMargins(10, 10, 10, 10);
		result.setLayoutParams(params);
		int color = ContextCompat.getColor(context, R.color.background_space);
		result.setBackgroundColor(color);
		result.setId(R.id.shipListView);
		return result;
	}

	public boolean isAlive(byte ownerId) {
		if (galaxy != null) {
			for (Star[] stars : galaxy) {
				for (Star star: stars) {
					if (star.getOwnerId() == ownerId) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean anyEnemyAlive() {
		for (AbstractAI ai: AIs) {
			if (isAlive(ai.ID)) {
				return true;
			}
		}
		return false;
	}

	public void invalidateIncomes() {
		Player.income = 0;
		for (AbstractAI ai: AIs) {
			ai.income = 5;
		}
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				byte ownerId = star.getOwnerId();
				byte shipOnOrbitOwnerId = star.getShipsOnOrbitOwnerId();
				if (ownerId != 0) {
					double income = star.getIncome();
					double buildingsMaintenance = star.getBuildingsCost();
					if (ownerId == Player.ID) {
						Player.income += income;
						Player.income -= buildingsMaintenance;
					}
					else {
						AIs[ownerId - 2].income += income;
						AIs[ownerId - 2].income -= buildingsMaintenance;
					}
				}
				if (shipOnOrbitOwnerId != 0) {
					double cost = star.getSumShipsCost();
					if (shipOnOrbitOwnerId == Player.ID) {
						Player.income -= cost;
					}
					else {
						AIs[shipOnOrbitOwnerId - 2].income -= cost;
					}
				}
			}
		}
	}
	
	public static double getTaxIncome(byte ownerId) {
		double result = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					result += star.getTaxIncome();
				}
			}
		}
		return result;
	}
	
	public static double getShipsCost(byte ownerId) {
		double result = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ownerId) {
					result += star.getSumShipsCost();
				}
			}
		}
		return result;
	}
	
	public static double getBuildingsCost(byte ownerId) {
		double result = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					result += star.getBuildingsCost();
				}
			}
		}
		return result;
	}

	public void invalidateTreasuries() {
		invalidateIncomes();
		Player.treasury += Player.income;
		for (AbstractAI ai: AIs) {
			ai.treasury += ai.income;
		}
	}

	public static double getIt(byte ownerId) {
		double result = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					result += star.getIt();
				}
			}
		}
		return result;
	}

	public void addITs() {
		double playerIt = getIt(Player.ID);
		Player.IT.add(playerIt);
		for (AbstractAI ai: AIs) {
			double aiIt = getIt(ai.ID);
			ai.IT.add(aiIt);
		}
	}
	
	public static double getTradeIncome(byte id) {
		double result = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == id && !star.shouldFillStorage) {
					result += star.getTradeIncome(star.getProductionProduced());
				}
			}
		}
		return result;
	}

	public void createScreen() {
		screenTexts = new StarTextView[galaxySize][galaxySize];
		screenShips = new FrameLayout[galaxySize][galaxySize];
		screenStars = new FrameLayout[galaxySize][galaxySize];
		View inflatedStar;
		StarTextView text;
		FrameLayout ship;
		FrameLayout star;
		byte x;
		byte y;
		TableLayout table = (TableLayout) findViewById(R.id.table);
		for (x = 0; x < galaxySize; x++) {
			TableRow tableRow = new TableRow(context);
			TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 10, 10, 10);
			for (y = 0; y < galaxySize; y++) {
				inflatedStar = LayoutInflater.from(context).inflate(R.layout.item_star, null);
				text = (StarTextView) inflatedStar.findViewById(R.id.selection);
				ship = (FrameLayout) inflatedStar.findViewById(R.id.ship);
				star = (FrameLayout) inflatedStar.findViewById(R.id.star);
				screenTexts[x][y] = text;
				screenShips[x][y] = ship;
				screenStars[x][y] = star;
				Star associatedStar = galaxy[x][y];
				text.setCoordinates(associatedStar.coordinates);
				tableRow.addView(inflatedStar);
			}
			table.addView(tableRow);
		}
		setScreenTextDefaultListeners();
	}

	public void setScreenTextDefaultListeners() {
		OnClickListener onStarClickListener = StarTextView.SYSTEM_SELECTION_LISTENER;
		for (StarTextView[] starTextViews: screenTexts) {
			for (StarTextView starTextView: starTextViews) {
				starTextView.setOnClickListener(onStarClickListener);
			}
		}
	}

	public void setScreenTextDestinationSelectors(Coordinates to) {
		OnClickListener onStarClickListener = StarTextView.makeDestinationSelector(to);
		for (StarTextView[] starTextViews: screenTexts) {
			for (StarTextView starTextView: starTextViews) {
				starTextView.setOnClickListener(onStarClickListener);
			}
		}
	}

	public void toMoveShips() {
		currentLayoutId = ID_LAUNCH_SHIPS;
		findViewById(R.id.turn).setEnabled(false);
		destinationCoordinates = new Coordinates(selectedStarCoordinates.x, selectedStarCoordinates.y);
		showGalaxy();
		setScreenTextDestinationSelectors(destinationCoordinates);
		final LinearLayout bottomView = (LinearLayout) findViewById(R.id.bottomView);
		int childCount = bottomView.getChildCount();
		backup = new View[childCount];
		for (int i = 0; i < childCount; i++) {
			backup[i] = bottomView.getChildAt(i);
		}
		bottomView.removeAllViewsInLayout();
		View newBottomView = LayoutInflater.from(context).inflate(R.layout.item_launch_ships, null);
		bottomView.addView(newBottomView);
		Button done = (Button) newBottomView.findViewById(R.id.done);
		Button cancel = (Button) findViewById(R.id.cancel);
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.done) {
					ArrayList<Ship> orbit = galaxy[selectedStarCoordinates.x][selectedStarCoordinates.y].orbit;
					for (Ship ship: orbit) {
						if (ship.isSelectedByPlayer) {
							ship.destination.x = destinationCoordinates.x;
							ship.destination.y = destinationCoordinates.y;
							ship.hasReachedDestination = ship.destination.equals(ship.position);
						}
					}
				}
				findViewById(R.id.turn).setEnabled(true);
				setScreenTextDefaultListeners();
				bottomView.removeAllViewsInLayout();
				for (View view: backup) {
					bottomView.addView(view);
				}
				destinationCoordinates = null;
				currentLayoutId = R.layout.activity_singleplayer;
				showGalaxy();
			}
		};
		done.setOnClickListener(listener);
		cancel.setOnClickListener(listener);
	}
	
	public static void i(String arg) {
		Log.i(LOG_TAG, arg);
	}

}
