package ru.barsk.moc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.barsk.util.Tools;

public class ScienceActivity extends Activity {

	private LinearLayout layout_main;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_science);
		initialize();
		layout_main.startAnimation(MainActivity.appearance);
	}

	public void initialize() {
		layout_main = (LinearLayout) findViewById(R.id.layout_main);
		int ITLevel = Player.IT.ITLevel();
		Tools.setColoredValue(Tools.round(MainActivity.getIt(Player.ID)), (TextView) findViewById(R.id.each_turn), true, false);
		((TextView) findViewById(R.id.level)).setText(String.valueOf(ITLevel));
		((TextView) findViewById(R.id.to_the_next_level)).setText(String.valueOf(Player.IT.toNextLevel()));
		((TextView) findViewById(R.id.ship_attack)).setText("+" + 10 * ITLevel + "%");
		((TextView) findViewById(R.id.ship_hp)).setText("+" + 10 * ITLevel + "%");
		((TextView) findViewById(R.id.production)).setText("+" + 7.5 * ITLevel + "%");
		findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	public void onBackPressed() {
		layout_main.startAnimation(MainActivity.disappearance);
		MainActivity.delayer.postDelayed(new Runnable() {
			@Override
			public void run() {
				ScienceActivity.super.onBackPressed();
			}
		}, 500);
	}

}
