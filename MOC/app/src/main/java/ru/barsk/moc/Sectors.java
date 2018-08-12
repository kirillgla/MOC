package ru.barsk.moc;

public /*final*/ abstract class Sectors {
	
	public static Sector[][] galaxy;
	
	public static void setGalaxy(Star[][] inputGalaxy) {
		int length = inputGalaxy.length / Sector.SIZE;
		galaxy = new Sector[length][length];
		for (byte i = 0; i < length; i++) {
			for (byte j = 0; j < length; j++) {
				galaxy[i][j] = new Sector(i, j, inputGalaxy);
			}
		}
	}

}
