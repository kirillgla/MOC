package ru.barsk.util;

import android.support.annotation.Nullable;

import ru.barsk.moc.ArtificialConstruction;
import ru.barsk.moc.Building;
import ru.barsk.moc.BuildingTemplate;
import ru.barsk.moc.Coordinates;
import ru.barsk.moc.ShipTemplate;
import ru.barsk.moc.Player;

public class ConstructionQueue implements Iterable<ArtificialConstruction> {

	public ArtificialConstruction[] queue;

	public int size() {
		int result = 0;
		for (ArtificialConstruction construction: queue) {
			if (construction != null) {
				result++;
			}
		}
		return result;
	}

	public ConstructionQueue(int limit) {
		queue = new ArtificialConstruction[limit];
	}

	public boolean offer(ArtificialConstruction construction) {
		int freeIndex = getFreeIndex();
		if (freeIndex == -1) {
			return false;
		}
		queue[freeIndex] = construction;
		return true;
	}

	public void remove(int i) {
		queue[i] = null;
		for (; i < queue.length - 1; i++) {
			queue[i] = queue[i + 1];
		}
		queue[i] = null;
	}
	
	public void removeBuildingId(int typeId) {
		for (int i = 0; i < queue.length; i++) {
			if (queue[i] == null) {
				return;
			}
			Object constructed = queue[i].construct(Player.ID, new Coordinates((byte) 0, (byte) 0));
			if (!(constructed instanceof Building)) {
				continue;
			}
			Building b = (Building) constructed;
			if (b.typeId == typeId) {
				remove(i);
			}
		}
	}

	public Object construct(byte ownerId, Coordinates coordinates) {
		Object result = queue[0].construct(ownerId, coordinates);
		queue[0] = null;
		moveShipsInQueue();
		return result;
	}
	
	public Object construct(byte ownerId, Coordinates coordinates, int index) {
		Object result = queue[index].construct(ownerId, coordinates);
		queue[index] = null;
		moveShipsInQueue();
		return result;
	}

	public boolean hasFreeSlots() {
		for (ArtificialConstruction construction: queue) {
			if (construction == null) {
				return true;
			}
		}
		return false;
	}

	public boolean hasFirst() {
		return queue[0] != null;
	}

	private int getFreeIndex() {
		for (int i = 0; i < queue.length; i++) {
			if (queue[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	private void moveShipsInQueue() {
		int j = 0;
		for (int i = 0; i < queue.length; i++) {
			if (queue[i] != null && i != j) {
				queue[j] = queue[i];
				queue[i] = null;
				j++;
			}
		}
	}

	public double getFirstProductionLeft() {
		return queue[0].productionCost;
	}

	@Nullable
	public Object investProduction(double production, byte ownerId, Coordinates coordinates) {
		queue[0].productionCost -= production;
		if (queue[0].productionCost <= 0) {
			return construct(ownerId, coordinates);
		}
		return null;
	}
	
	@Nullable
	public Object investProduction(double production, byte ownerId, Coordinates coordinates, int index) {
		queue[index].productionCost -= production;
		if (queue[index].productionCost <= 0) {
			return construct(ownerId, coordinates, index);
		}
		return null;
	}

	public ArtificialConstruction getFirst() {
		return queue[0];
	}

	public ArtificialConstruction get(int index) {
		return queue[index];
	}

	public boolean contains(Building building) {
		for (ArtificialConstruction construction: queue) {
			if (construction != null) {
				Object expected = construction.construct(Player.ID, new Coordinates((byte) 0, (byte) 0));
				if (expected.equals(building)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean contains(int id) {
		for (ArtificialConstruction construction: queue) {
			if (construction instanceof BuildingTemplate) {
				BuildingTemplate template = (BuildingTemplate) construction;
				if (template.typeId == id) {
					return true;
				}
			}
		}
		return false;
	}

	public Building getBuildingById(int id) {
		for (ArtificialConstruction construction: queue) {
			Object constructed = construction.construct(Player.ID, new Coordinates((byte) 0, (byte) 0));
			if (constructed instanceof Building) {
				if (((Building) constructed).typeId == id) {
					return (Building) constructed;
				}
			}
		}
		return null;
	}

	public int getFirstShipIndex() {
		for (int i = 0; i < queue.length; i++) {
			if (queue[i] instanceof ShipTemplate) {
				return i;
			}
		}
		return -1;
	}

	public double getTotalProductionCost() {
		double result = 0;
		for (ArtificialConstruction construction: this) {
			if (construction != null) {
				result += construction.productionCost;
			}
		}
		return result;
	}

	public double getShipsDanger() {
		double result = 0;
		for (ArtificialConstruction construction: queue) {
			if (construction instanceof ShipTemplate) {
				result += ((ShipTemplate) construction).danger();
			}
		}
		return result;
	}

	public java.util.Iterator<ArtificialConstruction> iterator() {
		return new ConstructionQueue.Iterator();
	}

	private class Iterator implements java.util.Iterator<ArtificialConstruction> {
		public Iterator() {
			index = -1;
		}
		private int index;
		@Override
		public ArtificialConstruction next() {
			index++;
			return get(index);
		}
		@Override
		public boolean hasNext() {
			return index + 1 < queue.length;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
