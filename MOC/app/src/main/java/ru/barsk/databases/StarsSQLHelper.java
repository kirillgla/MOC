package ru.barsk.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ru.barsk.moc.Star;
import android.content.ContentValues;
import java.util.ArrayList;
import android.database.Cursor;
import ru.barsk.util.BuildingsTreeSet;
import ru.barsk.moc.Ship;
import ru.barsk.util.ConstructionQueue;

//При обновлении StarsSQLHelper дропает таблицу => данные теряются.
//Поэтому необходимо переписать метод onUpgrade()
//в случае внесения измений в таблицу.
public class StarsSQLHelper extends SQLiteOpenHelper{

	public static final String DATABASE_NAME = "MOC_database";
	public static final String TABLE_NAME_GALAXY = "galaxy";
	public static final int DATABASE_VERSION = AIsSQLHelper.DATABASE_VERSION;
	
	public static final String COLUMN_FARMERS = "farmers_percentage";
	public static final String COLUMN_WORKERS = "workers_percentage";
	public static final String COLUMN_SCIENTISTS = "scientists_percentage";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_FERTILITY = "fertility";
	public static final String COLUMN_RICHNESS = "richness";
	public static final String COLUMN_MEC = "minerals_extracted_counter";
	public static final String COLUMN_SCIENTIFIC_POTENTIAL = "scientific_potential";
	public static final String COLUMN_MAX_POPULATION = "max_population";
	public static final String COLUMN_POPULATION = "population";
	public static final String COLUMN_OWNER_ID = "owner_id";
	public static final String COLUMN_MORALE = "morale";
	public static final String COLUMN_MAX_STORED = "max_stored";
	public static final String COLUMN_FOOD_STORED = "food_stored";
	public static final String COLUMN_PRODUCTION_STORED = "production_stored";
	public static final String COLUMN_X = "x";
	public static final String COLUMN_Y = "y";
	public static final String COLUMN_SHOULD_FILL_STORAGE = "should_fill_storage";
	
	public static final int NUM_COLUMN_FARMERS = 0;
	public static final int NUM_COLUMN_WORKERS = 1;
	public static final int NUM_COLUMN_SCIENTISTS = 2;
	public static final int NUM_COLUMN_NAME = 3;
	public static final int NUM_COLUMN_TYPE = 4;
	public static final int NUM_COLUMN_FERTILITY = 5;
	public static final int NUM_COLUMN_RICHNESS = 6;
	public static final int NUM_COLUMN_MEC = 7;
	public static final int NUM_COLUMN_SCIENTIFIC_POTENTIAL = 8;
	public static final int NUM_COLUMN_MAX_POPULATION = 9;
	public static final int NUM_COLUMN_POPULATION = 10;
	public static final int NUM_COLUMN_OWNER_ID = 11;
	public static final int NUM_COLUMN_MORALE = 12;
	public static final int NUM_COLUMN_MAX_STORED = 13;
	public static final int NUM_COLUMN_FOOD_STORED = 14;
	public static final int NUM_COLUMN_PRODUCTION_STORED = 15;
	public static final int NUM_COLUMN_X = 16;
	public static final int NUM_COLUMN_Y = 17;
	public static final int NUM_COLUMN_SHOULD_FILL_STORAGE = 18;

	private final int index;
	private final Context context;

	public StarsSQLHelper(Context context, int index) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.index = index;
		this.context = context;
		SQLiteDatabase db = getWritableDatabase();
		onCreate(db);
		db.close();
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String galaxyTableCreationQuery =
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_GALAXY + "_" + index + " (" +
				COLUMN_FARMERS + " REAL, " +
				COLUMN_WORKERS + " REAL, " +
				COLUMN_SCIENTISTS + " REAL ," +
				COLUMN_NAME + " TEXT, " +
				COLUMN_TYPE + " INTEGER, " +
				COLUMN_FERTILITY + " REAL ," +
				COLUMN_RICHNESS + " REAL, " +
				COLUMN_MEC + " REAL, " +
				COLUMN_SCIENTIFIC_POTENTIAL + " REAL, " +
				COLUMN_MAX_POPULATION + " REAL, " +
				COLUMN_POPULATION + " REAL, " +
				COLUMN_OWNER_ID + " INTEGER, " +
				COLUMN_MORALE + " REAL, " +
				COLUMN_MAX_STORED + " REAL, " +
				COLUMN_FOOD_STORED + " REAL, " +
				COLUMN_PRODUCTION_STORED + " REAL, " +
				COLUMN_X + " INTEGER, " +
				COLUMN_Y + " INTEGER, " +
				COLUMN_SHOULD_FILL_STORAGE + " INTEGER" +
				")";
		database.execSQL(galaxyTableCreationQuery);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GALAXY + index);
		onCreate(database);
	}
	
	public void insert(Star star) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_FARMERS, star.colonyManager.farmersPercentage);
		cv.put(COLUMN_WORKERS, star.colonyManager.workersPercentage);
		cv.put(COLUMN_SCIENTISTS, star.colonyManager.scientistsPercentage);
		cv.put(COLUMN_NAME, star.name);
		cv.put(COLUMN_TYPE, star.type);
		cv.put(COLUMN_FERTILITY, star.getFertility());
		cv.put(COLUMN_RICHNESS, star.getRichness());
		cv.put(COLUMN_MEC, star.mineralsExtractedCounter);
		cv.put(COLUMN_SCIENTIFIC_POTENTIAL, star.getScientificPotential());
		cv.put(COLUMN_MAX_POPULATION, star.getMaxPopulation());
		cv.put(COLUMN_POPULATION, star.getPopulation());
		cv.put(COLUMN_OWNER_ID, star.getOwnerId());
		cv.put(COLUMN_MORALE, star.getMorale());
		cv.put(COLUMN_MAX_STORED, star.getMaxStored());
		cv.put(COLUMN_FOOD_STORED, star.getFoodProduced());
		cv.put(COLUMN_PRODUCTION_STORED, star.getProductionStored());
		cv.put(COLUMN_X, star.coordinates.x);
		cv.put(COLUMN_Y, star.coordinates.y);
		cv.put(COLUMN_SHOULD_FILL_STORAGE, (star.shouldFillStorage ? 1 : 0));
		db.insert(TABLE_NAME_GALAXY + "_" + index, null, cv);
		db.close();
		ShipsSQLHelper shipsHelper = new ShipsSQLHelper(context, index, star.coordinates.x, star.coordinates.y);
		shipsHelper.insertAll(star.orbit);
		BuildingsSQLHelper buildingsHelper = new BuildingsSQLHelper(context, index, star.coordinates.x, star.coordinates.y);
		buildingsHelper.insertAll(star.buildings);
		ConstructionsSQLHelper ConstructionsHelper = new ConstructionsSQLHelper(context, index, star.coordinates.x, star.coordinates.y);
		ConstructionsHelper.insertAll(star.constructionQueue);
	}
	
	public void insertAll(ArrayList<Star> src) {
		for (Star star: src) {
			insert(star);
		}
	}

	public void insertAll(Star[] src) {
		for (Star star: src) {
			insert(star);
		}
	}

	public void insertAll(Star[][] galaxy) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				insert(star);
			}
		}
	}
	
	public void deleteAll() {
		int galaxySize = getGalaxySize();
		SQLiteDatabase db = getWritableDatabase();
		for (int i = 0; i < galaxySize; i++) {
			for (int j = 0; j < galaxySize; j++) {
				try {
					new ShipsSQLHelper(context, index, i, j).deleteAll();
				}
				catch (Exception ignored) {}
				try {
					new BuildingsSQLHelper(context, index, i, j).deleteAll();
				}
				catch (Exception ignored) {}
				try {
					new ConstructionsSQLHelper(context, index, i, j).deleteAll();
				}
				catch (Exception ignored) {}
			}
		}
		try {
			new AIsSQLHelper(context, index).deleteAll();
		}
		catch (Exception ignored) {}
		try {
			db.delete(TABLE_NAME_GALAXY + "_" + index, null, null);
		}
		catch (Exception ignored) {}
		db.close();
	}
	
	public ArrayList<Star> selectAll() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor mCursor = db.query(TABLE_NAME_GALAXY + "_" + index, null, null, null, null, null, null);
		ArrayList<Star> result = new ArrayList<>();
		mCursor.moveToFirst();
		if (!mCursor.isAfterLast()) {
			do {
				double farmers = mCursor.getDouble(NUM_COLUMN_FARMERS);
				double workers = mCursor.getDouble(NUM_COLUMN_WORKERS);
				double scientists = mCursor.getDouble(NUM_COLUMN_SCIENTISTS);
				String name = mCursor.getString(NUM_COLUMN_NAME);
				byte type = (byte) mCursor.getInt(NUM_COLUMN_TYPE);
				double fertility = mCursor.getDouble(NUM_COLUMN_FERTILITY);
				double richness = mCursor.getDouble(NUM_COLUMN_RICHNESS);
				double mineralsExtractedCounter = mCursor.getDouble(NUM_COLUMN_MEC);
				double scientificPotential = mCursor.getDouble(NUM_COLUMN_SCIENTIFIC_POTENTIAL);
				double maxPopulation = mCursor.getDouble(NUM_COLUMN_MAX_POPULATION);
				double population = mCursor.getDouble(NUM_COLUMN_POPULATION);
				byte ownerId = (byte) mCursor.getInt(NUM_COLUMN_OWNER_ID);
				double morale = mCursor.getDouble(NUM_COLUMN_MORALE);
				int maxStored = mCursor.getInt(NUM_COLUMN_MAX_STORED);
				double foodStored = mCursor.getDouble(NUM_COLUMN_FOOD_STORED);
				double productionStored = mCursor.getDouble(NUM_COLUMN_PRODUCTION_STORED);
				byte x = (byte) mCursor.getInt(NUM_COLUMN_X);
				byte y = (byte) mCursor.getInt(NUM_COLUMN_Y);
				boolean shouldFillStorage = mCursor.getInt(NUM_COLUMN_SHOULD_FILL_STORAGE) == 1;
				ShipsSQLHelper shipsHelper = new ShipsSQLHelper(context, index, x, y);
				ArrayList<Ship> orbit = shipsHelper.selectAll();
				BuildingsSQLHelper buildingsHelper = new BuildingsSQLHelper(context, index, x, y);
				BuildingsTreeSet buildings = buildingsHelper.selectAll();
				ConstructionsSQLHelper ConstructionsHelper = new ConstructionsSQLHelper(context, index, x, y);
				ConstructionQueue constructionQueue = ConstructionsHelper.selectAll();
				Star current = new Star(
						farmers,
						workers,
						scientists,
						name,
						type,
						fertility,
						richness,
						mineralsExtractedCounter,
						scientificPotential,
						maxPopulation,
						population,
						ownerId,
						morale,
						maxStored,
						foodStored,
						productionStored,
						x,
						y,
						shouldFillStorage,
						orbit,
						buildings,
						constructionQueue
				);
				result.add(current);
			}
			while (mCursor.moveToNext());
		}
		mCursor.close();
		db.close();
		return result;
	}
	
	public int getGalaxySize() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_NAME_GALAXY + "_" + index,
				null,
				null,
				null,
				null,
				null,
				null
		);
		cursor.moveToFirst();
		int result = 0;
		if (!cursor.isAfterLast()) {
			do {
				result++;
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		result = (int) Math.round(Math.sqrt(result));
		return result;
	}
	
	public int getStarCount() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_NAME_GALAXY + "_" + index,
				null,
				null,
				null,
				null,
				null,
				null
		);
		int result = 0;
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			do {
				byte type = (byte) cursor.getInt(NUM_COLUMN_TYPE);
				if (type != 0) {
					result++;
				}
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return result;
	}

	//Note: 0 and 1 ownerIDs are claimed for neutral.
	public int getEnemyCount() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_NAME_GALAXY + "_" + index,
				null,
				null,
				null,
				null,
				null,
				null
		);
		boolean[] exists = new boolean[/*8*/] {true, true, false, false, false, false, false, false};
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			do {
				byte ownerId = (byte) cursor.getInt(NUM_COLUMN_OWNER_ID);
				exists[ownerId] = true;
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		int result = 0;
		for (boolean b: exists) {
			if (b) {
				result++;
			}
		}
		result -= 2;
		return result;
	}

	public boolean isEmpty() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(
				TABLE_NAME_GALAXY + "_" + index,
				null,
				null,
				null,
				null,
				null,
				null
		);
		boolean result = !cursor.moveToFirst();
		cursor.close();
		db.close();
		return result;
	}

}
