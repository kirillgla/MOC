package ru.barsk.moc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.NoSuchElementException;

import ru.barsk.util.Lib;
import ru.barsk.util.Tools;

/**
 * При создании активности обязательно
 * поместить в Buffer.star нужную звезду.
 */
public class ConstructionSelectionActivity extends Activity {

	private Star observedStar;
	private LinearLayout layout_main;
	private LinearLayout shipContainer;
	private LinearLayout buildingsContainer;
	private boolean shipsExpanded;
	private boolean buildingsExpanded;
	private View[] expandedShips;
	private View[] expandedBuildings;
	private static final String LOG_TAG = "ConstrSelActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_construction_selection);
			shipsExpanded = false;
			buildingsExpanded = false;
			initialize();
			layout_main.startAnimation(MainActivity.appearance);
		}
		catch (Exception e) {
			Tools.analyze(MainActivity.context, e);
			finish();
		}
	}
	
	public void initialize() {
		observedStar = Buffer.star;
		layout_main = (LinearLayout) findViewById(R.id.layout_main);
		Button back = (Button) findViewById(R.id.back);
		Button ships = (Button) findViewById(R.id.ships);
		Button buildings = (Button) findViewById(R.id.buildings);
		shipContainer = (LinearLayout) findViewById(R.id.ships_container);
		buildingsContainer = (LinearLayout) findViewById(R.id.buildings_container);
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.back:
						onBackPressed();
						break;
					case R.id.ships:
						shipsExpanded = !shipsExpanded;
						shipContainer.removeAllViewsInLayout();
						if (shipsExpanded) {
							for (View view: expandedShips) {
								shipContainer.addView(view);
							}
						}
						else {
							shipContainer.addView(LayoutInflater.from(MainActivity.context).inflate(R.layout.item_null, null));
						}
						break;
					case R.id.buildings:
						buildingsExpanded = !buildingsExpanded;
						buildingsContainer.removeAllViewsInLayout();
						if (buildingsExpanded && expandedBuildings.length != 0) {
							for (View view: expandedBuildings) {
								buildingsContainer.addView(view);
							}
						}
						else {
							buildingsContainer.addView(LayoutInflater.from(MainActivity.context).inflate(R.layout.item_null, null));
						}
						if (expandedBuildings.length == 0) {
							Toast.makeText(ConstructionSelectionActivity.this, getString(R.string.no_buildings_left), Toast.LENGTH_SHORT).show();
						}
						break;
				}
			}
		};
		back.setOnClickListener(onClickListener);
		ships.setOnClickListener(onClickListener);
		buildings.setOnClickListener(onClickListener);
		expandedShips = new View[6];
		for (int i = 0; i < expandedShips.length; i++) {
			final int index = i;
			LayoutInflater inflater = LayoutInflater.from(this);
			expandedShips[i] = inflater.inflate(R.layout.item_ship_construction_adapter, null);
			TextView nameTextView = (TextView) expandedShips[i].findViewById(R.id.nameTextView);
			TextView maintenanceTextView = (TextView) expandedShips[i].findViewById(R.id.maintenanceTextView);
			TextView attackTextView = (TextView) expandedShips[i].findViewById(R.id.attackTextView);
			TextView hpTextView = (TextView) expandedShips[i].findViewById(R.id.hpTextView);
			TextView speedTextView = (TextView) expandedShips[i].findViewById(R.id.speedTextView);
			TextView canColonizeTextView = (TextView) expandedShips[i].findViewById(R.id.canColonizeTextView);
			TextView costTextView = (TextView) expandedShips[i].findViewById(R.id.costTextView);
			ImageView shipImageView = (ImageView) expandedShips[i].findViewById(R.id.shipImageView);
			Button build = (Button) expandedShips[i].findViewById(R.id.build);
			final Ship ship = Lib.ShipTemplates.getShipTemplate(i).construct(Player.ID, new Coordinates((byte) 0, (byte) 0));
			nameTextView.setText(ship.name);
			maintenanceTextView.setText(getString(R.string.maintenance_short) + " " + Tools.round(ship.maintenance, (byte) 2) + "$");
			attackTextView.setText(getString(R.string.attack) + " " + Tools.round(ship.getAttack()));
			hpTextView.setText(getString(R.string.hp) + " " + Tools.round(ship.getMaxHp()));
			speedTextView.setText(getString(R.string.speed) + " " + Tools.round(ship.speed));
			canColonizeTextView.setText(getString(R.string.canColonize) + " " + (ship.canColonize ? getString(R.string.yes) : getString(R.string.no)));
			costTextView.setText(getString(R.string.cost) + " " + Tools.round(Lib.ShipTemplates.getShipTemplate(i).initialProductionCost));
			shipImageView.setImageResource(MainActivity.getShipDrawableId(ship.base, Player.colorId));
			build.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ConstructionSelectionActivity.this);
					String title = ship.name;
					String message = getString(R.string.build2);
					String yes = getString(R.string.yes);
					String no = getString(R.string.no);
					DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								observedStar.constructionQueue.offer(Lib.ShipTemplates.getShipTemplate(index));
								for (View view: expandedShips) {
									Button build = (Button) view.findViewById(R.id.build);
									build.setEnabled(observedStar.constructionQueue.hasFreeSlots());
								}
								for (View view: expandedBuildings) {
									Button build = (Button) view.findViewById(R.id.build);
									build.setEnabled(observedStar.constructionQueue.hasFreeSlots());
								}
							}
							dialog.cancel();
						}
					};
					builder.setTitle(title);
					builder.setMessage(message);
					builder.setPositiveButton(yes, listener);
					builder.setNegativeButton(no, listener);
					builder.setCancelable(true);
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
				}
			});
			build.setEnabled(observedStar.constructionQueue.hasFreeSlots());
		}
		initializeBuildings();
	}
	
	public boolean shouldShowBuilding(int index) {
		if (observedStar.buildings.contains(index)) {
			return false;
		}
		if (observedStar.constructionQueue.contains(index)) {
			return false;
		}
		return true;
	}
	
	public int getNumberOfConstructableBuildings() {
		int result = 0;
		for (int i = 0; i < Building.numberOfTypes; i++) {
			if (shouldShowBuilding(i)) {
				result++;
			}
		}
		return result;
	}

	@Override
	public void onBackPressed() {
		layout_main.startAnimation(MainActivity.disappearance);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				ConstructionSelectionActivity.super.onBackPressed();
				StarViewActivity.context.initialize();
				StarViewActivity.layout_main.startAnimation(MainActivity.appearance);
			}
		}, 500);
	}
	
	public void initializeBuildings() {
		expandedBuildings = new View[getNumberOfConstructableBuildings()];
		int j = 0;
		for (int i = 0; i < Building.numberOfTypes; i++) {
			if (!shouldShowBuilding(i)) {
				continue;
			}
			final int index = i;
			final int jndex = j;
			LayoutInflater inflater = LayoutInflater.from(this);
			expandedBuildings[jndex] = inflater.inflate(R.layout.item_building_construction_adapter, null);
			TextView nameTextView = (TextView) expandedBuildings[jndex].findViewById(R.id.nameTextView);
			TextView commentTextView = (TextView) expandedBuildings[jndex].findViewById(R.id.commentTextView);
			TextView costTextView = (TextView) expandedBuildings[jndex].findViewById(R.id.costTextView);
			TextView maintenanceTextView = (TextView) expandedBuildings[jndex].findViewById(R.id.maintenanceTextView);
			Button build = (Button) expandedBuildings[jndex].findViewById(R.id.build);
			final Building building = Building.makeBuilding(i, 1);
			if (building == null) {
				throw new NoSuchElementException("building not found");
			}
			nameTextView.setText(building.getName());
			commentTextView.setText(building.getComment());
			maintenanceTextView.setText(getString(R.string.maintenance) + " " + Tools.round(building.maintenance, (byte) 2) + "$");
			costTextView.setText(getString(R.string.cost) + " " + Building.getCost(i));
			build.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ConstructionSelectionActivity.this);
					String title = building.getName();
					String message = getString(R.string.build2);
					String yes = getString(R.string.yes);
					String no = getString(R.string.no);
					DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								observedStar.constructionQueue.offer(new BuildingTemplate(Building.getCost(index), index, 1, Building.getName(index, 1)));
								expandedBuildings[jndex].startAnimation(MainActivity.disappearance_right);
								MainActivity.delayer.postDelayed(new Runnable() {
									@Override
									public void run() {
										initializeBuildings();
									}
								}, 500);
							}
							dialog.cancel();
						}
					};
					builder.setTitle(title);
					builder.setMessage(message);
					builder.setPositiveButton(yes, listener);
					builder.setNegativeButton(no, listener);
					builder.setCancelable(true);
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
				}
			});
			build.setEnabled(observedStar.constructionQueue.hasFreeSlots());
			j++;
		}
		buildingsContainer.removeAllViewsInLayout();
		if (buildingsExpanded && expandedBuildings.length != 0) {
			for (View view: expandedBuildings) {
				buildingsContainer.addView(view);
			}
		}
		else {
			buildingsContainer.addView(LayoutInflater.from(MainActivity.context).inflate(R.layout.item_null, null));
		}
	}

}
