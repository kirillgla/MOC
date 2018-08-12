package ru.barsk.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ru.barsk.moc.Ship;
import android.content.ContentValues;
import java.util.ArrayList;
import android.database.Cursor;
import android.widget.Toast;
import ru.barsk.moc.MainActivity;

public class ShipsSQLHelper extends SQLiteOpenHelper{

	private static final String LOG_TAG = "ShipsSQLHelper";
	public static final String DATABASE_NAME = StarsSQLHelper.DATABASE_NAME;
	public static final String TABLE_NAME_ORBIT = "orbit";
	public static final int DATABASE_VERSION = AIsSQLHelper.DATABASE_VERSION;

	public static final String COLUMN_OWNER_ID = "owner_id";
	public static final String COLUMN_CAN_COLONIZE = "can_colonize";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_MAX_HP = "max_hp";
	public static final String COLUMN_HP = "hp";
	public static final String COLUMN_POSITION_X = "position_x";
	public static final String COLUMN_POSITION_Y = "position_y";
	public static final String COLUMN_DESTINATION_X = "destination_x";
	public static final String COLUMN_DESTINATION_Y = "destination_y";
	public static final String COLUMN_BASE = "base";
	public static final String COLUMN_ATTACK = "attack";
	public static final String COLUMN_SPEED = "speed";
	public static final String COLUMN_MAINTENANCE = "maintenance";

	public static final int NUM_COLUMN_OWNER_ID = 0;
	public static final int NUM_COLUMN_CAN_COLONIZE = 1;
	public static final int NUM_COLUMN_NAME = 2;
	public static final int NUM_COLUMN_MAX_HP = 3;
	public static final int NUM_COLUMN_HP = 4;
	public static final int NUM_COLUMN_POSITION_X = 5;
	public static final int NUM_COLUMN_POSITION_Y = 6;
	public static final int NUM_COLUMN_DESTINATION_X = 7;
	public static final int NUM_COLUMN_DESTINATION_Y = 8;
	public static final int NUM_COLUMN_BASE = 9;
	public static final int NUM_COLUMN_ATTACK = 10;
	public static final int NUM_COLUMN_SPEED = 11;
	public static final int NUM_COLUMN_MAINTENANCE = 12;

	private final int index;
	private final int x;
	private final int y;

	public ShipsSQLHelper(Context context, int index, int x, int y) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.index = index;
		this.x = x;
		this.y = y;
		SQLiteDatabase db = getWritableDatabase();
		onCreate(db);
		db.close();
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String orbitsTableCreationQuery =
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ORBIT + "_" + index + "_" + x + "_" + y + "(" +
				COLUMN_OWNER_ID + " INTEGER, " +
				COLUMN_CAN_COLONIZE + " INTEGER, " +
				COLUMN_NAME + " TEXT, " +
				COLUMN_MAX_HP + " INTEGER, " +
				COLUMN_HP + " INTEGER, " +
				COLUMN_POSITION_X + " INTEGER, " +
				COLUMN_POSITION_Y + " INTEGER, " +
				COLUMN_DESTINATION_X + " INTEGER, " +
				COLUMN_DESTINATION_Y + " INTEGER, " +
				COLUMN_BASE + " INTEGER, " +
				COLUMN_ATTACK + " INTEGER, " +
				COLUMN_SPEED + " INTEGER, " +
				COLUMN_MAINTENANCE + " REAL" +
				")";
		database.execSQL(orbitsTableCreationQuery);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ORBIT);
		onCreate(db);
	}
	
	public void insert(Ship ship) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_OWNER_ID, ship.ownerId);
		cv.put(COLUMN_CAN_COLONIZE, (ship.canColonize? 1: 0));
		cv.put(COLUMN_NAME, ship.name);
		cv.put(COLUMN_MAX_HP, ship.getRawMaxHp());
		cv.put(COLUMN_HP, ship.getRawHp());
		cv.put(COLUMN_POSITION_X, ship.position.x);
		cv.put(COLUMN_POSITION_Y, ship.position.y);
		cv.put(COLUMN_DESTINATION_X, ship.destination.x);
		cv.put(COLUMN_DESTINATION_Y, ship.destination.y);
		cv.put(COLUMN_BASE, ship.base);
		cv.put(COLUMN_ATTACK, ship.getRawAttack());
		cv.put(COLUMN_SPEED, ship.speed);
		cv.put(COLUMN_MAINTENANCE, ship.maintenance);
		db.insert(TABLE_NAME_ORBIT + "_" + index + "_" + x + "_" + y, null, cv);
		db.close();
	}
	
	public void insertAll(ArrayList<Ship> src) {
		for (Ship ship: src) {
			insert(ship);
		}
	}

	public void insertAll(Ship[] src) {
		for (Ship ship: src) {
			insert(ship);
		}
	}

	public void deleteAll() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ORBIT + "_" + index + "_" + x + "_" + y);
		db.close();
	}
	
	public ArrayList<Ship> selectAll() {
		SQLiteDatabase db = getWritableDatabase();
		ArrayList<Ship> result = new ArrayList<>();
		Cursor cursor = db.query(
				TABLE_NAME_ORBIT + "_" + index + "_" + x + "_" + y,
				null,
				null,
				null,
				null,
				null,
				null
		);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			do {
				byte ownerId = (byte) cursor.getInt(NUM_COLUMN_OWNER_ID);
				boolean canColonize = cursor.getInt(NUM_COLUMN_CAN_COLONIZE) == 1;
				String name = cursor.getString(NUM_COLUMN_NAME);
				int maxHp = cursor.getInt(NUM_COLUMN_MAX_HP);
				int hp = cursor.getInt(NUM_COLUMN_HP);
				byte posX = (byte) cursor.getInt(NUM_COLUMN_POSITION_X);
				byte posY = (byte) cursor.getInt(NUM_COLUMN_POSITION_Y);
				byte desX = (byte) cursor.getInt(NUM_COLUMN_DESTINATION_X);
				byte desY = (byte) cursor.getInt(NUM_COLUMN_DESTINATION_Y);
				byte base = (byte) cursor.getInt(NUM_COLUMN_BASE);
				byte attack = (byte) cursor.getInt(NUM_COLUMN_ATTACK);
				byte speed = (byte) cursor.getInt(NUM_COLUMN_SPEED);
				double maintenanance = cursor.getDouble(NUM_COLUMN_MAINTENANCE);
				Ship current = new Ship(
						ownerId,
						canColonize,
						name,
						maxHp,
						hp,
						posX,
						posY,
						desX,
						desY,
						base,
						attack,
						speed,
						maintenanance
				);
				result.add(current);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return result;
	}
	
}
