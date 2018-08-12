package ru.barsk.moc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RadioButton;

import ru.barsk.util.ConstructionQueue;
import ru.barsk.util.Tools;

/**
 * Перед запуском обязательно:
 * - поместить в Buffer.star необходимую звезду.
 */
public class ConstructionQueueActivity extends Activity {

	private Star observedStar;
	private LinearLayout chooser;
	private LinearLayout layout_main;
	private LinearLayout container;
	private View[] content;
	private static final String LOG_TAG = "ConstrQueueActiv";
	private RadioButton tradeChoice;
	private RadioButton storageChoice;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_construction_queue);
		initialize();
		layout_main.startAnimation(MainActivity.appearance);
	}

	public void initialize() {
		layout_main = (LinearLayout) findViewById(R.id.layout_main);
		if (chooser == null) {
			chooser = (LinearLayout) findViewById(R.id.no_construction_chooser);
			tradeChoice = (RadioButton) chooser.findViewById(R.id.tradeChoice);
			storageChoice = (RadioButton) chooser.findViewById(R.id.storeChoice);
		}
		observedStar = Buffer.star;
		if (observedStar.shouldFillStorage) {
			storageChoice.setChecked(true);
		}
		else {
			tradeChoice.setChecked(true);
		}
		final ConstructionQueue observedQueue = observedStar.constructionQueue;
		container = (LinearLayout) findViewById(R.id.container);
		container.removeView(chooser);
		final LayoutInflater inflater = LayoutInflater.from(this);
		content = new View[observedQueue.size()];
		for (int i = 0; i < observedQueue.size(); i++) {
			final int j = i; //to feed anonymous class with
			Object current = observedQueue.get(i);
			if (current instanceof ShipTemplate) {
				final ShipTemplate shipTemplate = (ShipTemplate) current;
				Ship constructed = shipTemplate.construct(Player.ID, new Coordinates((byte) 0, (byte) 0));
				content[i] = inflater.inflate(R.layout.item_ship_in_queue, null);
				TextView nameTextView = (TextView) content[i].findViewById(R.id.nameTextView);
				TextView maintenanceTextView = (TextView) content[i].findViewById(R.id.maintenanceTextView);
				TextView attackTextView = (TextView) content[i].findViewById(R.id.attackTextView);
				TextView hpTextView = (TextView) content[i].findViewById(R.id.hpTextView);
				TextView speedTextView = (TextView) content[i].findViewById(R.id.speedTextView);
				TextView canColonizeTextView = (TextView) content[i].findViewById(R.id.canColonizeTextView);
				TextView productionTextView = (TextView) content[i].findViewById(R.id.productionTextView);
				ImageView shipImageView = (ImageView) content[i].findViewById(R.id.shipImageView);
				SeekBar progressSeekBar = (SeekBar) content[i].findViewById(R.id.progressSeekBar);
				Button move_up = (Button) content[i].findViewById(R.id.move_up);
				Button move_down = (Button) content[i].findViewById(R.id.move_down);
				ImageButton delete = (ImageButton) content[i].findViewById(R.id.delete);
				ImageButton rush = (ImageButton) content[i].findViewById(R.id.rushProductionButton);
				nameTextView.setText(constructed.name);
				maintenanceTextView.setText(getString(R.string.maintenance_short) + " " + constructed.maintenance + "$");
				attackTextView.setText(getString(R.string.attack) + " " + constructed.getAttack());
				hpTextView.setText(getString(R.string.hp) + " " + constructed.getMaxHp());
				speedTextView.setText(getString(R.string.speed) + " " + constructed.speed);
				canColonizeTextView.setText(getString(R.string.canColonize) + " " + (constructed.canColonize? getString(R.string.yes) : getString(R.string.no)));
				productionTextView.setText(getString(R.string.cost) + " " + Tools.round(shipTemplate.initialProductionCost - shipTemplate.productionCost) + "/" + Tools.round(shipTemplate.initialProductionCost));
				shipImageView.setImageResource(MainActivity.getShipDrawableId(constructed.base, Player.colorId));
				progressSeekBar.setEnabled(false);
				progressSeekBar.setProgress((int) Math.round((shipTemplate.initialProductionCost - shipTemplate.productionCost) * 100d / shipTemplate.initialProductionCost));
				View.OnClickListener onButtonClickListener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (v.getId()) {
							case R.id.delete: {
								AlertDialog.Builder builder = new AlertDialog.Builder(ConstructionQueueActivity.this);
								String title = observedQueue.get(j).name;
								String message = getString(R.string.remove2);
								String yes = getString(R.string.yes);
								String no = getString(R.string.no);
								DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (which == DialogInterface.BUTTON_POSITIVE) {
											content[j].startAnimation(MainActivity.disappearance_left);
											MainActivity.delayer.postDelayed(new Runnable() {
												@Override
												public void run() {
													double productionSpent = shipTemplate.initialProductionCost - shipTemplate.productionCost;
													observedStar.setProductionStored(observedStar.getProductionStored() + productionSpent);
													observedQueue.remove(j);
													initialize();
												}
											}, 500);
										}
										dialog.cancel();
									}
								};
								builder.setTitle(title);
								builder.setMessage(message);
								builder.setPositiveButton(yes, onClickListener);
								builder.setNegativeButton(no, onClickListener);
								builder.setCancelable(true);
								AlertDialog dialog = builder.create();
								dialog.show();
								break;
							}
							case R.id.move_up: {
								ArtificialConstruction buffer = observedQueue.queue[j];
								observedQueue.queue[j] = observedQueue.queue[j - 1];
								observedQueue.queue[j - 1] = buffer;
								initialize();
								break;
							}
							case R.id.move_down: {
								ArtificialConstruction buffer = observedQueue.queue[j];
								observedQueue.queue[j] = observedQueue.queue[j + 1];
								observedQueue.queue[j + 1] = buffer;
								initialize();
								break;
							}
							case R.id.rushProductionButton: {
								observedStar.rushProduction(j);
								initialize();
								break;
							}
						}
					}
				};
				move_up.setOnClickListener(onButtonClickListener);
				move_down.setOnClickListener(onButtonClickListener);
				delete.setOnClickListener(onButtonClickListener);
				rush.setOnClickListener(onButtonClickListener);
				move_up.setEnabled(j != 0);
				move_down.setEnabled(j != observedQueue.size() - 1);
				rush.setEnabled(observedStar.getProductionStored() != 0);
			}
			else {
				// => current instanceof BuildingTemplate
				final BuildingTemplate buildingTemplate = (BuildingTemplate) current;
				final Building constructed = buildingTemplate.construct(Player.ID, new Coordinates((byte) 0, (byte) 0));
				content[i] = inflater.inflate(R.layout.item_building_in_queue, null);
				TextView nameTextView = (TextView) content[i].findViewById(R.id.nameTextView);
				TextView commentTextView = (TextView) content[i].findViewById(R.id.commentTextView);
				TextView productionTextView = (TextView) content[i].findViewById(R.id.productionTextView);
				TextView maintenanceTextView = (TextView) content[i].findViewById(R.id.maintenanceTextView);
				SeekBar progressSeekBar = (SeekBar) content[i].findViewById(R.id.progressSeekBar);
				Button move_up = (Button) content[i].findViewById(R.id.move_up);
				Button move_down = (Button) content[i].findViewById(R.id.move_down);
				ImageButton delete = (ImageButton) content[i].findViewById(R.id.delete);
				ImageButton rush = (ImageButton) content[i].findViewById(R.id.rushProductionButton);
				nameTextView.setText(observedQueue.get(i).name);
				commentTextView.setText(constructed.getComment());
				productionTextView.setText(getString(R.string.cost) + " " + Tools.round(buildingTemplate.initialProductionCost - buildingTemplate.productionCost) + "/" + Tools.round(buildingTemplate.initialProductionCost));
				maintenanceTextView.setText(getString(R.string.maintenance_short) + " " + constructed.maintenance + "$");
				progressSeekBar.setEnabled(false);
				progressSeekBar.setProgress((int) Math.round((buildingTemplate.initialProductionCost - buildingTemplate.productionCost) / buildingTemplate.initialProductionCost * 100d));
				View.OnClickListener onButtonClickListener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (v.getId()) {
							case R.id.delete: {
								AlertDialog.Builder builder = new AlertDialog.Builder(ConstructionQueueActivity.this);
								String title = buildingTemplate.name;
								String message = getString(R.string.remove2);
								String yes = getString(R.string.yes);
								String no = getString(R.string.no);
								DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (which == DialogInterface.BUTTON_POSITIVE) {
											content[j].startAnimation(MainActivity.disappearance_left);
											MainActivity.delayer.postDelayed(new Runnable() {
												@Override
												public void run() {
													double productionSpent = buildingTemplate.initialProductionCost - buildingTemplate.productionCost;
													observedStar.setProductionStored(observedStar.getProductionStored() + productionSpent);
													observedQueue.remove(j);
													initialize();
												}
											}, 500);
										}
										dialog.cancel();
									}
								};
								builder.setTitle(title);
								builder.setMessage(message);
								builder.setPositiveButton(yes, onClickListener);
								builder.setNegativeButton(no, onClickListener);
								AlertDialog dialog = builder.create();
								dialog.show();
								break;
							}
							case R.id.move_up: {
								ArtificialConstruction buffer = observedQueue.queue[j];
								observedQueue.queue[j] = observedQueue.queue[j - 1];
								observedQueue.queue[j - 1] = buffer;
								initialize();
								break;
							}
							case R.id.move_down: {
								ArtificialConstruction buffer = observedQueue.queue[j];
								observedQueue.queue[j] = observedQueue.queue[j + 1];
								observedQueue.queue[j + 1] = buffer;
								initialize();
								break;
							}
							case R.id.rushProductionButton: {
								observedStar.rushProduction(j);
								initialize();
								break;
							}
						}
					}
				};
				move_up.setEnabled(i != 0);
				move_down.setEnabled(i != observedQueue.size() - 1);
				rush.setEnabled(observedStar.getProductionStored() != 0);
				move_up.setOnClickListener(onButtonClickListener);
				move_down.setOnClickListener(onButtonClickListener);
				rush.setOnClickListener(onButtonClickListener);
				delete.setOnClickListener(onButtonClickListener);
			}
		}
		if (content.length == 0) {
			content = new View[2];
			content[0] = createNoItemsMessage();
			content[1] = chooser;
		}
		container.removeAllViewsInLayout();
		for (View view: content) {
			container.addView(view);
		}
		Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	public void onBackPressed() {
		layout_main.startAnimation(MainActivity.disappearance);
		if (observedStar.constructionQueue.size() == 0) {
			observedStar.shouldFillStorage = storageChoice.isChecked();
		}
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				ConstructionQueueActivity.super.onBackPressed();
				StarViewActivity.context.initialize();
				StarViewActivity.layout_main.startAnimation(MainActivity.appearance);
			}
		}, 500);
	}

	public View createNoItemsMessage() {
		TextView result;
		result = new TextView(this);
		result.setText(getString(R.string.no_items));
		result.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.text_color_base));
		result.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		result.setBackgroundColor(ContextCompat.getColor(MainActivity.context, R.color.background_group));
		TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 10, 10, 10);
		result.setLayoutParams(params);
		result.setPadding(10, 10, 10, 10);
		result.setGravity(Gravity.CENTER);
		return result;
	}

}
