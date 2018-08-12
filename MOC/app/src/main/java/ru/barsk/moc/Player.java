package ru.barsk.moc;

import ru.barsk.util.LongDouble;

public /*final*/ abstract class Player {

	public static final byte ID = 1;
	private static final int initialTreasury = 100;
	public static double treasury = initialTreasury;
	public static double income = 0;
	public static LongDouble IT = new LongDouble();
	public static byte colorId;
    //public String name;

	public static void clear() {
		treasury = initialTreasury;
		income = 0;
		IT.clear();
	}

}

