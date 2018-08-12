package ru.barsk.moc;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;

import ru.barsk.util.BuildingsTreeSet;

/**
 * При создании активности обязательно:
 * - поместить в Buffer.star нужную звезду.
 */
public class BuildingsViewActivity extends Activity {

	private static final String LOG_TAG = "BuildingsViewActivity";
	private BuildingsTreeSet set;
	private LinearLayout layout_main;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buildings_view);
		initialize();
		layout_main.startAnimation(MainActivity.appearance);
	}

	public void initialize() {
		Star observedStar = Buffer.star;
		layout_main = (LinearLayout) findViewById(R.id.layout_main);
		TextView header = (TextView) findViewById(R.id.system);
		header.setText(observedStar.name);
		Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		set = observedStar.buildings;
		if (set.isEmpty()) {
			layout_main.addView(makeNoBuildingsMessage(), 3);
		}
		else {
			ListView buildingsListView = (ListView) findViewById(R.id.buildings_list_view);
			Building[] buildingsArray = observedStar.buildings.toArray(new Building[observedStar.buildings.size()]);
			List<Building> buildingsList = new ArrayList<>();
			for (Building building: buildingsArray) {
				buildingsList.add(building);
			}
			BuildingsListAdapter adapter = new BuildingsListAdapter(this, buildingsList, observedStar.buildings);
			buildingsListView.setAdapter(adapter);
		}
	}

	@Override
	public void onBackPressed() {
		layout_main.startAnimation(MainActivity.disappearance);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				BuildingsViewActivity.super.onBackPressed();
				StarViewActivity.layout_main.startAnimation(MainActivity.appearance);
				StarViewActivity.context.initialize();
			}
		}, 500);
	}

	public TextView makeNoBuildingsMessage() {
		TextView result;
		result = new TextView(MainActivity.context);
		result.setText(getString(R.string.no_buildings));
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
