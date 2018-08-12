package ru.barsk.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.barsk.moc.Building;
import ru.barsk.util.BuildingsTreeSet;

public class BuildingsSQLHelper extends SQLiteOpenHelper{

	public static final String DATABASE_NAME = StarsSQLHelper.DATABASE_NAME;
	public static final int DATABASE_VERSION = AIsSQLHelper.DATABASE_VERSION;
	public static final String TABLE_NAME_BUILDINGS = "buildings";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TYPE_ID = "type_id";
	public static final String COLUMN_LEVEL = "level";
	public static final String COLUMN_MAINTENANCE = "maintenance";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_COMMENT_PREFIX = "comment_prefix";
	public static final String COLUMN_COMMENT = "comment";

	public static final int NUM_COLUMN_ID = 0;
	public static final int NUM_COLUMN_TYPE_ID = 1;
	public static final int NUM_COLUMN_LEVEL = 2;
	public static final int NUM_COLUMN_MAINTENANCE = 3;
	public static final int NUM_COLUMN_NAME = 4;
	public static final int NUM_COLUMN_COMMENT_PREFIX = 5;
	public static final int NUM_COLUMN_COMMENT = 6;

	private final int index;
	private final int x;
	private final int y;

	public BuildingsSQLHelper(Context context, int index, int x, int y) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.index = index;
		this.x = x;
		this.y = y;
		SQLiteDatabase db = getWritableDatabase();
		onCreate(db);
		db.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String query =
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BUILDINGS + "_" + index + "_" + x + "_" + y + " (" +
				COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_TYPE_ID + " INTEGER UNIQUE, " +
				COLUMN_LEVEL + " INTEGER, " +
				COLUMN_MAINTENANCE + " REAL, " +
				COLUMN_NAME + " TEXT, " +
				COLUMN_COMMENT_PREFIX + " TEXT, " +
				COLUMN_COMMENT + " TEXT" +
				")";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BUILDINGS + "_" + index + "_" + x + "_" + y);
		onCreate(db);
	}

	public void insert(Building building) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_TYPE_ID, building.typeId);
		cv.put(COLUMN_LEVEL, building.level);
		cv.put(COLUMN_MAINTENANCE, building.maintenance);
		cv.put(COLUMN_NAME, building.getName());
		cv.put(COLUMN_COMMENT_PREFIX, building.getCommentPrefix());
		cv.put(COLUMN_COMMENT, building.getRawComment());
		db.insert(TABLE_NAME_BUILDINGS + "_" + index + "_" + x + "_" + y, null, cv);
		db.close();
	}

	public void insertAll(BuildingsTreeSet set) {
		for (Building b: set) {
			insert(b);
		}
	}

	public BuildingsTreeSet selectAll() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_NAME_BUILDINGS + "_" + index + "_" + x + "_" + y,
				null,
				null,
				null,
				null,
				null,
				null
		);
		BuildingsTreeSet result = new BuildingsTreeSet();
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			do {
				byte typeId = (byte) cursor.getInt(NUM_COLUMN_TYPE_ID);
				byte level = (byte) cursor.getInt(NUM_COLUMN_LEVEL);
				double maintenance = cursor.getDouble(NUM_COLUMN_MAINTENANCE);
				String name = cursor.getString(NUM_COLUMN_NAME);
				String commentPrefix = cursor.getString(NUM_COLUMN_COMMENT_PREFIX);
				String comment = cursor.getString(NUM_COLUMN_COMMENT);
				Building current = new Building(
						typeId,
						level,
						maintenance,
						name,
						commentPrefix,
						comment
				);
				result.add(current);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return result;
	}

	public void deleteAll() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BUILDINGS + "_" + index + "_" + x + "_" + y);
		db.close();
	}

}
