package ru.barsk.moc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TurnEventListAdapter extends ArrayAdapter<TurnEvent> {

	public TurnEventListAdapter(Context context, ArrayList<TurnEvent> nonAdaptedArray) {
		super(context, R.layout.item_event_adapter, nonAdaptedArray);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TurnEvent event = getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(MainActivity.context).inflate(R.layout.item_event_adapter, null);
		}
		//Конструктор сам произведёт все необходимые операции
		new OnTurnEventItemClickListener(event, convertView);
		return convertView;
	}

	public static class OnTurnEventItemClickListener implements View.OnClickListener {

		private TurnEvent associatedEvent;

		public OnTurnEventItemClickListener(TurnEvent event, View listened) {
			associatedEvent = event;
			ImageView eventImage = (ImageView) listened.findViewById(R.id.eventImage);
			TextView eventText = (TextView) listened.findViewById(R.id.eventText);
			ImageView eventStar = (ImageView) listened.findViewById(R.id.toStar);
			CheckBox checked = (CheckBox) listened.findViewById(R.id.close);
			switch (event.tag) {
				case BUILDING_COMPLETE:
					eventImage.setImageResource(R.drawable.alert_building);
					break;
				case SHIP_COMPLETE:
					eventImage.setImageResource(R.drawable.alert_ship);
					break;
				case ENEMY_SYSTEM_CAPTURED:
				case COLONY_FOUND:
					eventImage.setImageResource(R.drawable.alert_colony);
					break;
				case COLONY_GROWN:
					eventImage.setImageResource(R.drawable.alert_population);
					break;
				case POPULATION_DIE_OUT:
				case OUR_SYSTEM_CAPTURED:
					eventImage.setImageResource(R.drawable.alert_warning);
					break;
				case ECONOMY_WARNING:
					eventImage.setImageResource(R.drawable.alert_economy);
					break;
				case COLONY_IDLE:
					eventImage.setImageResource(R.drawable.alert_colony_idle);
					break;
				case SHIP_REACHED_DESTINATION:
					eventImage.setImageResource(R.drawable.alert_ship2);
					break;
				case COLONY_DIED:
					eventImage.setImageResource(R.drawable.alert_colony_dead);
			}
			eventText.setText(event.content);
			checked.setChecked(associatedEvent.checked);
			if (event.hasAssociatedStar()) {
				eventStar.setOnClickListener(this);
			}
			checked.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.toStar) {
				EventsViewActivity.context.onBackPressed();
				MainActivity.delayer.postDelayed(new Runnable() {
					@Override
					public void run() {
						associatedEvent.toStar();
					}
				}, 500);
			}
			else {
				CheckBox checkBox = (CheckBox) v;
				associatedEvent.checked = checkBox.isChecked();
			}
		}

	}
}
