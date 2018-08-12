package ru.barsk.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ru.barsk.moc.ArtificialConstruction;
import ru.barsk.moc.BuildingTemplate;
import ru.barsk.moc.ShipTemplate;
import ru.barsk.util.ConstructionQueue;
import android.database.Cursor;
import ru.barsk.moc.Star;

public class ConstructionsSQLHelper extends SQLiteOpenHelper{

	public static final String DATABASE_NAME = StarsSQLHelper.DATABASE_NAME;
	public static final String TABLE_NAME_CONSTRUCTIONS = "constructions";
	public static final int DATABASE_VERSION = AIsSQLHelper.DATABASE_VERSION;

	public static final String COLUMN_INITIAL_PRODUCTION_COST = "initial_production_cost";
	public static final String COLUMN_PRODUCTION_COST = "production_cost";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TYPE = "type"; //"ship_template" or "building_template";
	public static final String TYPE_SHIP_TEMPLATE = "ship_template";
	public static final String TYPE_BUILDING_TEMPLATE = "building_template";
	//for shipTemplate:
	public static final String COLUMN_MAX_HP = "max_hp";
	public static final String COLUMN_BASE = "base";
	public static final String COLUMN_ATTACK = "attack";
	public static final String COLUMN_SPEED = "speed";
	//for buildingTemplate
	public static final String COLUMN_TYPE_ID = "type_id";
	public static final String COLUMN_LEVEL = "level";

	public static final int NUM_COLUMN_INITIAL_PRODUCTION_COST = 0;
	public static final int NUM_COLUMN_PRODUCTION_COST = 1;
	public static final int NUM_COLUMN_NAME = 2;
	public static final int NUM_COLUMN_TYPE = 3;
	public static final int NUM_COLUMN_MAX_HP = 4;
	public static final int NUM_COLUMN_BASE = 5;
	public static final int NUM_COLUMN_ATATCK = 6;
	public static final int NUM_COLUMN_SPEED = 7;
	public static final int NUM_COLUMN_TYPE_ID = 8;
	public static final int NUM_COLUMN_LEVEL = 9;

	private final int index;
	private final int x;
	private final int y;

	public ConstructionsSQLHelper(Context context, int index, int x, int y) {
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
		String query = "CREATE TABLE IF NOT EXISTS " + getTableName() + " (" +
				COLUMN_INITIAL_PRODUCTION_COST + " REAL, " +
				COLUMN_PRODUCTION_COST + " REAL, " +
				COLUMN_NAME + " TTEXT, " +
				COLUMN_TYPE + " TEXT, " +
				COLUMN_MAX_HP + " INTEGER, " +
				COLUMN_BASE + " INTEGER, " +
				COLUMN_ATTACK + " INTEGER, " +
				COLUMN_SPEED + " INTEGER, " +
				COLUMN_TYPE_ID + " INTEGER, " +
				COLUMN_LEVEL + " INTEGER" +
				")";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + getTableName());
		onCreate(db);
	}

	public String getTableName() {
		return TABLE_NAME_CONSTRUCTIONS + "_" + index + "_" + x + "_" + y;
	}

	public static String getTableName(int index, int x, int y) {
		return TABLE_NAME_CONSTRUCTIONS + "_" + index + "_" + x + "_" + y;
	}

	@Deprecated
	public String getOldTableName() {
		return TABLE_NAME_CONSTRUCTIONS + index + "_" + x + "_" + y;
	}
	
	public void insert(ArtificialConstruction construction) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_INITIAL_PRODUCTION_COST, construction.initialProductionCost);
		cv.put(COLUMN_PRODUCTION_COST, construction.productionCost);
		cv.put(COLUMN_NAME, construction.name);
		if (construction instanceof ShipTemplate) {
			cv.put(COLUMN_TYPE, TYPE_SHIP_TEMPLATE);
			ShipTemplate shipTemplate = (ShipTemplate) construction;
			cv.put(COLUMN_MAX_HP, shipTemplate.maxHp);
			cv.put(COLUMN_BASE, shipTemplate.base);
			cv.put(COLUMN_ATTACK, shipTemplate.attack);
			cv.put(COLUMN_SPEED, shipTemplate.speed);
		}
		else {
			if (construction instanceof BuildingTemplate) {
				cv.put(COLUMN_TYPE, TYPE_BUILDING_TEMPLATE);
				BuildingTemplate buildingTemplate = (BuildingTemplate) construction;
				cv.put(COLUMN_TYPE_ID, buildingTemplate.typeId);
				cv.put(COLUMN_LEVEL, buildingTemplate.level);
			}
			else {
				throw new ClassCastException("only ShipTemplates and BuildingTemplates are supported!");
			}
		}
		db.insert(getTableName(this.index, x, y), null, cv);
		db.close();
	}
	
	public void insertAll(ConstructionQueue queue) {
		int size = queue.size();
		for (int i = 0; i < size; i++) {
			insert(queue.get(i));
		}
	}
	
	public ConstructionQueue selectAll() {
		SQLiteDatabase db = getWritableDatabase();
		ConstructionQueue result = new ConstructionQueue(Star.SHIPYARD_LIMIT);
		Cursor cursor = db.query(
				getTableName(index, x, y),
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
				double initialProductionCost = cursor.getDouble(NUM_COLUMN_INITIAL_PRODUCTION_COST);
				double productionCost = cursor.getDouble(NUM_COLUMN_PRODUCTION_COST);
				String name = cursor.getString(NUM_COLUMN_NAME);
				String type = cursor.getString(NUM_COLUMN_TYPE);
				if (type.equals(TYPE_SHIP_TEMPLATE)) {
					byte maxHp = (byte) cursor.getInt(NUM_COLUMN_MAX_HP);
					byte base = (byte) cursor.getInt(NUM_COLUMN_BASE);
					byte attack = (byte) cursor.getInt(NUM_COLUMN_ATATCK);
					byte speed = (byte) cursor.getInt(NUM_COLUMN_SPEED);
					ShipTemplate current = new ShipTemplate(initialProductionCost, productionCost, name, base, attack, speed, maxHp);
					result.offer(current);
				}
				else {
					if (type.equals(TYPE_BUILDING_TEMPLATE)) {
						int typeId = cursor.getInt(NUM_COLUMN_TYPE_ID);
						int level = cursor.getInt(NUM_COLUMN_LEVEL);
						BuildingTemplate current = new BuildingTemplate(initialProductionCost, productionCost, typeId, level, name);
						result.offer(current);
					}
					else {
						throw new ClassCastException("only ShipTemplates and BuildingTemplates are supported!");
					}
				}
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return result;
	}

	public void deleteAll() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + getTableName());
		db.close();
	}

}
