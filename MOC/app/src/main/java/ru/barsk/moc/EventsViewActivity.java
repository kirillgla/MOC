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
import android.widget.Toast;
import android.widget.CheckBox;
import android.content.SharedPreferences;

public class EventsViewActivity extends Activity {

	private static final String LOG_TAG = "EventsViewActivity";
	public static EventsViewActivity context;
	private LinearLayout view_main;
	private CheckBox doNotShowThisAgainCheckBox;
	private ListView eventsListView;
	private TurnEventListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events_view);
		initialize();
		view_main.startAnimation(MainActivity.appearance);
		adapter = new TurnEventListAdapter(MainActivity.context, MainActivity.events);
		eventsListView.setAdapter(adapter);
		if (MainActivity.events.size() == 0) {
			view_main.addView(makeNoEventsMessage(), 1);
		}
	}

	private void initialize() {
		context = EventsViewActivity.this;
		view_main = (LinearLayout) findViewById(R.id.view_main);
		TextView turn = (TextView) findViewById(R.id.turn);
		Button back = (Button) findViewById(R.id.back);
		Button remove = (Button) findViewById(R.id.delete_checked);
		doNotShowThisAgainCheckBox = (CheckBox) findViewById(R.id.doNotShowAgainCheckBox);
		doNotShowThisAgainCheckBox.setChecked(!Settings.doShowTurnEvents);
		eventsListView = (ListView) findViewById(R.id.eventListView);
		turn.setText(getString(R.string.turn) + " " + MainActivity.turnLong);
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.back) {
					onBackPressed();
				}
				else {
					//remove all checked
					int i = 0;
					while (i < MainActivity.events.size()) {
						if (MainActivity.events.get(i).checked) {
							MainActivity.events.remove(i);
						}
						else {
							i++;
						}
					}
					adapter.notifyDataSetChanged();
					if (adapter.isEmpty()) {
						view_main.addView(makeNoEventsMessage(), 1);
					}
				}
			}
		};
		back.setOnClickListener(listener);
		remove.setOnClickListener(listener);
	}

	@Override
	public void onBackPressed() {
		Settings.doShowTurnEvents = !doNotShowThisAgainCheckBox.isChecked();
		MainActivity.context.saveSettings();
		view_main.startAnimation(MainActivity.disappearance);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				EventsViewActivity.super.onBackPressed();
				MainActivity.context.invalidateScreen(true);
			}
		}, 500);
	}

	public TextView makeNoEventsMessage() {
		TextView result;
		result = new TextView(MainActivity.context);
		result.setText(getString(R.string.no_events));
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
