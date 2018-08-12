package ru.barsk.moc;

import ru.barsk.util.Names;

public final class Generator {

	public Star[][] generateGalaxy(byte SIZE, byte DENSITY, byte numberOfPlayers, Names starNamesManager) {
		byte x;
		byte y;
		int numberOfStars;
		int i;
		byte[][] starTypes = new byte[SIZE][SIZE];
		Star[][] starMap = new Star[SIZE][SIZE];
		byte[] ownerIDs;
		String[] names;
		do {
			assignStarTypes(DENSITY, starTypes);
			numberOfStars = getNumberOfStars(starTypes);
		}
		while (numberOfStars < numberOfPlayers);
		names = new String[numberOfStars];
		starNamesManager.getNames(names);
		ownerIDs = new byte[numberOfStars];
		assignOwners(numberOfPlayers, ownerIDs);
		i = 0;
		for (x = 0; x < SIZE; x++) {
			for (y = 0; y < SIZE; y++) {
				if (starTypes[x][y] == 0) {
					starMap[x][y] = new Star((byte) (0), "", (byte) (0), x, y);
				}
				else {
					String name = names[i];
					byte ownerID = ownerIDs[i];
					starMap[x][y] = new Star(starTypes[x][y], name, ownerID, x, y);
					i++;
				}
			}
		}
		return starMap;
	}

	public void assignOwners(byte numberOfPlayers, byte[] ownerIDs) {
		int i;
		boolean[] uses = new boolean[ownerIDs.length];
		boolean usesAllStars;
        usesAllStars = !(ownerIDs.length > 0);
        for (i = 0; i < uses.length; i++) {
			uses[i] = false;
		}
		for (i = 0; i < ownerIDs.length; i++) {
			ownerIDs[i] = 0;
		}
		byte j = 1;
		while (j <= numberOfPlayers && !usesAllStars) {
			int star = (int) (Math.random()*ownerIDs.length);
			if (!uses[star]) {
				usesAllStars = true;
				uses[star] = true;
				ownerIDs[star] = j;
				j++;
				for (i = 0; i < uses.length; i++) {
					usesAllStars = usesAllStars && uses[i];
				}
			}
		}
	}

	public void assignStarTypes(byte DENSITY, byte[][] starTypes) {
		for (int x = 0; x < starTypes.length; x++) {
			for (int y = 0; y < starTypes[0].length; y++) {
				starTypes[x][y] = 0;
			}
		}
		for (int x = 0; x < starTypes.length; x++) {
			for (int y = 0; y < starTypes[0].length; y++) {
				if ((x > 0) && (x < (starTypes.length - 1)) && (y > 0) && (y < (starTypes[0].length- 1))) {
					if((starTypes[x-1][y-1] == 0) && (starTypes[x-1][y] == 0) && (starTypes[x-1][y+1] == 0) && (starTypes[x][y-1] == 0)){
						if((byte)(Math.random()*DENSITY)==0){
							starTypes[x][y] = (byte) (Math.random()*8);
						}
					}
				}
			}
		}
	}

	public int getNumberOfStars(byte[][] starTypes) {
		int numberOfStars = 0;
        for (int x = 0; x < starTypes.length; x++) {
			for (int y = 0; y < starTypes[0].length; y++) {
				if (starTypes[x][y] != 0) {
					numberOfStars++;
				}
			}
		}
		return numberOfStars;
	}

}
