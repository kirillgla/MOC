package ru.barsk.moc;

import android.support.annotation.NonNull;

public class Building implements Comparable<Building> {

	public static final int ID_FARM = 0;
	public static final int ID_FACTORY = 1;
	public static final int ID_RESEARCH_CENTER = 2;
	public static final int ID_CITY = 3;
	public static final int ID_BANK = 4;
	public static final int numberOfTypes = 5;

	public final int typeId;
	public int level;
	public double maintenance;
	private String name;
	private String commentPrefix;
	private String comment;

	public static Building makeFarm(int level) {
		return new Building(
				ID_FARM,
				level,
				(level == 1? 0.25: 0.5),
				MainActivity.context.getString(R.string.farm),
				(level == 1? "+0.5 ": "+1"),
				MainActivity.context.getString(R.string.message_farm)
		);
	}
	public static Building makeFactory(int level) {
		return new Building(
				ID_FACTORY,
				level,
				(level == 1? 0.5: 1),
				MainActivity.context.getString(R.string.factory),
				(level == 1? "+0.5 ": "+1"),
				MainActivity.context.getString(R.string.message_factory)
		);
	}
	public static Building makeResearchCenter(int level) {
		return new Building(
				ID_RESEARCH_CENTER,
				level,
				(level == 1? 0.75: 1.5),
				MainActivity.context.getString(R.string.research_center),
				(level == 1? "+0.5 ": "+1"),
				MainActivity.context.getString(R.string.message_research_center)
		);
	}
	public static Building makeCity(int level) {
		return new Building(
				ID_CITY,
				level,
				(level == 1? 0.25: 0.5),
				MainActivity.context.getString(R.string.city),
				(level == 1? "+2.5 ": "+5"),
				MainActivity.context.getString(R.string.message_city)
		);
	}
	public static Building makeBank(int level) {
		return new Building(
				ID_BANK,
				level,
				0.25,
				MainActivity.context.getString(R.string.bank),
				"+0.05$",
				MainActivity.context.getString(R.string.message_bank)
		);
	}

	public Building(
			int typeId,
			int level,
			double maintenance,
			String name,
			String commentPrefix,
			String comment
	) {
		this.typeId = typeId;
		this.level = level;
		this.maintenance = maintenance;
		this.name = name;
		this.commentPrefix = commentPrefix;
		this.comment = comment;
	}
	
	public String getName() {
		return name;
	}

	public String getComment() {
		return commentPrefix + " " + comment;
	}

	public String getRawComment() {
		return this.comment;
	}

	public String getCommentPrefix() {
		return this.commentPrefix;
	}

	public static Building makeBuilding(int typeId, int level) {
		switch (typeId) {
			case ID_FARM: return makeFarm(level);
			case ID_FACTORY: return makeFactory(level);
			case ID_RESEARCH_CENTER: return makeResearchCenter(level);
			case ID_CITY: return makeCity(level);
			case ID_BANK: return makeBank(level);
		}
		return null;
	}

	public static double getCost(int id) {
		switch (id) {
			case ID_FARM: return 50;
			case ID_FACTORY: return 120;
			case ID_RESEARCH_CENTER: return 300;
			case ID_CITY: return 150;
			case ID_BANK: return 200;
			default: return (int) 1e9;
		}
	}
	
	public static String getName(int id, int level) {
		switch (id) {
			case ID_FARM: return MainActivity.context.getString(R.string.farm) + (level == 1? "": " " + level);
			case ID_FACTORY: return MainActivity.context.getString(R.string.factory) + (level == 1? "": " " + level);
			case ID_RESEARCH_CENTER: return MainActivity.context.getString(R.string.research_center) + (level == 1? "": " " + level);
			case ID_CITY: return MainActivity.context.getString(R.string.city) + (level == 1? "": " " + level);
			case ID_BANK: return MainActivity.context.getString(R.string.bank) + (level == 1? "": " " + level);
			default: return "error";
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Building) {
			Building b = (Building) other;
			return b.typeId == this.typeId;
			//level is ignored
		}
		return false;
	}

	@Override
	public int compareTo(@NonNull Building other) {
		return Integer.valueOf(this.typeId).compareTo(other.typeId);
	}

	@Override
	@Deprecated
	public String toString() {
		return "Building " + super.toString();
	}
	
}
