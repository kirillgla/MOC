package ru.barsk.moc;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;

import ru.barsk.util.BuildingsTreeSet;
import ru.barsk.util.Tools;

public class BuildingsListAdapter extends ArrayAdapter<Building> {
	
	private BuildingsViewActivity context;
	private BuildingsTreeSet nonAdaptedArray;
	private Star observedStar;

	public BuildingsListAdapter(BuildingsViewActivity context, List<Building> src, BuildingsTreeSet nonAdaptedArray) {
		super(context, R.layout.item_buildings_adapter, src);
		this.nonAdaptedArray = nonAdaptedArray;
		this.context = context;
		observedStar = Buffer.star;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Building building = getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_buildings_adapter, null);
		}
		TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
		TextView maintenanceTextView = (TextView) convertView.findViewById(R.id.maintenanceTextView);
		TextView commentTextView = (TextView) convertView.findViewById(R.id.commentTextView);
		TextView levelTextView = (TextView) convertView.findViewById(R.id.levelTextView);
		final Button improve = (Button) convertView.findViewById(R.id.improveButton);
		Button destroy = (Button) convertView.findViewById(R.id.destroy);
		nameTextView.setText(building.getName());
		Tools.setColoredValue(-Math.abs(building.maintenance), maintenanceTextView, true, true);
		commentTextView.setText(building.getComment());
		levelTextView.setText(context.getString(R.string.level) + " " + building.level);
		View.OnClickListener onButtonClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.destroy) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					String title = building.getName();
					String message = context.getString(R.string.destroy2);
					String yes = context.getString(R.string.yes);
					String no = context.getString(R.string.no);
					DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								nonAdaptedArray.remove(building);
								remove(building);
								observedStar.constructionQueue.removeBuildingId(building.typeId);
								notifyDataSetChanged();
								context.initialize();
							}
							dialog.cancel();
						}
					};
					builder.setTitle(title);
					builder.setMessage(message);
					builder.setPositiveButton(yes, onClickListener);
					builder.setNegativeButton(no, onClickListener);
					builder.setCancelable(true);
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					String title = building.getName();
					String message = context.getString(R.string.improve2)
							+ "\n" + context.getString(R.string.next_level) + " " + (building.level + 1)
							+ "\n" + context.getString(R.string.cost) + " " + Building.getCost(building.typeId) * 2;
					String yes = context.getString(R.string.yes);
					String no = context.getString(R.string.no);
					DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								observedStar.constructionQueue.offer(new BuildingTemplate(Building.getCost(building.typeId) * 2, building.typeId, building.level + 1, Building.getName(building.typeId, building.level + 1)));
								improve.setEnabled(false);
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
				}
			}
		};
		destroy.setOnClickListener(onButtonClickListener);
		improve.setOnClickListener(onButtonClickListener);
		improve.setEnabled(true);
		if (building.typeId == Building.ID_BANK) {
			improve.setEnabled(false);
		}
		if (building.level != 1) {
			improve.setEnabled(false);
		}
		if (observedStar.constructionQueue.contains(building.typeId)) {
			improve.setEnabled(false);
		}
		return convertView;
	}

}
