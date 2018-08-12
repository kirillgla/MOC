package ru.barsk.moc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import ru.barsk.view.StarTextView;

public class MapViewActivity extends Activity {

	LinearLayout view_main;
	TableLayout table;
	Button back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		initialize();
		view_main.startAnimation(MainActivity.appearance);
		StarTextView[][] map = makeMap();
		final TableRow[] adaptedMap = makeRows(map);
		setListeners(map);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (TableRow tableRow: adaptedMap) {
					table.addView(tableRow);
				}
			}
		}, 500);
	}

	public void initialize() {
		view_main = (LinearLayout) findViewById(R.id.view_main);
		table = (TableLayout) findViewById(R.id.table);
		back = (Button) findViewById(R.id.back);
	}

	public void setListeners(StarTextView[][] map) {
		for (StarTextView[] starTextViews: map) {
			for (StarTextView starTextView: starTextViews) {
				starTextView.setOnClickListener(SYSTEM_FINDING_LISTENER);
			}
		}
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	public StarTextView[][] makeMap() {
		StarTextView[][] result;
		result = new StarTextView[MainActivity.galaxySize][MainActivity.galaxySize];
		for (byte x = 0; x < result.length; x++) {
			for (byte y = 0; y < result[x].length; y++) {
				int imageId = getImageId(MainActivity.galaxy[x][y]);
				result[x][y] = new StarTextView(MainActivity.context);
				TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				result[x][y].setLayoutParams(params);
				result[x][y].setBackgroundResource(imageId);
				result[x][y].setCoordinates(new Coordinates(x, y));
			}
		}
		return result;
	}

	public TableRow[] makeRows(StarTextView[][] map) {
		TableRow[] result;
		result = new TableRow[map.length];
		for (int x = 0; x < map.length; x++) {
			result[x] = new TableRow(MainActivity.context);
			for (int y = 0; y < map[x].length; y++) {
				result[x].addView(map[x][y]);
			}
		}
		return result;
	}

	public static int getImageId(Star star) {
		if (star.type == 0) {
			return R.drawable.star_image_clear;
		}
		byte owner = star.getOwnerId();
		byte colorId;
		switch (owner) {
			case 0:
				colorId = 0;
				break;
			case Player.ID:
				colorId = Player.colorId;
				break;
			default:
				colorId = MainActivity.AIs[owner - 2].colorId;
		}
		switch (colorId) {
			case 0: return R.drawable.star_image_0;
			case 1: return R.drawable.star_image_1;
			case 2: return R.drawable.star_image_2;
			case 3: return R.drawable.star_image_3;
			case 4: return R.drawable.star_image_4;
			case 5: return R.drawable.star_image_5;
			case 6: return R.drawable.star_image_6;
			case 7: return R.drawable.star_image_7;
			default: return -1;
		}
	}

	@Override
	public void onBackPressed() {
		view_main.startAnimation(MainActivity.disappearance);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				MapViewActivity.super.onBackPressed();
			}
		}, 500);
	}

	public final View.OnClickListener SYSTEM_FINDING_LISTENER = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!(v instanceof StarTextView)) {
				throw new ClassCastException("Listener given not to starTextView!");
			}
			StarTextView starTextView = (StarTextView) v;
			byte x = starTextView.coordinates.x;
			byte y = starTextView.coordinates.y;
			MainActivity.selectedStarCoordinates.x = x;
			MainActivity.selectedStarCoordinates.y = y;
			MapViewActivity.this.onBackPressed();
			MainActivity.context.invalidateScreen(true);
			MainActivity.context.focusOnStar(MainActivity.selectedStarCoordinates);
		}
	};

}
