package ru.barsk.moc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import ru.barsk.util.Tools;

public final class StarViewActivity extends Activity {

	public ColonyManager colonyManager;
	public Star observedStar;
	private Coordinates coordinates;
	public static LinearLayout layout_main;
	public static StarViewActivity context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_view);
		context = this;
		layout_main = (LinearLayout) findViewById(R.id.layout_main);
		layout_main.startAnimation(MainActivity.appearance);
		coordinates = MainActivity.selectedStarCoordinates;
		observedStar = MainActivity.galaxy[coordinates.x][coordinates.y];
		colonyManager = observedStar.colonyManager;
		initialize();
	}

	public void initialize() {
		final ImageView starView = (ImageView) findViewById(R.id.starView);
		final TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
		final TextView ownerTextView = (TextView) findViewById(R.id.ownerTextView);
		final TextView fertilityTextView = (TextView) findViewById(R.id.fertilityTextView);
		final TextView richnessTextView = (TextView) findViewById(R.id.richnessTextView);
		final TextView scienceTextView = (TextView) findViewById(R.id.scienceTextView);
		final TextView max_populationTextView = (TextView) findViewById(R.id.max_populationTextView);
		final TextView populationTextView = (TextView) findViewById(R.id.populationTextView);
		final TextView income_from_star = (TextView) findViewById(R.id.income_from_star);
		final Button buildings = (Button) findViewById(R.id.buildings);
		final Button previousStar = (Button) findViewById(R.id.previous_star);
		final Button nextStar = (Button) findViewById(R.id.next_star);
		final SeekBar farmersSeekBar = (SeekBar) findViewById(R.id.farmersSeekBar);
		final ImageView farmersLock = (ImageView) findViewById(R.id.farmers_lock);
		final SeekBar workersSeekBar = (SeekBar) findViewById(R.id.workersSeekBar);
		final ImageView workersLock = (ImageView) findViewById(R.id.workers_lock);
		final SeekBar scientistsSeekBar = (SeekBar) findViewById(R.id.scientistsSeekBar);
		final ImageView scientistsLock = (ImageView) findViewById(R.id.scientists_lock);
		final TextView buildingTextView = (TextView) findViewById(R.id.buildingTextView);
		final ImageView buildingDrawable = (ImageView) findViewById(R.id.buildingDrawable);
		final TextView costTextView = (TextView) findViewById(R.id.costTextView);
		final Button changeBuilding = (Button) findViewById(R.id.add);
		final Button queue = (Button) findViewById(R.id.queue);
		final TextView foodStorage = (TextView) findViewById(R.id.foodStorage);
		final TextView productionStorage = (TextView) findViewById(R.id.productionStorage);
		final TextView moraleTextView = (TextView) findViewById(R.id.moraleTextView);
		final ImageView moraleImageView = (ImageView) findViewById(R.id.moraleImageView);
		final Button done = (Button) findViewById(R.id.done);
		ArtificialConstruction construction = observedStar.constructionQueue.getFirst();
		if (construction == null) {
			if (observedStar.shouldFillStorage) {
				buildingTextView.setText(getString(R.string.idle));
				buildingDrawable.setImageResource(R.drawable.idle);
			}
			else {
				buildingTextView.setText(getString(R.string.trade2));
				buildingDrawable.setImageResource(R.drawable.money);
			}
			costTextView.setText("(0/0)");
		}
		else {
			buildingTextView.setText(construction.name);
			costTextView.setText("(" + Tools.round(construction.initialProductionCost - construction.productionCost) + "/" + Tools.round(construction.initialProductionCost) + ")");
			Object expectedResult = construction.construct(Player.ID, new Coordinates((byte) 0, (byte) 0));
			if (expectedResult instanceof Building) {
				buildingDrawable.setImageResource(R.drawable.alert_building);
			}
			else {
				buildingDrawable.setImageResource(MainActivity.getShipDrawableId(((Ship) expectedResult).base, observedStar.getOwnerId()));
			}
		}
		final View.OnClickListener onLockClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.farmers_lock:
						colonyManager.farmersEnabled = !colonyManager.farmersEnabled;
						if (colonyManager.farmersEnabled) {
							farmersLock.setImageResource(R.drawable.lock_opened);
						}
						else {
							farmersLock.setImageResource(R.drawable.lock_closed);
						}
						break;
					case R.id.workers_lock:
						colonyManager.workersEnabled = !colonyManager.workersEnabled;
						if (colonyManager.workersEnabled) {
							workersLock.setImageResource(R.drawable.lock_opened);
						}
						else {
							workersLock.setImageResource(R.drawable.lock_closed);
						}
						break;
					case R.id.scientists_lock:
						colonyManager.scientistsEnabled = !colonyManager.scientistsEnabled;
						if (colonyManager.scientistsEnabled) {
							scientistsLock.setImageResource(R.drawable.lock_opened);
						}
						else {
							scientistsLock.setImageResource(R.drawable.lock_closed);
						}
				}
				farmersSeekBar.setEnabled(colonyManager.farmersEnabled && (colonyManager.workersEnabled || colonyManager.scientistsEnabled));
				workersSeekBar.setEnabled(colonyManager.workersEnabled && (colonyManager.farmersEnabled || colonyManager.scientistsEnabled));
				scientistsSeekBar.setEnabled(colonyManager.scientistsEnabled && (colonyManager.farmersEnabled || colonyManager.workersEnabled));
			}
		};
		farmersLock.setOnClickListener(onLockClickListener);
		workersLock.setOnClickListener(onLockClickListener);
		scientistsLock.setOnClickListener(onLockClickListener);
		final View.OnClickListener onButtonClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.done:
						onBackPressed();
						break;
					case R.id.next_star:
						toOtherStar(true);
						break;
					case R.id.previous_star:
						toOtherStar(false);
						break;
					case R.id.buildings:
						layout_main.startAnimation(MainActivity.disappearance);
						MainActivity.delayer.postDelayed(new Runnable() {
							@Override
							public void run() {
								Intent intent = new Intent(StarViewActivity.this, BuildingsViewActivity.class);
								Buffer.star = observedStar;
								startActivity(intent);
							}
						}, 500);
						break;
					case R.id.queue:
						layout_main.startAnimation(MainActivity.disappearance);
						MainActivity.delayer.postDelayed(new Runnable() {
							@Override
							public void run() {
								Intent intent = new Intent(StarViewActivity.this, ConstructionQueueActivity.class);
								Buffer.star = observedStar;
								startActivity(intent);
							}
						}, 500);
						break;
					case R.id.add:
						layout_main.startAnimation(MainActivity.disappearance);
						MainActivity.delayer.postDelayed(new Runnable() {
							@Override
							public void run() {
								Intent intent = new Intent(StarViewActivity.this, ConstructionSelectionActivity.class);
								Buffer.star = observedStar;
								startActivity(intent);
							}
						}, 500);
						break;
					default:
						Toast.makeText(MainActivity.context, "Error: unknown button.", Toast.LENGTH_SHORT).show();
				}
			}
		};
		boolean hasOtherStars = hasOtherStars();
		nextStar.setEnabled(hasOtherStars);
		previousStar.setEnabled(hasOtherStars);
		nextStar.setOnClickListener(onButtonClickListener);
		previousStar.setOnClickListener(onButtonClickListener);
		done.setOnClickListener(onButtonClickListener);
		buildings.setOnClickListener(onButtonClickListener);
		queue.setOnClickListener(onButtonClickListener);
		changeBuilding.setOnClickListener(onButtonClickListener);
		final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //maxProgress has not been determined manually => == 100
				if (fromUser) {
					switch (seekBar.getId()) {
						case R.id.farmersSeekBar:
							if (colonyManager.workersEnabled && colonyManager.scientistsEnabled) {
								double k;
								if (colonyManager.workersPercentage + colonyManager.scientistsPercentage != 0) {
									k = colonyManager.workersPercentage / (colonyManager.workersPercentage + colonyManager.scientistsPercentage);
								}
								else {
									k = 0.5;
								}
								double newFarmersPercentage = progress / 100.0;
								double sumPercentageLeft = 1 - newFarmersPercentage;
								double newWorkersPercentage = k * sumPercentageLeft;
								double newScientistPercentage  = sumPercentageLeft - newWorkersPercentage;
								colonyManager.setPercentage(newFarmersPercentage, newWorkersPercentage, newScientistPercentage);
							}
							else {
								if (colonyManager.workersEnabled) {
									double maxPercentage = colonyManager.farmersPercentage + colonyManager.workersPercentage;
									int maxProgress = (int) Math.round(maxPercentage * 100);
									if (progress >= maxProgress) {
										colonyManager.setPercentage(maxPercentage, 0, colonyManager.scientistsPercentage);
									}
									else {
										double newFarmersPercentage = progress / 100.0;
										double newWorkersPercentage = maxPercentage - newFarmersPercentage;
										colonyManager.setPercentage(newFarmersPercentage, newWorkersPercentage, colonyManager.scientistsPercentage);
									}
								}
								else {
                                    /*=> colonyManager.scientistsEnabled == true, otherwise user would be unable to impact farmersSeekBar*/
									double maxPercentage = colonyManager.farmersPercentage + colonyManager.scientistsPercentage;
									int maxProgress = (int) Math.round(maxPercentage * 100);
									if (progress >= maxProgress) {
										colonyManager.setPercentage(maxPercentage, colonyManager.workersPercentage, 0);
									}
									else {
										double newFarmersPercentage = progress / 100.0;
										double newScientistsPercentage = maxPercentage - newFarmersPercentage;
										colonyManager.setPercentage(newFarmersPercentage, colonyManager.workersPercentage, newScientistsPercentage);
									}
								}
							}
							invalidatePercentages();
							invalidateDeltaMorale();
							break;
						case R.id.workersSeekBar:
							if (colonyManager.farmersEnabled && colonyManager.scientistsEnabled) {
								double k;
								if (colonyManager.farmersPercentage + colonyManager.scientistsPercentage != 0) {
									k = colonyManager.farmersPercentage / (colonyManager.farmersPercentage + colonyManager.scientistsPercentage);
								}
								else {
									k = 0.5;
								}
								double newWorkersPercentage = progress / 100.0;
								double sumPercentageLeft = 1 - newWorkersPercentage;
								double newFarmersPercentage = k * sumPercentageLeft;
								double newScientistsPercentage = sumPercentageLeft - newFarmersPercentage;
								colonyManager.setPercentage(newFarmersPercentage, newWorkersPercentage, newScientistsPercentage);
							}
							else {
								if (colonyManager.farmersEnabled) {
									double maxPercentage = colonyManager.farmersPercentage + colonyManager.workersPercentage;
									int maxProgress = (int) Math.round(maxPercentage * 100);
									if (progress >= maxProgress) {
										colonyManager.setPercentage(0, maxPercentage, colonyManager.scientistsPercentage);
									}
									else {
										double newWorkersPercentage = progress / 100.0;
										double newFarmersPercentage = maxPercentage - newWorkersPercentage;
										colonyManager.setPercentage(newFarmersPercentage, newWorkersPercentage, colonyManager.scientistsPercentage);
									}
								}
								else {
                                    //=> colonyManager.scientistsEnabled == true, otherwise user would be unable to impact workersSeekBar
									double maxPercentage = colonyManager.workersPercentage + colonyManager.scientistsPercentage;
									int maxProgress = (int) Math.round(maxPercentage * 100);
									if (progress >= maxProgress) {
										colonyManager.setPercentage(colonyManager.farmersPercentage, maxPercentage, 0);
									}
									else {
										double newWorkersPercentage = progress / 100.0;
										double newScientistsPercentage = maxPercentage - newWorkersPercentage;
										colonyManager.setPercentage(colonyManager.farmersPercentage, newWorkersPercentage, newScientistsPercentage);
									}
								}
							}
							invalidatePercentages();
							invalidateDeltaMorale();
							break;
						case R.id.scientistsSeekBar:
							if (colonyManager.farmersEnabled && colonyManager.workersEnabled) {
								double k;
								if (colonyManager.farmersPercentage + colonyManager.workersPercentage != 0) {
									k = colonyManager.farmersPercentage / (colonyManager.farmersPercentage + colonyManager.workersPercentage);
								}
								else {
									k = 0.5;
								}
								double newScientistsPercentage = progress / 100.0;
								double sumPercentageLeft = 1 - newScientistsPercentage;
								double newFarmersPercentage = k * sumPercentageLeft;
								double newWorkersPercentage = sumPercentageLeft - newFarmersPercentage;
								colonyManager.setPercentage(newFarmersPercentage, newWorkersPercentage, newScientistsPercentage);
							}
							else {
								if (colonyManager.farmersEnabled) {
									double maxPercentage = colonyManager.farmersPercentage + colonyManager.scientistsPercentage;
									int maxProgress = (int) Math.round(maxPercentage * 100);
									if (progress >= maxProgress) {
										colonyManager.setPercentage(0, colonyManager.workersPercentage, maxPercentage);
									}
									else {
										double newScientistsPercentage = progress / 100.0;
										double newFarmersPercentage = maxPercentage - newScientistsPercentage;
										colonyManager.setPercentage(newFarmersPercentage, colonyManager.workersPercentage, newScientistsPercentage);
									}
								}
								else {
                                    /*=> colonyManager.workersEnabled == true, otherwise user would be unable to impact scientistsSeekBar*/
									double maxPercentage = colonyManager.workersPercentage + colonyManager.scientistsPercentage;
									int maxProgress = (int) Math.round(maxPercentage * 100);
									if (progress >= maxProgress) {
										colonyManager.setPercentage(colonyManager.farmersPercentage, 0, maxPercentage);
									}
									else {
										double newScientistsPercentage = progress / 100.0;
										double newWorkersPercentage  = maxPercentage - newScientistsPercentage;
										colonyManager.setPercentage(colonyManager.farmersPercentage, newWorkersPercentage, newScientistsPercentage);
									}
								}
							}
							invalidatePercentages();
							invalidateDeltaMorale();
							break;
						default:
							Toast.makeText(MainActivity.context, "Error: unknown seekBar.", Toast.LENGTH_SHORT).show();
					}
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		};
		farmersSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		workersSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		scientistsSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		starView.setImageResource(MainActivity.getStarDrawableId(observedStar.type, (byte) 2));
		nameTextView.setText(observedStar.name);
		ownerTextView.setText(getString(R.string.owner_player));
		fertilityTextView.setText("" + observedStar.getFertility());
		richnessTextView.setText("" + observedStar.getRichness());
		scienceTextView.setText("" + observedStar.getScientificPotential());
		max_populationTextView.setText("" + observedStar.getMaxPopulation());
		populationTextView.setText("" + Tools.round(observedStar.getPopulation(), (byte) 1));
		Tools.setColoredValue(Tools.round(observedStar.getIncome() - observedStar.getBuildingsCost(), (byte) 1), income_from_star, true, true);
        //TODO: buildings
		farmersSeekBar.setProgress((int) Math.round(colonyManager.farmersPercentage * 100));
		farmersSeekBar.setEnabled(colonyManager.farmersEnabled && (colonyManager.workersEnabled || colonyManager.scientistsEnabled));
		workersSeekBar.setProgress((int) Math.round(colonyManager.workersPercentage * 100));
		workersSeekBar.setEnabled(colonyManager.workersEnabled && (colonyManager.farmersEnabled || colonyManager.scientistsEnabled));
		scientistsSeekBar.setProgress((int) Math.round(colonyManager.scientistsPercentage * 100));
		scientistsSeekBar.setEnabled(colonyManager.scientistsEnabled && (colonyManager.farmersEnabled || colonyManager.workersEnabled));
		invalidatePercentages();
		if (colonyManager.farmersEnabled) {
			farmersLock.setImageResource(R.drawable.lock_opened);
		}
		else {
			farmersLock.setImageResource(R.drawable.lock_closed);
		}
		if (colonyManager.workersEnabled) {
			workersLock.setImageResource(R.drawable.lock_opened);
		}
		else {
			workersLock.setImageResource(R.drawable.lock_closed);
		}
		if (colonyManager.scientistsEnabled) {
			scientistsLock.setImageResource(R.drawable.lock_opened);
		}
		else {
			scientistsLock.setImageResource(R.drawable.lock_closed);
		}
		queue.setText(observedStar.constructionQueue.size() + " " + getString(R.string.in_queue));
		foodStorage.setText(getString(R.string.food) + " " + Tools.round(observedStar.getFoodStored(), (byte) 1) + "/" + Tools.round(observedStar.getMaxStored(), (byte) 1));
		productionStorage.setText(getString(R.string.production) + " " + Tools.round(observedStar.getProductionStored(), (byte) 1) + "/" + Tools.round(observedStar.getMaxStored(), (byte) 1));
		showMorale(observedStar.getMorale(), moraleTextView, moraleImageView);
		invalidateDeltaMorale();
	}

	public void onBackPressed() {
		layout_main.startAnimation(MainActivity.disappearance);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				StarViewActivity.super.onBackPressed();
				MainActivity.selectedStarCoordinates = coordinates;
				MainActivity.context.focusOnStar(coordinates);
				MainActivity.context.invalidateScreen(true);
			}
		}, 500);
	}

	private void invalidatePercentages() {
		SeekBar farmersSeekBar = (SeekBar) findViewById(R.id.farmersSeekBar);
		SeekBar workersSeekBar = (SeekBar) findViewById(R.id.workersSeekBar);
		SeekBar scientistsSeekBar = (SeekBar) findViewById(R.id.scientistsSeekBar);
		TextView farmersTextView = (TextView) findViewById(R.id.farmersTextView);
		TextView workersTextView = (TextView) findViewById(R.id.workersTextView);
		TextView scientistsTextView = (TextView) findViewById(R.id.scientistsTextView);
		TextView income_from_star = (TextView) findViewById(R.id.income_from_star);
		int farmersSeekBarProgress = (int) Math.round(colonyManager.farmersPercentage * 100);
		int workersSeekBarProgress = (int) Math.round(colonyManager.workersPercentage * 100);
		int scientistsSeekBarProgress = (int) Math.round(colonyManager.scientistsPercentage * 100);
		farmersSeekBar.setProgress(farmersSeekBarProgress);
		workersSeekBar.setProgress(workersSeekBarProgress);
		scientistsSeekBar.setProgress(scientistsSeekBarProgress);
		double foodIncome = observedStar.getFoodProduced() - observedStar.getPopulation();
		foodIncome = Tools.round(foodIncome);
		double productionIncome = observedStar.getProductionProduced();
		productionIncome = Tools.round(productionIncome);
		double scienceIncome = observedStar.getIt();
		scienceIncome = Tools.round(scienceIncome);
		Tools.setColoredValue(foodIncome, farmersTextView, true, false);
		Tools.setColoredValue(productionIncome, workersTextView, true, false);
		Tools.setColoredValue(scienceIncome, scientistsTextView, true, false);
		Tools.setColoredValue(Tools.round(observedStar.getIncome() - observedStar.getBuildingsCost(), (byte) 2), income_from_star, true, true);
	}

	private void invalidateDeltaMorale() {
		ImageView delta_moraleImageView = (ImageView) findViewById(R.id.delta_moraleImageView);
		TextView delta_moraleTextView = (TextView) findViewById(R.id.delta_moraleTextView);
		double foodProduced = observedStar.getFoodProduced();
		double delta_morale;
		if (foodProduced + observedStar.getFoodStored() >= observedStar.getPopulation()) {
			delta_morale = observedStar.getMorale() * 0.1;
		}
		else {
			if (foodProduced + observedStar.getFoodStored() == 0) {
				delta_morale = observedStar.getMorale() * (-0.1);
			}
			else {
				double food = observedStar.getFoodStored() + foodProduced;
				double newMorale = observedStar.getMorale() * (food / (5 * observedStar.getPopulation()) + 0.9);
				delta_morale = newMorale - observedStar.getMorale();
			}
		}
		if (delta_morale > 10 - observedStar.getMorale()) {
			delta_morale = 10 - observedStar.getMorale();
		}
		if (delta_morale > 0.08) {
			delta_moraleImageView.setImageResource(R.drawable.arrow4);
			delta_moraleTextView.setText("+" + Tools.round(delta_morale, (byte) 2));
			delta_moraleTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_good));
		}
		else {
			if (delta_morale > 0.04) {
				delta_moraleImageView.setImageResource(R.drawable.arrow3);
				delta_moraleTextView.setText("+" + Tools.round(delta_morale, (byte) 2));
				delta_moraleTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_better));
			}
			else {
				if (delta_morale >= -0.04) {
					delta_moraleImageView.setImageResource(R.drawable.arrow2);
					delta_moraleTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_average));
					if (delta_morale >= 0) {
						delta_moraleTextView.setText("+" + Tools.round(delta_morale, (byte) 2));
					}
					else {
						delta_moraleTextView.setText("" + Tools.round(delta_morale, (byte) 2));
					}
				}
				else {
					if (delta_morale >= -0.08) {
						delta_moraleImageView.setImageResource(R.drawable.arrow1);
						delta_moraleTextView.setText("" + Tools.round(delta_morale, (byte) 2));
						delta_moraleTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_worse));
					}
					else {
						delta_moraleImageView.setImageResource(R.drawable.arrow0);
						delta_moraleTextView.setText("" + Tools.round(delta_morale, (byte) 2));
						delta_moraleTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_bad));
					}
				}
			}
		}
	}

	private void showMorale(double morale, TextView linkToTextView, ImageView linkToImageView) {
		linkToTextView.setText("" + Tools.round(morale, (byte) 1));
		if (morale < 2) {
			linkToTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_bad));
			linkToImageView.setImageResource(R.drawable.morale0);
		}
		else {
			if (morale < 4) {
				linkToTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_worse));
				linkToImageView.setImageResource(R.drawable.morale1);
			}
			else {
				if (morale < 6) {
					linkToTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_average));
					linkToImageView.setImageResource(R.drawable.morale2);
				}
				else {
					if (morale < 8) {
						linkToTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_better));
						linkToImageView.setImageResource(R.drawable.morale3);
					}
					else {
						linkToTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color_good));
						linkToImageView.setImageResource(R.drawable.morale4);
					}
				}
			}
		}
	}

	/**
	 * @param right
	 * direction of switching star
	 * true, if right
	 * false if left
	 */
	public void toOtherStar(boolean right) {
		byte x;
		byte y;
		if (right) {
			for (x = coordinates.x; x < MainActivity.galaxy.length; x++) {
				for (y = 0; y < MainActivity.galaxy[x].length; y++) {
					if (x == coordinates.x && y <= coordinates.y) {
						continue;
					}
					if (MainActivity.galaxy[x][y].getOwnerId() == Player.ID) {
						toStar(x, y, true);
						return;
					}
				}
			}
			outer:
			for (x = 0; x <= coordinates.x; x++) {
				for (y = 0; y < MainActivity.galaxy[x].length; y++) {
					if (x == coordinates.x && y == coordinates.y) {
						break outer;
					}
					if (MainActivity.galaxy[x][y].getOwnerId() == Player.ID) {
						toStar(x, y, true);
						return;
					}
				}
			}
		}
		else {
			for (x = coordinates.x; x >= 0; x--) {
				for (y = (byte) (MainActivity.galaxySize - 1); y >= 0; y--) {
					if (x == coordinates.x && y >= coordinates.y) {
						continue;
					}
					if (MainActivity.galaxy[x][y].getOwnerId() == Player.ID) {
						toStar(x, y, false);
						return;
					}
				}
			}
			outer:
			for (x = (byte) (MainActivity.galaxySize - 1); x >= coordinates.x; x--) {
				for (y = (byte) (MainActivity.galaxySize - 1); y >= 0; y--) {
					if (x == coordinates.x && y == coordinates.y) {
						break outer;
					}
					if (MainActivity.galaxy[x][y].getOwnerId() == Player.ID) {
						toStar(x, y, false);
						return;
					}
				}
			}
		}
	}

	public void toStar (byte x, byte y, boolean right) {
		observedStar = MainActivity.galaxy[x][y];
		coordinates = new Coordinates(x, y);
		colonyManager = observedStar.colonyManager;
		if (right) {
			layout_main.startAnimation(MainActivity.disappearance_left);
			MainActivity.delayer.postDelayed(new Runnable() {
				@Override
				public void run() {
					initialize();
					layout_main.startAnimation(MainActivity.appearance_right);
				}
			}, 500);
		}
		else {
			layout_main.startAnimation(MainActivity.disappearance_right);
			MainActivity.delayer.postDelayed(new Runnable() {
				@Override
				public void run() {
					initialize();
					layout_main.startAnimation(MainActivity.appearance_left);
				}
			}, 500);
		}
	}

	public boolean hasOtherStars() {
		for (byte x = 0; x < MainActivity.galaxySize; x++) {
			for (byte y = 0; y < MainActivity.galaxySize; y++) {
				if (x == coordinates.x && y == coordinates.y) {
					continue;
				}
				if (MainActivity.galaxy[x][y].getOwnerId() == Player.ID) {
					return true;
				}
			}
		}
		return false;
	}

}
