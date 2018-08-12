package ru.barsk.moc;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.barsk.view.Shot;
import ru.barsk.view.ShotsView;

public class CombatActivity extends Activity {
	
	private static int TIME_TO_MAKE_SHOT;
	private static int TIME_TO_EXPLODE;
	private static int TIME_TO_APPEAR;
	public static float oneDp;
	public static float oneSp;
	private LinearLayout combat_notification;
	private Star star;
	private LinearLayout combatPlace;
	private static final String LOG_TAG = "CombatActivity";
	private ArrayList<Ship> combatSideOneShips;
	private ArrayList<Ship> combatSideTwoShips;
	public static Ship[] sideOneFightingShips;
	public static Ship[] sideTwoFightingShips;
	private ArrayList<Ship> deadShips;
	private Button ok;
	private Button skip;
	private LinearLayout combatSideOne;
	private LinearLayout combatSideTwo;
	private ImageView[] combatSideOneChildren;
	private ImageView[] combatSideTwoChildren;
	private byte ownerOne;
	private byte ownerTwo;
	private byte ownerOneColorId;
	private byte ownerTwoColorId;
	private boolean combatHasStarted;
	private boolean combatHasEnded;
	private LinearLayout combatEndBackup;
	private ShotsView shotsView;
	public static int maxNumberOfShipsOnScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		combatHasStarted = false;
		combatHasEnded = false;
		setContentView(R.layout.activity_combat);
		initialize();
		combat_notification.startAnimation(MainActivity.appearance);
		((ViewGroup) skip.getParent()).removeView(skip);
		//TODO: Убрать костыль и добавить функцию пропуска битв.
	}

	public void initialize() {
		oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
		oneSp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1, getResources().getDisplayMetrics());
		star = Buffer.star;
		//Log.i(LOG_TAG, "initializing combat at " + star.coordinates);
		combatSideOneShips = star.getCombatSideOne();
		combatSideTwoShips = star.getCombatSideTwo();
		ownerOne = combatSideOneShips.get(0).ownerId;
		ownerTwo = combatSideTwoShips.get(0).ownerId;
		maxNumberOfShipsOnScreen = determineNumberOfShipsOnScreen();
		deadShips = new ArrayList<>();
		sideOneFightingShips = new Ship[maxNumberOfShipsOnScreen];
		sideTwoFightingShips = new Ship[maxNumberOfShipsOnScreen];
		combatSideOneChildren = new ImageView[maxNumberOfShipsOnScreen];
		combatSideTwoChildren = new ImageView[maxNumberOfShipsOnScreen];
		combat_notification = (LinearLayout) findViewById(R.id.combat_notification);
		combatPlace = (LinearLayout) findViewById(R.id.combat_place);
		combatEndBackup = (LinearLayout) findViewById(R.id.combat_end);
		combatPlace.removeAllViewsInLayout();
		combatPlace.addView(combat_notification);
		combatSideOne = (LinearLayout) findViewById(R.id.combat_side_one);
		combatSideTwo = (LinearLayout) findViewById(R.id.combat_side_two);
		TextView combatSystem = (TextView) findViewById(R.id.combat_system);
		TextView combatMembers = (TextView) findViewById(R.id.combat_members);
		ok = (Button) findViewById(R.id.ok);
		skip = (Button) findViewById(R.id.skip);
		combatSystem.setText(getString(R.string.system) + " " + (star.name.equals("")? getString(R.string.empty_space): star.name));
		String ownerOneName;
		String ownerTwoName;
		//Log.i(LOG_TAG, "ownerOne is " + ownerOne);
		//Log.i(LOG_TAG, "ownerTwo is " + ownerTwo);
		//Log.i(LOG_TAG, "Player.ID is " + Player.ID);
		if (ownerOne == Player.ID) {
			ownerOneName = getString(R.string.owner_player);
			ownerOneColorId = Player.colorId;
		}
		else {
			ownerOneName = MainActivity.AIs[ownerOne - 2].name;
			ownerOneColorId = MainActivity.AIs[ownerOne - 2].colorId;
		}
		if (ownerTwo == Player.ID) {
			ownerTwoName = getString(R.string.owner_player);
			ownerTwoColorId = Player.colorId;
		}
		else {
			ownerTwoName = MainActivity.AIs[ownerTwo - 2].name;
			ownerTwoColorId = MainActivity.AIs[ownerTwo - 2].colorId;
		}
		combatMembers.setText(ownerOneName + " vs " + ownerTwoName);
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.ok) {
					startCombat();
				}
				else {
					skipCombat();
				}
			}
		};
		ok.setOnClickListener(listener);
		skip.setOnClickListener(listener);
		switch (Settings.getCombatLength()) {
			case 1:
				TIME_TO_MAKE_SHOT = 3000;
				TIME_TO_EXPLODE = 500;
				TIME_TO_APPEAR = 500;
				break;
			case 2:
				TIME_TO_MAKE_SHOT = 5000;
				TIME_TO_EXPLODE = 1000;
				TIME_TO_APPEAR = 1000;
				break;
			case 3:
				TIME_TO_MAKE_SHOT = 8000;
				TIME_TO_EXPLODE = 1500;
				TIME_TO_APPEAR = 1500;
		}
	}

	public String getOwnerName(boolean left) {
		String result;
		if (left) {
			if (ownerOne == Player.ID) {
				result = getString(R.string.owner_player);
			}
			else {
				result = MainActivity.AIs[ownerOne - 2].name;
			}
		}
		else {
			if (ownerTwo == Player.ID) {
				result = getString(R.string.owner_player);
			}
			else {
				result = MainActivity.AIs[ownerTwo - 2].name;
			}
		}
		return result;
	}

	public int determineNumberOfShipsOnScreen() {
		int shipParamInDP = 64;
		int shipParamInPX = Math.round(shipParamInDP * oneDp);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int scrHeight = metrics.heightPixels;
		int combatScreenHeight = (int) (scrHeight - 50 * oneDp - 40 * oneSp);
		int result = combatScreenHeight / shipParamInPX;
		if (result < 1) {
			result = 1;
		}
		return result;
	}

	public void startCombat() {
		onCombatBegin();
	}

	public void skipCombat() {
		skip.setEnabled(false);
		if (!combatHasStarted) {
			onCombatBegin();
		}
		Toast.makeText(MainActivity.context, "Combat cannot be skipped yet!", Toast.LENGTH_SHORT).show();
	}

	public void onCombatBegin() {
		combatHasStarted = true;
		ok.setEnabled(false);
		combat_notification.startAnimation(MainActivity.disappearance);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				combatPlace.removeAllViewsInLayout();
				initializeCombatSides();
				fillSidesWithFightingShips();
				invalidateShipImages();
				shotsView = new ShotsView(MainActivity.context);
				combatPlace.addView(shotsView);
				MainActivity.delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						makeShots(TIME_TO_MAKE_SHOT);
					}
				}, TIME_TO_APPEAR + TIME_TO_EXPLODE);
			}
		}, 500);
	}

	public void onCombatEnd(int endId) {
		combatHasEnded = true;
		combatSideOne.removeAllViewsInLayout();
		combatSideTwo.removeAllViewsInLayout();
		combatPlace.removeAllViewsInLayout();
		combatPlace.addView(combatEndBackup);
		combatEndBackup.startAnimation(MainActivity.appearance);
		TextView combatSystem = (TextView) findViewById(R.id.combat_system2);
		TextView winner = (TextView) findViewById(R.id.winner);
		Button done = (Button) findViewById(R.id.done);
		combatSystem.setText(getString(R.string.system) + " " + star.name);
		String winnerMessage = getString(R.string.winner);
		switch (endId) {
			case 1:
				winnerMessage += getOwnerName(true);
				break;
			case 2:
				winnerMessage += getOwnerName(false);
				break;
			case -1:
				winnerMessage += getString(R.string.no);
				break;
		}
		winnerMessage += ".";
		winner.setText(winnerMessage);
		star.orbit.removeAll(deadShips);
		done.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				combatEndBackup.startAnimation(MainActivity.disappearance);
				MainActivity.delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						CombatActivity.super.onBackPressed();

						MainActivity.context.callForBattles();
					}
				}, 500);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (combatHasEnded) {
			combatEndBackup.startAnimation(MainActivity.disappearance);
			MainActivity.delayer.postDelayed(new Runnable() {
				@Override
				public void run() {
					CombatActivity.super.onBackPressed();
					star.orbit.removeAll(deadShips);
					MainActivity.context.callForBattles();
				}
			}, 500);
		}
		else {
			if (!combatHasStarted) {
				combatHasStarted = true;
				ok.setEnabled(false);
				combat_notification.startAnimation(MainActivity.disappearance);
				MainActivity.delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						combatPlace.removeAllViewsInLayout();
						initializeCombatSides();
						fillSidesWithFightingShips();
						invalidateShipImages();
						shotsView = new ShotsView(MainActivity.context);
						combatPlace.addView(shotsView);
						MainActivity.delayer.postDelayed(new Runnable() {
							@Override
							public void run() {
								makeShots(TIME_TO_MAKE_SHOT);
							}
						}, TIME_TO_APPEAR + TIME_TO_EXPLODE);
					}
				}, 500);
			}
			else {
				Toast.makeText(MainActivity.context, "Combat cannot be skipped yet!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void makeShots(final int deltaT) {
		//managing left-side ships:
		for (int i = 0; i < maxNumberOfShipsOnScreen; i++) {
			if (sideOneFightingShips[i] != null) {
				Ship[] shipsGoingToStayAlive = sideTwoFightingShips.clone();
				shot(i, true, sideOneFightingShips[i].getAttack(), deltaT, shipsGoingToStayAlive);
			}
		}
		//managing right-side ships
		for (int i = 0; i < maxNumberOfShipsOnScreen; i++) {
			if (sideTwoFightingShips[i] != null) {
				Ship[] shipsGoingToStayAlive = sideOneFightingShips.clone();
				shot (i, false, sideTwoFightingShips[i].getAttack(), deltaT, shipsGoingToStayAlive);
			}
		}
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				explodeDeadShips(deltaT);
			}
		}, deltaT + 500);
	}

	public void shot(int shipIndex, boolean shipIsLeft, int damage, int deltaT, @NonNull Ship[] shipsGoingToStayAlive) {
		if (damage <= 0) {
			Log.w(LOG_TAG, "tried to make shot with damage of " + damage);
			return;
		}
		if (shouldMakeMoreShots(shipIsLeft, shipsGoingToStayAlive)) {
			if (shipIsLeft) {
				int targetIndex = getMostPowerfulShipIndex(true, shipsGoingToStayAlive);
				int damageBeingDealt = shotsView.getSumDamage(targetIndex, false);
				int hpLeft = sideTwoFightingShips[targetIndex].getHp() - damageBeingDealt / 2;
				if (hpLeft > damage / 2) {
					Shot shot = new Shot(sideTwoFightingShips[targetIndex], 16, getY(shipIndex), shotsView.viewWidth + 16, getY(targetIndex), ownerOneColorId, deltaT, damage, targetIndex);
					shotsView.add(shot);
				}
				else {
					int damageLeft = damage - hpLeft * 2;
					Shot shot = new Shot(sideTwoFightingShips[targetIndex], 16, getY(shipIndex), shotsView.viewWidth + 16, getY(targetIndex), ownerOneColorId, deltaT, hpLeft * 2, targetIndex);
					shotsView.add(shot);
					shipsGoingToStayAlive[targetIndex] = null;
					shot(shipIndex, true, damageLeft, deltaT, shipsGoingToStayAlive);
				}
			}
			else {
				int targetIndex = getMostPowerfulShipIndex(false, shipsGoingToStayAlive);
				int damageBeingDealt = shotsView.getSumDamage(targetIndex, true);
				int hpLeft = sideOneFightingShips[targetIndex].getHp() - damageBeingDealt / 2;
				if (hpLeft > damage / 2) {
					Shot shot = new Shot (sideOneFightingShips[targetIndex], shotsView.viewWidth + 16, getY(shipIndex), 16, getY(targetIndex), ownerTwoColorId, deltaT, damage, targetIndex);
					shotsView.add(shot);
				}
				else {
					int damageLeft = damage - hpLeft * 2;
					Shot shot = new Shot (sideOneFightingShips[targetIndex], shotsView.viewWidth + 16, getY(shipIndex), 16, getY(targetIndex), ownerTwoColorId, deltaT, hpLeft * 2, targetIndex);
					shotsView.add(shot);
					shipsGoingToStayAlive[targetIndex] = null;
					shot(shipIndex, false, damageLeft, deltaT, shipsGoingToStayAlive);
				}
			}
		}
	}

	public int getMostPowerfulShipIndex(boolean left, Ship[] ships) {
		int resultIndex = getFirstShipIndex(ships);
		double maxMaintenance = ships[resultIndex].maintenance;
		for (int i = resultIndex; i < ships.length; i++) {
			if (ships[i] != null) {
				double maintenance = ships[i].maintenance;
				int damageBeingDealt = shotsView.getSumDamage(i, left);
				int hpLeft = ships[i].getHp() - damageBeingDealt / 2;
				if (maintenance > maxMaintenance && hpLeft > 0) {
					resultIndex = i;
					maxMaintenance = ships[i].maintenance;
				}
			}
		}
		return resultIndex;
	}

	public boolean shouldMakeMoreShots(boolean shooterIsLeft, Ship[] target) {
		for (int i = 0; i < target.length; i++) {
			if (target[i] != null) {
				int damage = shotsView.getSumDamage(i, !shooterIsLeft);
				int hpLeft = target[i].getHp() - damage / 2;
				if (hpLeft > 0 && target[i].alive) {
					return true;
				}
			}
		}
		return false;
	}

	public int getFirstShipIndex(Ship[] ships) {
		for (int i = 0; i < ships.length; i++) {
			if (ships[i] != null) {
				return i;
			}
		}
		return -1;
	}

	public void explodeDeadShips(final int deltaT) {
		boolean shouldVibrate = false;
		for (int i = 0; i < maxNumberOfShipsOnScreen; i++) {
			if (sideOneFightingShips[i] != null) {
				if (!sideOneFightingShips[i].alive) {
					explodeSideOneShip(i);
					shouldVibrate = true;
					deadShips.add(sideOneFightingShips[i]);
					sideOneFightingShips[i] = null;
				}
			}
			if (sideTwoFightingShips[i] != null) {
				if (!sideTwoFightingShips[i].alive) {
					explodeSideTwoShip(i);
					shouldVibrate = true;
					deadShips.add(sideTwoFightingShips[i]);
					sideTwoFightingShips[i] = null;
				}
			}
		}
		if (shouldVibrate && Settings.doVibrate) {
			MainActivity.vibrator.vibrate(TIME_TO_EXPLODE * 2 / 3);
		}
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				fillSidesWithFightingShips();
				invalidateShipImages();
				final int winner = getWinner();
				MainActivity.delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (winner == 0) {
							makeShots(deltaT);
						}
						else {
							onCombatEnd(winner);
						}
					}
				}, 500 + 500);
			}
		}, TIME_TO_EXPLODE + 500);
	}
	
	public void explodeSideOneShip(final int index) {
		combatSideOneChildren[index].setImageResource(R.drawable.ship_exploding_0);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				combatSideOneChildren[index].setImageResource(R.drawable.ship_exploding_1);
				MainActivity.delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						combatSideOneChildren[index].setImageResource(R.drawable.ship_exploding_2);
						MainActivity.delayer.postDelayed(new Runnable() {
							@Override
							public void run() {
								combatSideOneChildren[index].setImageResource(R.drawable.ship_clear);
							}
						}, TIME_TO_EXPLODE / 3);
					}
				}, TIME_TO_EXPLODE / 3);
			}
		}, TIME_TO_EXPLODE / 3);
	}

	public void explodeSideTwoShip(final int index) {
		combatSideTwoChildren[index].setImageResource(R.drawable.ship_exploding_0);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				combatSideTwoChildren[index].setImageResource(R.drawable.ship_exploding_1);
				MainActivity.delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						combatSideTwoChildren[index].setImageResource(R.drawable.ship_exploding_2);
						MainActivity.delayer.postDelayed(new Runnable() {
							@Override
							public void run() {
								combatSideTwoChildren[index].setImageResource(R.drawable.ship_clear);
							}
						}, TIME_TO_EXPLODE / 3);
					}
				}, TIME_TO_EXPLODE / 3);
			}
		}, TIME_TO_EXPLODE / 3);
	}

	public void fillSidesWithFightingShips() {
		int numberOfShips;
		if (combatSideOneShips.size() > maxNumberOfShipsOnScreen) {
			numberOfShips = maxNumberOfShipsOnScreen;
		}
		else {
			numberOfShips = combatSideOneShips.size();
		}
		for (int i = 0; i < numberOfShips; i++) {
			if (sideOneFightingShips[i] == null) {
				sideOneFightingShips[i] = combatSideOneShips.get(0);
				combatSideOneShips.remove(0);
				combatSideOneChildren[i].startAnimation(MainActivity.appearance_left);
			}
		}
		if (combatSideTwoShips.size() > maxNumberOfShipsOnScreen) {
			numberOfShips = maxNumberOfShipsOnScreen;
		}
		else {
			numberOfShips = combatSideTwoShips.size();
		}
		for (int i = 0; i < numberOfShips; i++) {
			if (sideTwoFightingShips[i] == null) {
				sideTwoFightingShips[i] = combatSideTwoShips.get(0);
				combatSideTwoShips.remove(0);
				combatSideTwoChildren[i].startAnimation(MainActivity.appearance_right);
			}
		}
	}

	public void initializeCombatSides() {
		for (int i = 0; i < maxNumberOfShipsOnScreen; i++) {
			combatSideOneChildren[i] = makeShipImageView(true);
			combatSideOne.addView(combatSideOneChildren[i]);
			combatSideTwoChildren[i] = makeShipImageView(false);
			combatSideTwo.addView(combatSideTwoChildren[i]);
		}
	}

	public void invalidateShipImages() {
		for (int i = 0; i < maxNumberOfShipsOnScreen; i++) {
			Ship ship1 = sideOneFightingShips[i];
			ImageView ship1Image = combatSideOneChildren[i];
			if (ship1 != null) {
				byte ship1ColorCode;
				if (ship1.ownerId == Player.ID) {
					ship1ColorCode = Player.colorId;
				}
				else {
					ship1ColorCode = MainActivity.AIs[ship1.ownerId - 2].colorId;
				}
				int drawable1 = MainActivity.getShipDrawableId(ship1.base, ship1ColorCode);
				ship1Image.setImageResource(drawable1);
			}
			else {
				ship1Image.setImageResource(R.drawable.ship_clear);
			}
			Ship ship2 = sideTwoFightingShips[i];
			ImageView ship2Image = combatSideTwoChildren[i];
			if (ship2 != null) {
				byte ship2ColorCode;
				if (ship2.ownerId == Player.ID) {
					ship2ColorCode = Player.colorId;
				}
				else {
					ship2ColorCode = MainActivity.AIs[ship2.ownerId - 2].colorId;
				}
				int drawable2 = MainActivity.getShipDrawableId(ship2.base, ship2ColorCode);
				ship2Image.setImageResource(drawable2);
			}
			else {
				ship2Image.setImageResource(R.drawable.ship_clear);
			}
		}
	}

	public static int getY(int index) {
		return (int) ((index * 64 * oneDp) + 32 * oneDp);
	}

	public ImageView makeShipImageView(boolean inverted) {
		ImageView result;
		result = new ImageView(MainActivity.context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (64 * oneDp), (int) (64 * oneDp));
		result.setLayoutParams(params);
		if (inverted) {
			result.setRotation(180);
		}
		return result;
	}

	public int getSideOneNumberOfFightingShips() {
		int result = 0;
		for (Ship ship: sideOneFightingShips) {
			if (ship != null) {
				if (ship.alive) {
					result++;
				}
			}
		}
		return result;
	}

	@Deprecated
	public void logCombat() {
		StringBuilder result = new StringBuilder();
		for (Ship ship: sideOneFightingShips) {
			if (ship == null) {
				result.append("null ");
			}
			else {
				result.append(ship.alive);
				result.append(" ");
			}
		}
		result.append(" vs ");
		for (Ship ship: sideTwoFightingShips) {
			if (ship == null) {
				result.append("null ");
			}
			else {
				result.append(ship.alive);
				result.append(" ");
			}
		}
		Log.i(LOG_TAG, result.toString());
	}

	public int getSideTwoNumberOfFightingShips() {
		int result = 0;
		for (Ship ship: sideTwoFightingShips) {
			if (ship != null) {
				if (ship.alive) {
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * @return  0, if no winner;
	 *			1, if side one has won;
	 * 			2, if side two has won;
	 * 			-1, if no ships left at all
	 */
	public int getWinner() {
		int numberOfShips1 = getSideOneNumberOfFightingShips();
		int numberOfShips2 = getSideTwoNumberOfFightingShips();
		if (numberOfShips1 != 0 && numberOfShips2 != 0) {
			return 0;
		}
		else {
			//noinspection ConstantConditions
			if (numberOfShips1 != 0 && numberOfShips2 == 0) {
				return 1;
			}
			else {
				//noinspection ConstantConditions
				if (numberOfShips1 == 0 && numberOfShips2 != 0) {
					return 2;
				}
				else {
					return -1;
				}
			}
		}
	}

}
