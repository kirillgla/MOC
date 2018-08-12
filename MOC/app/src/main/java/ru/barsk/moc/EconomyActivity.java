package ru.barsk.moc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import ru.barsk.util.Tools;

public class EconomyActivity extends Activity {
	
	private View layout_main;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_economy);
		initialize();
		layout_main.startAnimation(MainActivity.appearance);
	}
	
	public void initialize() {
		layout_main = findViewById(R.id.layout_main);
		MainActivity.context.invalidateIncomes();
		Tools.setColoredValue(Tools.round(Player.income, (byte) 2), (TextView) findViewById(R.id.total), true, true);
		Tools.setColoredValue(Tools.round(MainActivity.getTaxIncome(Player.ID), (byte) 2), (TextView) findViewById(R.id.taxTextView), true, true);
		Tools.setColoredValue(Tools.round(MainActivity.getTradeIncome(Player.ID), (byte) 2), (TextView) findViewById(R.id.tradeTextView), true, true);
		Tools.setColoredValue(Tools.round(-MainActivity.getShipsCost(Player.ID), (byte) 2), (TextView) findViewById(R.id.shipMaintTextView), true, true);
		Tools.setColoredValue(Tools.round(-MainActivity.getBuildingsCost(Player.ID), (byte) 2), (TextView) findViewById(R.id.buildingMaintTextView), true, true);
		findViewById(R.id.done).setOnClickListener(new OnClickListener() {
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
				EconomyActivity.super.onBackPressed();
			}
		}, 500);
	}
	
}
