package ru.barsk.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import ru.barsk.moc.Coordinates;
import ru.barsk.moc.MainActivity;
import ru.barsk.moc.Player;
import ru.barsk.moc.StarViewActivity;

/**
 * Класс, использующийся для отображения
 * звёзд на главном экране и MapViewActivity.
 * TextView, знающий позицию отображаемой звезды.
 */
public class StarTextView extends TextView {

	public Coordinates coordinates;

	public StarTextView(Context context) {
		super(context);
		coordinates = new Coordinates((byte) 0, (byte) 0);
	}

	public StarTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		coordinates = new Coordinates((byte) 0, (byte) 0);
	}

	public StarTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		coordinates = new Coordinates((byte) 0, (byte) 0);
	}

	public static final OnClickListener SYSTEM_SELECTION_LISTENER = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!(v instanceof StarTextView)) {
				throw new ClassCastException("Listener given not to starTextView!");
			}
			StarTextView starTextView = (StarTextView) v;
			byte x = starTextView.coordinates.x;
			byte y = starTextView.coordinates.y;
			if (x == MainActivity.selectedStarCoordinates.x && y == MainActivity.selectedStarCoordinates.y && MainActivity.galaxy[x][y].type != 0 && MainActivity.galaxy[x][y].getOwnerId() == Player.ID) {
				Intent intent = new Intent(MainActivity.context, StarViewActivity.class);
				MainActivity.context.startActivity(intent);
			}
			else {
				int selectionDrawableId;
				selectionDrawableId = MainActivity.getSelectionDrawableId((byte) 0, MainActivity.currentSize);
				MainActivity.screenTexts[MainActivity.selectedStarCoordinates.x][MainActivity.selectedStarCoordinates.y].setCompoundDrawablesWithIntrinsicBounds(0, selectionDrawableId, 0, 0);
				MainActivity.selectedStarCoordinates.x = x;
				MainActivity.selectedStarCoordinates.y = y;
				selectionDrawableId = MainActivity.getSelectionDrawableId((byte) 1, MainActivity.currentSize);
				MainActivity.screenTexts[MainActivity.selectedStarCoordinates.x][MainActivity.selectedStarCoordinates.y].setCompoundDrawablesWithIntrinsicBounds(0, selectionDrawableId, 0, 0);
				MainActivity.context.invalidateScreen(false);
			}
		}
	};

	public static OnClickListener makeDestinationSelector(@NonNull final Coordinates to) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!(v instanceof StarTextView)) {
					throw new ClassCastException("Listener given not to starTextView!");
				}
				StarTextView starTextView = (StarTextView) v;
				to.x = starTextView.coordinates.x;
				to.y = starTextView.coordinates.y;
				MainActivity.context.showGalaxy(); // Можно ли сделать это рационально?
			}
		};
	}

	public void setCoordinates(Coordinates coordinates) {
		this.coordinates.x = coordinates.x;
		this.coordinates.y = coordinates.y;
	}

}
