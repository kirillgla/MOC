package ru.barsk.moc;

import android.util.Log;

//TODO use this class
public class TurnEvent {

	private static final String LOG_TAG = "TurnEvent";

	public final String content;
	public final Star associatedStar;
	public final Tag tag;
	public boolean checked;

	public void toStar() {
		if (associatedStar != null) {
			MainActivity.selectedStarCoordinates.x = associatedStar.coordinates.x;
			MainActivity.selectedStarCoordinates.y = associatedStar.coordinates.y;
			MainActivity.context.invalidateScreen(true);
			MainActivity.context.focusOnStar(associatedStar.coordinates.x, associatedStar.coordinates.y);
		}
		else {
			Log.w(LOG_TAG, "called toStar() while associatedStar == null!");
		}
	}

	@Override
	public String toString() {
		return content;
	}

	public TurnEvent(Tag tag, Star associatedStar, String content) {
		this.tag = tag;
		this.associatedStar = associatedStar;
		this.content = content;
		this.checked = false;
	}

	public boolean hasAssociatedStar() {
		return this.associatedStar != null;
	}

	public enum Tag {
		BUILDING_COMPLETE,
		SHIP_COMPLETE,
		COLONY_FOUND,
		COLONY_GROWN,
		ENEMY_SYSTEM_CAPTURED,
		OUR_SYSTEM_CAPTURED,
		POPULATION_DIE_OUT,
		ECONOMY_WARNING,
		COLONY_IDLE,
		SHIP_REACHED_DESTINATION,
		COLONY_DIED,
		RESEARCH_COMPLETE
	}

}
