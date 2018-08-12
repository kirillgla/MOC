package ru.barsk.util;

import java.util.TreeSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ru.barsk.moc.Building;
import ru.barsk.moc.Coordinates;

public class BuildingsTreeSet extends TreeSet<Building> {

	/**
	 * Will override existing object
	 * instead of returning false.
	 * @param object building to add
	 * @return true
	 */
	@Override
	public boolean add(Building object) {
		if (contains(object)) {
			super.remove(object);
		}
		super.add(object);
		return true;
	}


	public boolean contains(int typeId) {
		for (Building building: this) {
			if (building.typeId == typeId) {
				return true;
			}
		}
		return false;
	}

	public Building getById(int id) {
		for (Building b: this) {
			if (b.typeId == id) {
				return b;
			}
		}
		return null;
	}

	public Building get(int index) {
		Iterator<Building> iterator = iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Building b = iterator.next();
			if (i == index) {
				return b;
			}
			i++;
		}
		throw new NoSuchElementException();
	}

	public void removeRandom(Coordinates coordinates) {
		int index = (int) (Math.random() * size());
		Building b = get(index);
		Tools.notifyBuildingAbandoned(b, coordinates);
		remove(b);
	}

}
