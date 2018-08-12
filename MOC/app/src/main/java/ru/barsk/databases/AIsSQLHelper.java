package ru.barsk.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import ru.barsk.moc.AbstractAI;
import ru.barsk.moc.AI;
import ru.barsk.moc.Player;
import ru.barsk.util.LongDouble;

public class AIsSQLHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = StarsSQLHelper.DATABASE_NAME;
	public static final String TABLE_NAME_AIS = "ais";
	public static final int DATABASE_VERSION = 3;
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TREASURY = "treasury";
	public static final String COLUMN_COLOR_ID = "color_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_IT = "it";
	public static final int NUM_COLUMN_ID = 0;
	public static final int NUM_COLUMN_TREASURY = 1;
	public static final int NUM_COLUMN_COLOR_ID = 2;
	public static final int NUM_COLUMN_NAME = 3;
	public static final int NUM_COLUMN_IT = 4;

	private final int index;

	public AIsSQLHelper(Context context, int index) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.index = index;
		SQLiteDatabase db = getWritableDatabase();
		onCreate(db);
		db.close();
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String query =
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_AIS + "_" + index + " (" +
				COLUMN_ID + " INTEGER PRIMARY KEY UNIQUE, " +
				COLUMN_TREASURY + " REAL, " +
				COLUMN_COLOR_ID + " INTEGER UNIQUE, " +
				COLUMN_NAME + " TEXT, " +
				COLUMN_IT + " TEXT" +
				")";
		database.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_AIS + "_" + index);
		onCreate(database);
	}

	public void insertVoid() {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ID, 0);
		cv.put(COLUMN_TREASURY, 0);
		cv.put(COLUMN_COLOR_ID, 0);
		cv.put(COLUMN_NAME, "");
		cv.put(COLUMN_IT, "0");
		db.insert(TABLE_NAME_AIS + "_" + index, null, cv);
		db.close();
	}

	public void insertPlayer() {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ID, Player.ID);
		cv.put(COLUMN_TREASURY, Player.treasury);
		cv.put(COLUMN_COLOR_ID, Player.colorId);
		cv.put(COLUMN_NAME, "Player");
		cv.put(COLUMN_IT, Player.IT.toString());
		db.insert(TABLE_NAME_AIS + "_" + index, null, cv);
		db.close();
	}

	public void insert(AbstractAI ai) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ID, ai.ID);
		cv.put(COLUMN_TREASURY, ai.treasury);
		cv.put(COLUMN_COLOR_ID, ai.colorId);
		cv.put(COLUMN_NAME, ai.name);
		cv.put(COLUMN_IT, ai.IT.toString());
		db.insert(TABLE_NAME_AIS + "_" + index, null, cv);
		db.close();
	}

	public void insertAll(AbstractAI[] ais) {
		for (AbstractAI ai: ais) {
			insert(ai);
		}
	}

	public ArrayList<AbstractAI> selectAll() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_NAME_AIS + "_" + index,
				null,
				null,
				null,
				null,
				null,
				null
		);
		ArrayList<AbstractAI> result = new ArrayList<>();
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			do {
				byte id = (byte) cursor.getInt(NUM_COLUMN_ID);
				double treasury = cursor.getDouble(NUM_COLUMN_TREASURY);
				byte colorId = (byte) cursor.getInt(NUM_COLUMN_COLOR_ID);
				String name = cursor.getString(NUM_COLUMN_NAME);
				LongDouble it = LongDouble.parseLongDouble(cursor.getString(NUM_COLUMN_IT));;
				AbstractAI current = new AI(id, colorId, treasury, name, it);
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
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_AIS + "_" + index);
		db.close();
	}

}
