package ru.barsk.moc;

import android.util.Log;

import java.util.ArrayList;

import ru.barsk.util.Tools;

public class Sector implements Iterable<Star[]> {

	private static final String LOG_TAG = "Sector";

	public static final int SIZE = 4;
	public final Coordinates coordinates;
	private Star[][] sector;

	public Sector(byte x, byte y, Star[][] galaxy) {
		this.coordinates = new Coordinates(x, y);
		sector = new Star[SIZE][SIZE];
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				sector[i][j] = galaxy[x * SIZE + i][y * SIZE + j];
			}
		}
	}
	
	public void callShipsTo(ArrayList<AttackedStar> attackedStars, byte ownerId, boolean fromSameSector) {
		ArrayList<Coordinates>  attackedStarsCoordinates;
		if (fromSameSector) {
			attackedStarsCoordinates = getCoordinatesOf(attackedStars);
		}
		else {
			attackedStarsCoordinates = getCoordinatesOf(getAttackedStars(ownerId));
		}
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ownerId) {
					if (!attackedStarsCoordinates.contains(star.coordinates)) {
						sendShipsFromSafeStar(attackedStars, star.orbit);
					}
					else {
						double attackPower = star.getAttackPower();
						double defencePower = star.getDefencePower();
						if (2 * attackPower < defencePower) {
							sendShipsFormEndangeredStar(attackedStars, star.orbit, 2 * attackPower - defencePower);
						}
					}
				}
			}
		}
	}
	
	public void callShipsToAttackedStars(byte ownerId) {
		i("called ships to attacked stars...");
		ArrayList<AttackedStar> attackedStars = getAttackedStars(ownerId);
		for (int x = (int) Tools.max(coordinates.x - 1, 0); x <= Tools.min(coordinates.x + 1, Sectors.galaxy.length - 1); x++) {
			for (int y = (int) Tools.max(coordinates.y - 1, 0); y <= Tools.min(coordinates.y + 1, Sectors.galaxy[x].length - 1); y++) {
				Sectors.galaxy[x][y].callShipsTo(attackedStars, ownerId, x == coordinates.x && y == coordinates.y);
				if (attackedStars.size() == 0) {
					return;
				}
			}
		}
	}

	public void sendShipsFromSafeStar(ArrayList<AttackedStar> targets, ArrayList<Ship> orbit) {
		int i = 0;
		int orbitSize = orbit.size();
		while (targets.size() != 0 && i < orbitSize) {
			Ship current = orbit.get(i);
			if (current.destination.equals(current.position)) {
				double danger = current.danger();
				AttackedStar attacked = targets.get(0);
				current.destination = attacked.coordinates;
				attacked.attackPower -= 0.5 * danger;
				if (attacked.attackPower <= 0) {
					targets.remove(0);
				}
			}
			i++;
		}
	}

	/**@param #attackPower is expected to bebe negative*/
	public void sendShipsFormEndangeredStar(ArrayList<AttackedStar> targets, ArrayList<Ship> orbit, double attackPower) {
		int i = 0;
		//int orbitSize = orbit.size();
		while (targets.size() != 0 /*&& i < orbitSize*/ && attackPower > 0) {
			Ship current = orbit.get(i);
			double currentDanger = current.danger();
			if (attackPower + currentDanger > 0) {
				return;
			}
			if (current.destination.equals(current.position)) {
				AttackedStar attacked = targets.get(0);
				current.destination = attacked.coordinates;
				attackPower += currentDanger;
				attacked.attackPower -= currentDanger;
				if (attacked.attackPower <= 0) {
					targets.remove(0);
				}
			}
			i++;
		}
	}

	public double getSumShipsDanger(byte ownerId) {
		double result = 0;
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ownerId) {
					for (Ship ship: star.orbit) {
						result += ship.danger();
					}
				}
			}
		}
		return result;
	}

	public Coordinates getMostEndangeredStar(byte ownerId) {
		double maxEnemyDanger = 0;
		Coordinates result = new Coordinates((byte) (coordinates.x * SIZE), (byte) (coordinates.y * SIZE));
		Coordinates any = new Coordinates((byte) 0, (byte) 0);
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					double danger = star.getEnemyDangerFor(ownerId);
					if (danger > maxEnemyDanger) {
						maxEnemyDanger = danger;
						result.x = star.coordinates.x;
						result.y = star.coordinates.y;
					}
					else {
						any.x = star.coordinates.x;
						any.y = star.coordinates.y;
					}
				}
			}
		}
		if (maxEnemyDanger == 0) {
			return any;
		}
		return result;
	}

	public double getStarsCost(byte ownerId) {
		double result = 0;
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					result += star.valueability();
				}
			}
		}
		return result;
	}

	public double getEnemyDanger(byte ownerId) {
		double result = 0;
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					result += star.getEnemyDangerFor(ownerId);
				}
			}
		}
		return result;
	}
	
	public double getDefencePriority(byte ownerId) {
		double cost = getStarsCost(ownerId);
		double enemyDanger = getEnemyDanger(ownerId);
		return Math.sqrt(cost) * enemyDanger * enemyDanger;
	}
	
	/**
	 * @param ownerId id of AI calling this method
	 * @return value determining how dangerous it is in the region.
	 *		0, if neutral
	 *		positive value, if region is safe
	 *		negative value, if region is dangerous
	 */
	public double getSumDangerDelta(byte ownerId) {
		double dangerBy = 0;
		double dangerFor = 0;
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					dangerBy += star.getDangerBy(ownerId);
					dangerFor += star.getEnemyDangerFor(ownerId);
				}
			}
		}
		return dangerBy - dangerFor;
	}
	
	public boolean hasAttackedStars(byte ownerId) {
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					double attackPower = star.getAttackPower();
					if (attackPower > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public ArrayList<AttackedStar> getAttackedStars(byte ownerId) {
		ArrayList<AttackedStar> result = new ArrayList<>();
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					double attackPower = star.getAttackPower();
					double defencePower = star.getDefencePower();
					if (attackPower > 0) {
						result.add(new AttackedStar(star.coordinates, attackPower - defencePower));
					}
				}
			}
		}
		return result;
	}

	public static ArrayList<Coordinates> getCoordinatesOf(ArrayList<AttackedStar> src) {
		ArrayList<Coordinates> result = new ArrayList<>();
		for (AttackedStar star: src) {
			result.add(star.coordinates);
		}
		return result;
	}

	public boolean hasAnyStarsHere(byte ownerId) {
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getOwnerId() == ownerId) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<Ship> getFreeShips(byte ownerId) {
		ArrayList<Ship> result = new ArrayList<>();
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ownerId && !star.isUnderAttack(ownerId)) {
					for (Ship ship: star.orbit) {
						if (ship.destination.equals(ship.position) && ship.base != Ship.BASE_FREIGHTER) {
							result.add(ship);
						}
					}
				}
			}
		}
		return result;
	}
	
	public Coordinates getStarWithShips(byte ownerId) {
		for (Star[] stars: sector) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ownerId) {
					return star.coordinates;
				}
			}
		}
		return null;
	}
	
	public static Coordinates sectorOf(Star star) {
		byte x = (byte) (star.coordinates.x / Sector.SIZE);
		byte y = (byte) (star.coordinates.y / Sector.SIZE);
		return new Coordinates(x, y);
	}

	public void i(String arg) {
		Log.i(LOG_TAG, arg);
	}

	public Iterator iterator() {
		return new Iterator();
	}

	private class Iterator implements java.util.Iterator<Star[]> {
		private int index;
		public Iterator() {
			this.index = -1;
		}
		@Override
		public boolean hasNext() {
			return index + 1 < Sector.this.sector.length;
		}
		@Override
		public Star[] next() {
			index++;
			return Sector.this.sector[index];
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
