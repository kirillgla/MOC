package ru.barsk.util;

import java.util.NoSuchElementException;

import ru.barsk.moc.MainActivity;
import ru.barsk.moc.R;
import ru.barsk.moc.Ship;
import ru.barsk.moc.ShipTemplate;
import ru.barsk.moc.Star;

public /*final*/ abstract class Lib {

    public static /*final*/ abstract class Galaxy {
		public static final Star[][] defaultGalaxy_2 =
		{
				{
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 0),
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 1),
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 4),
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 5),
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 0, (byte) 7),
				},
				{
						new Star((byte) 0, "", (byte) 0, (byte) 1, (byte) 0),
						new Star((byte) 6, "New Sol", (byte) 1, (byte) 1, (byte) 1),
						new Star((byte) 0, "", (byte) 0, (byte) 1, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 1, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 1, (byte) 4),
						new Star((byte) 0, "", (byte) 0, (byte) 1, (byte) 5),
						new Star((byte) 0, "", (byte) 0, (byte) 1, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 1, (byte) 7),
				},
				{
						new Star((byte) 0, "", (byte) 0, (byte) 2, (byte) 0),
						new Star((byte) 0, "", (byte) 0, (byte) 2, (byte) 1),
						new Star((byte) 0, "", (byte) 0, (byte) 2, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 2, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 2, (byte) 4),
						new Star((byte) 1, "Horison", (byte) 0, (byte) 2, (byte) 5),
						new Star((byte) 0, "", (byte) 0, (byte) 2, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 2, (byte) 7),
				},
				{
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 0),
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 1),
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 4),
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 5),
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 3, (byte) 7),
				},
				{
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 0),
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 1),
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 4),
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 5),
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 4, (byte) 7),
				},
				{
						new Star((byte) 0, "", (byte) 0, (byte) 5, (byte) 0),
						new Star((byte) 0, "", (byte) 0, (byte) 5, (byte) 1),
						new Star((byte) 7, "Golem", (byte) 0, (byte) 5, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 5, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 5, (byte) 4),
						new Star((byte) 0, "", (byte) 0, (byte) 5, (byte) 5),
						new Star((byte) 0, "", (byte) 0, (byte) 5, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 5, (byte) 7),
				},
				{
						new Star((byte) 0, "", (byte) 0, (byte) 6, (byte) 0),
						new Star((byte) 0, "", (byte) 0, (byte) 6, (byte) 1),
						new Star((byte) 0, "", (byte) 0, (byte) 6, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 6, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 6, (byte) 4),
						new Star((byte) 0, "", (byte) 0, (byte) 6, (byte) 5),
						new Star((byte) 6, "Aschela", (byte) 0, (byte) 6, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 6, (byte) 7),
				},
				{
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 0),
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 1),
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 2),
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 3),
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 4),
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 5),
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 6),
						new Star((byte) 0, "", (byte) 0, (byte) 7, (byte) 7),
				}
		};
		/*TODO: convert to new constructors this galaxy, too
		public static final Star[][] defaultGalaxy_3 =
		{
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0)
		},
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 6, "Core", (byte) 1),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 3, "Zora", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0)
		},
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 6, "Creature", (byte) 2),
		new Star((byte) 0, "", (byte) 0)
		},
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 4, "Eva", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0)
		},
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 1, "Ahar", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 3, "Poor", (byte) 0),
		new Star((byte) 0, "", (byte) 0)
		},
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0)
		},
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 6, "Qurat", (byte) 3),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 4, "Water", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0)
		},
		{
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0),
		new Star((byte) 0, "", (byte) 0)
		}
		};
		*/
	}
	
	public static /*final*/ abstract class ShipTemplates {
		public static ShipTemplate getShipTemplate(int index) {
			switch (index) {
				case 0: return new ShipTemplate(50, MainActivity.context.getString(R.string.ship0), Ship.BASE_TINY, (byte) 2, (byte) 2, 3);
				case 1: return new ShipTemplate(100, MainActivity.context.getString(R.string.ship1), Ship.BASE_SMALL, (byte) 5, (byte) 2, 12);
				case 2: return new ShipTemplate(600, MainActivity.context.getString(R.string.ship2), Ship.BASE_MEDIUM, (byte) 16, (byte) 1, 34);
				case 3: return new ShipTemplate(1000, MainActivity.context.getString(R.string.ship3), Ship.BASE_LARGE, (byte) 30, (byte) 1, 80);
				case 4: return new ShipTemplate(1600, MainActivity.context.getString(R.string.ship4), Ship.BASE_HUGE, (byte) 120, (byte) 1, 350);
				case 5: return new ShipTemplate(350, MainActivity.context.getString(R.string.ship5), Ship.BASE_FREIGHTER, (byte) 1, (byte) 1, 20);
				default: return new ShipTemplate(1e18, "ERROR", Ship.BASE_TINY, (byte) 1, (byte) 1, (byte) 1);
			}
		}
	}
	
	public static class Colors {

		public boolean[] used;

		public Colors() {
			used = new boolean[/*8*/] {true/*color code 0 is claimed for neutral*/, false, false, false, false, false, false, false};
			for (int i = 1; i < used.length; i++) {
				used[i] = false;
			}
		}
		
		public boolean allUsed() {
			for (boolean b: used) {
				if (!b) {
					return false;
				}
			}
			return true;
		}

		public synchronized byte getFreeColorId() {
			if (allUsed()) {
				throw new NoSuchElementException("All the colors are in use!");
			}
			else {
				while (true) {
					byte index = (byte) (Math.random() * used.length);
					if (!used[index]) {
						used[index] = true;
						return index;
					}
				}
			}
		}

	}

}
