package ru.barsk.moc;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import ru.barsk.util.Lib;
import ru.barsk.util.LongDouble;
import ru.barsk.util.Tools;

public class AI extends AbstractAI {

	public static final String LOG_TAG;
	public static final int numberOfTurnsNotCaringAboutNegativeIncome;
	public static final int criticalNumberOfTurnsNotCaringAboutNegativeIncome;
	public static final double CRITICAL_DANGER;
	public static final double COLONIZATION_CRITICAL_DANGER;
	public static final double DANGER_SHIP_0;
	public static final double DANGER_SHIP_1;
	public static final double DANGER_SHIP_2;
	public static final double DANGER_SHIP_3;
	public static final double DANGER_SHIP_4;
	public static final double DANGER_SHIP_5;
	public static final double COST_SHIP_0;
	public static final double COST_SHIP_1;
	public static final double COST_SHIP_2;
	public static final double COST_SHIP_3;
	public static final double COST_SHIP_4;
	public static final double COST_SHIP_5;

	public double totalIncome;
	private boolean[][] starsBeingColonized;

	static {
		LOG_TAG = "AI";
		numberOfTurnsNotCaringAboutNegativeIncome = 25;
		criticalNumberOfTurnsNotCaringAboutNegativeIncome = 10;
		CRITICAL_DANGER = 0.75;
		COLONIZATION_CRITICAL_DANGER = 4;
		ShipTemplate template;
		template = Lib.ShipTemplates.getShipTemplate(0);
		DANGER_SHIP_0 = template.danger();
		COST_SHIP_0 = template.productionCost;
		template = Lib.ShipTemplates.getShipTemplate(1);
		DANGER_SHIP_1 = template.danger();
		COST_SHIP_1 = template.productionCost;
		template = Lib.ShipTemplates.getShipTemplate(2);
		DANGER_SHIP_2 = template.danger();
		COST_SHIP_2 = template.productionCost;
		template = Lib.ShipTemplates.getShipTemplate(3);
		DANGER_SHIP_3 = template.danger();
		COST_SHIP_3 = template.productionCost;
		template = Lib.ShipTemplates.getShipTemplate(4);
		DANGER_SHIP_4 = template.danger();
		COST_SHIP_4 = template.productionCost;
		DANGER_SHIP_5 = 0;
		COST_SHIP_5 = Lib.ShipTemplates.getShipTemplate(5).productionCost;
	}
	
    public AI(byte ID, byte colorId, String name) {
        super(ID, colorId, name);
    }

	public AI(byte ID, byte colorId, double treasury, String name, @Nullable LongDouble IT) {
		super(ID, colorId, treasury, name, IT);
	}
	
	@Override
	public void manageGalaxy(Star[][] galaxy) {
		invalidateTotalIncome(galaxy);
		buildShips(galaxy);
		super.manageGalaxy(galaxy);
		starsBeingColonized = null;
	}

	@Override
    public void manageStar(Star star) {
        double requiredFood;
        if (star.getFoodStored() < 50) {
            requiredFood = star.getPopulation() + 0.1;
        }
        else {
            if (star.getFoodStored() > 100) {
                requiredFood = star.getPopulation() - 0.1;
            }
            else {
                requiredFood = star.getPopulation();
            }
        }
        double newFarmersPercentage;
        double newWorkersPercentage;
        double newScientistsPercentage;
        newFarmersPercentage = requiredFood / (star.getPopulation() * star.getAbsoluteFertility());
        if (newFarmersPercentage > 1) {
            newFarmersPercentage = 1;
        }
        double sumPercentageLeft = 1 - newFarmersPercentage;
        if (star.constructionQueue.hasFirst()) {
			star.shouldFillStorage = true;
            newWorkersPercentage = sumPercentageLeft;
            newScientistsPercentage = 0;
        }
        else {
            if  (star.getProductionStored() == star.getMaxStored()) {
				star.shouldFillStorage = false;
				newWorkersPercentage = 0.2;
				if (newWorkersPercentage > sumPercentageLeft) {
					newWorkersPercentage = sumPercentageLeft;
					newScientistsPercentage = 0;
				}
				else {
					newScientistsPercentage = sumPercentageLeft - newWorkersPercentage;
				}
            }
            else {
				star.shouldFillStorage = true;
                newWorkersPercentage = 5 / (star.getPopulation() * star.getAbsoluteRichness());
                if (newWorkersPercentage > sumPercentageLeft) {
                    newWorkersPercentage = sumPercentageLeft;
                    newScientistsPercentage = 0;
                }
                else {
                    newScientistsPercentage = sumPercentageLeft - newWorkersPercentage;
                }
            }
        }
        star.colonyManager.setPercentage(newFarmersPercentage, newWorkersPercentage, newScientistsPercentage);
		manageBuildings(star);
    }
	
	public void manageBuildings(Star star) {
		if (star.getPopulation() < 5) {
			removeBuildingsFromConstructions(star);
			return;
		}
		if (hasBuildingsInConstruction(star)) {
			return;
		}
		if (!hasBuilding(star, Building.ID_FACTORY)) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_FACTORY), Building.ID_FACTORY, 1, Building.getName(Building.ID_FACTORY, 1)));
			return;
		}
		if (!hasBuilding(star, Building.ID_BANK)) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_BANK), Building.ID_BANK, 1, Building.getName(Building.ID_BANK, 1)));
			return;
		}
		if (!hasBuilding(star, Building.ID_FARM)) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_FARM), Building.ID_FARM, 1, Building.getName(Building.ID_FARM, 1)));
			return;
		}
		if (!hasBuilding(star, Building.ID_CITY)) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_CITY), Building.ID_CITY, 1, Building.getName(Building.ID_CITY, 1)));
			return;
		}
		/*if (!hasBuilding(star, Building.ID_RESEARCH_CENTER)) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_RESEARCH_CENTER), Building.ID_RESEARCH_CENTER, 1, Building.getName(Building.ID_RESEARCH_CENTER, 1)));
			return;
		}*/
		Building alreadyConstructed;
		alreadyConstructed = star.buildings.getById(Building.ID_FACTORY);
		if (alreadyConstructed.level == 1) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_FACTORY), Building.ID_FACTORY, 2, Building.getName(Building.ID_FACTORY, 2)));
			return;
		}
		alreadyConstructed = star.buildings.getById(Building.ID_FARM);
		if (alreadyConstructed.level == 1) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_FARM), Building.ID_FARM, 2, Building.getName(Building.ID_FARM, 2)));
			return;
		}
		alreadyConstructed = star.buildings.getById(Building.ID_CITY);
		if (alreadyConstructed.level == 1) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_CITY), Building.ID_CITY, 2, Building.getName(Building.ID_CITY, 2)));
			return;
		}
		/*alreadyConstructed = star.buildings.getById(Building.ID_RESEARCH_CENTER);
		if (alreadyConstructed.level == 1) {
			star.constructionQueue.offer(new BuildingTemplate(Building.getCost(Building.ID_RESEARCH_CENTER), Building.ID_RESEARCH_CENTER, 2, Building.getName(Building.ID_RESEARCH_CENTER, 2)));
			return;
		}*/
		if (hasBuilding(star, Building.ID_RESEARCH_CENTER)) {
			for (Building b: star.buildings) {
				if (b.typeId == Building.ID_RESEARCH_CENTER) {
					star.buildings.remove(b);
					return;
				}
			}
		}
	}

	@Override
	public void manageShips(Star[][] galaxy) {
		manageColonizers(galaxy); //done
		rushProductionInAttackedStars(galaxy); //done
		moveShipsToAttackedStars(); //done
		moveShipsBetweenSectors(); //debug
		moveShipsInsideSectors(); //done
		moveShipsToAttackEnemy(galaxy); //debug
	}

	public void moveShipsInsideSectors() {
		for (Sector[] sectors: Sectors.galaxy) {
			for (Sector sector: sectors) {
				Coordinates endangered = sector.getMostEndangeredStar(ID);
				if (endangered == null) continue;
				for (Star[] stars: sector) {
					for (Star star: stars) {
						if (star.getShipsOnOrbitOwnerId() == ID && star.getAttackPower() == 0) {
							for (Ship ship: star.orbit) {
								if (ship.base != Ship.BASE_FREIGHTER && ship.destination.equals(ship.position)) {
									ship.destination = endangered.copy();
								}
							}
						}
					}
				}
			}
		}
	}

	public void moveShipsBetweenSectors() {
		double[][] defencePriority = new double[Sectors.galaxy.length][Sectors.galaxy[0].length];
		double[][] dangerDelta = new double[Sectors.galaxy.length][Sectors.galaxy[0].length]; //positive in save sectors, negative in dangerous ones
		for (int x = 0; x < Sectors.galaxy.length; x++) {
			for (int y = 0; y < Sectors.galaxy[x].length; y++) {
				double sectorDefencePriority = Sectors.galaxy[x][y].getDefencePriority(ID);
				double sectorDangerDelta = Sectors.galaxy[x][y].getSumDangerDelta(ID);
				defencePriority[x][y] = sectorDefencePriority;
				dangerDelta[x][y] = sectorDangerDelta;
			}
		}
		for (byte x = 0; x < Sectors.galaxy.length; x++) {
			for (byte y = 0; y < Sectors.galaxy[x].length; y++) {
				peacefulMoveShips(dangerDelta, defencePriority, x, y);
			}
		}
	}

	public void peacefulMoveShips(double[][] dangerDelta, double[][] defencePriorities, byte currentX, byte currentY) {
		double maxDefencePriority = defencePriorities[currentX][currentY];
		Coordinates coordinatesWithMaxDefencePriority = new Coordinates(currentX, currentY);
		for (byte x = (byte) Tools.max(currentX - 1, 0); x <= Tools.min(currentX + 1, defencePriorities.length - 1); x++) {
			for (byte y = (byte) Tools.max(currentY - 1, 0); y <= Tools.min(currentY + 1, defencePriorities[x].length - 1); y++) {
				double currentDefencePriority = defencePriorities[x][y];
				if (currentDefencePriority > maxDefencePriority) {
					maxDefencePriority = currentDefencePriority;
					coordinatesWithMaxDefencePriority.x = x;
					coordinatesWithMaxDefencePriority.y = y;
				}
			}
		}
		if (!(coordinatesWithMaxDefencePriority.x == currentX && coordinatesWithMaxDefencePriority.y == currentY)) {
			double maxShipDangerWeCanAffordToMove = dangerDelta[currentX][currentY];
			double shipDangerTheyNeed = -dangerDelta[coordinatesWithMaxDefencePriority.x][coordinatesWithMaxDefencePriority.y];
			if (shipDangerTheyNeed > 0) {
				if (maxShipDangerWeCanAffordToMove > 0) {
					double shipDangerToMove = Tools.min(shipDangerTheyNeed, maxShipDangerWeCanAffordToMove);
					if (shipDangerToMove > DANGER_SHIP_2) {
						Coordinates from = Sectors.galaxy[currentX][currentY].getStarWithShips(ID);
						if (from != null) {
							Coordinates target = Sectors.galaxy[coordinatesWithMaxDefencePriority.x][coordinatesWithMaxDefencePriority.y].getMostEndangeredStar(ID);
							do {
								ArrayList<Ship> fromOrbit = MainActivity.galaxy[from.x][from.y].orbit;
								Ship suitableShip = getMostSuitableShip(fromOrbit, shipDangerToMove);
								double currentShipDanger = suitableShip.danger();
								suitableShip.destination = target;
								shipDangerToMove -= currentShipDanger;
							}
							while (shipDangerToMove > 0);
						}
					}
				}
			}
		}
	}

	public Ship getMostSuitableShip(ArrayList<Ship> orbit, double dangerLimit) {
		double maxSuitableDanger = orbit.get(0).danger();
		double minDanger = orbit.get(0).danger();
		Ship result = null;
		Ship minDangerShip = orbit.get(0);
		for (Ship ship : orbit) {
			double shipDanger = ship.danger();
			if (shipDanger > maxSuitableDanger && shipDanger < dangerLimit && ship.destination.equals(ship.position)) {
				result = ship;
				maxSuitableDanger = shipDanger;
			}
			if (shipDanger < minDanger && ship.destination.equals(ship.position)) {
				minDangerShip = ship;
				minDanger = shipDanger;
			}
		}
		if (result != null) {
			return result;
		} else {
			return minDangerShip;
		}
	}

	public void moveShipsToAttackedStars() {
		for (Sector[] sectors: Sectors.galaxy) {
			for (Sector sector: sectors) {
				if (sector.hasAttackedStars(ID)) {
					sector.callShipsToAttackedStars(ID);
				}
			}
		}
	}

	public double getSumShipDanger(Star[][] galaxy) {
		double result = 0;
		for (Star[] stars : galaxy) {
			for (Star star : stars) {
				if (star.getShipsOnOrbitOwnerId() == ID) {
					for (Ship ship : star.orbit) {
						result += ship.danger();
					}
				}
			}
		}
		return result;
	}

	public double getShipsDangerDelta(Star[][] galaxy) {
		double result = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ID) {
					for (Ship ship: star.orbit) {
						result += ship.danger();
					}
				}
				else {
					for (Ship ship: star.orbit) {
						result -= 2 * ship.danger();
					}
				}
				if (star.getOwnerId() == ID) {
					result += star.constructionQueue.getShipsDanger();
				}
			}
		}
		return result;
	}

	public void removeBuildingsFromConstructions(Star star) {
		int i = 0;
		while (i < star.constructionQueue.size()) {
			ArtificialConstruction construction = star.constructionQueue.get(i);
			if (construction instanceof BuildingTemplate) {
				BuildingTemplate template = (BuildingTemplate) construction;
				if (template.typeId != Building.ID_CITY && template.typeId != Building.ID_FACTORY) {
					star.setProductionStored(star.getProductionStored() + template.initialProductionCost - template.productionCost);
					star.constructionQueue.remove(i);
				}
				else {
					i++;
				}
			}
			else {
				i++;
			}
		}
	}

	public boolean hasBuildingsInConstruction(Star star) {
		for (ArtificialConstruction construction: star.constructionQueue) {
			if (construction instanceof BuildingTemplate) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasBuilding(Star star, int typeId) {
		return star.buildings.contains(typeId) || star.constructionQueue.contains(typeId);
	}

	@Override
	public void manageScience() {
		//TODO
	}
	
	public void manageColonizers(Star[][] galaxy) {
		foundColonies(galaxy);
		if (hasFreeColonizersOnOrbits(galaxy)) {
			if (hasSuitableNeutralStars(galaxy)) {
				moveColonizers(galaxy);
			}
			else {
				removeAllColonizers(galaxy, !hasValueableNeutralStars(galaxy));
			}
		}
		else {
			if (hasValueableNeutralStars(galaxy)) {
				if (!hasColonizersInConstruction(galaxy)) {
					Coordinates idle = getMostIdleStar(galaxy);
					galaxy[idle.x][idle.y].constructionQueue.offer(Lib.ShipTemplates.getShipTemplate(Ship.BASE_FREIGHTER));
				}
			}
			else {
				removeAllColonizers(galaxy, false);
			}
		}
	}

	public boolean hasEnoughMoney() {
		if (totalIncome >= 0) {
			return true;
		}
		else {
			double turnlyDecay = -totalIncome;
			double numberOfTurnsToHavePositiveTreasury = treasury / turnlyDecay;
			return numberOfTurnsToHavePositiveTreasury >= numberOfTurnsNotCaringAboutNegativeIncome;
		}
	}

	public boolean hasEnoughMoneyFor(double maintenance) {
		double newIncome = totalIncome - maintenance;
		if (newIncome >= 0) {
			return true;
		}
		else {
			double turnlyDecay = -newIncome;
			double numberOfTurnsToHavePositiveTreasury = treasury / turnlyDecay;
			return numberOfTurnsToHavePositiveTreasury >= numberOfTurnsNotCaringAboutNegativeIncome;
		}
	}

	public boolean hasSuitableNeutralStars(Star[][] galaxy) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == 0) {
					if (isSuitableForColonization(star) && star.getDangerFor(ID) < COLONIZATION_CRITICAL_DANGER) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean hasValueableNeutralStars(Star[][] galaxy) {
		starsBeingColonized = new boolean[galaxy.length][galaxy[0].length];
		for (int x = 0; x < starsBeingColonized.length; x++) {
			for (int y = 0; y < starsBeingColonized[x].length; y++) {
				starsBeingColonized[x][y] = false;
			}
		}
		for (Star[] stars: galaxy) {
			for (Star star : stars) {
				if (star.getShipsOnOrbitOwnerId() == ID) {
					for (Ship ship : star.orbit) {
						if (ship.base == Ship.BASE_FREIGHTER && !ship.destination.equals(ship.position)) {
							starsBeingColonized[ship.destination.x][ship.destination.y] = true;
						}
					}
				}
			}
		}
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == 0) {
					if (isSuitableForColonization(star)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void removeAllColonizers(Star[][] galaxy, boolean removeExisting) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() != ID) {
					continue;
				}
				int i = 0;
				while (i < star.constructionQueue.size()) {
					ArtificialConstruction construction = star.constructionQueue.get(i);
					if (construction instanceof ShipTemplate) {
						ShipTemplate template = (ShipTemplate) construction;
						if (template.base == Ship.BASE_FREIGHTER) {
							star.setProductionStored(star.getProductionStored() + template.initialProductionCost - template.productionCost);
							star.constructionQueue.remove(i);
						}
						else {
							i++;
						}
					}
					else {
						i++;
					}
				}
				if (removeExisting) {
					i = 0;
					while (i < star.orbit.size()) {
						if (star.orbit.get(i).base == Ship.BASE_FREIGHTER) {
							star.orbit.remove(i);
						}
						else {
							i++;
						}
					}
				}
			}
		}
	}

	public boolean hasFreeColonizersOnOrbits(Star[][] galaxy) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ID) {
					for (Ship ship: star.orbit) {
						if (ship.base == Ship.BASE_FREIGHTER && ship.destination.equals(ship.position)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean hasColonizersInConstruction(Star[][] galaxy) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == ID) {
					for (ArtificialConstruction construction: star.constructionQueue) {
						if (construction instanceof ShipTemplate) {
							ShipTemplate template = (ShipTemplate) construction;
							if (template.base == Ship.BASE_FREIGHTER) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	

	public Coordinates getMostSuitableNeutralStar(Star[][] galaxy) {
		double maxSuitability = 0;
		Coordinates result = new Coordinates((byte) 0, (byte) 0);
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == 0 && !starsBeingColonized[star.coordinates.x][star.coordinates.y] && isSuitableForColonization(star)) {
					double suitability = star.suitability(ID);
					if (suitability > maxSuitability) {
						maxSuitability = suitability;
						result.x = star.coordinates.x;
						result.y = star.coordinates.y;
					}
				}
			}
		}
		return result;
	}

	public boolean isSuitableForColonization(Star star) {
		if (starsBeingColonized != null) {
			if (starsBeingColonized[star.coordinates.x][star.coordinates.y]) {
				return false;
			}
		}
		boolean underAttack = star.isUnderAttack(ID);
		return star.getFertility() >= 1.5 && star.getRichness() >= 1 && star.valueability() >= 50 && !underAttack;
	}

	public void foundColonies(Star[][] galaxy) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				boolean suitable = isSuitableForColonization(star);
				if (star.getOwnerId() == 0 && star.hasColonistOnOrbit() && suitable && star.getShipsOnOrbitOwnerId() == ID) {
					star.colonize(ID);
					star.removeOneColonizer();
				}
			}
		}
	}

	public void moveColonizers(Star[][] galaxy) {
		starsBeingColonized = new boolean[galaxy.length][galaxy[0].length];
		for (int x = 0; x < starsBeingColonized.length; x++) {
			for (int y = 0; y < starsBeingColonized[x].length; y++) {
				starsBeingColonized[x][y] = false;
			}
		}
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ID) {
					for (Ship ship : star.orbit) {
						if (ship.base == Ship.BASE_FREIGHTER && !ship.destination.equals(ship.position)) {
							starsBeingColonized[ship.destination.x][ship.destination.y] = true;
						}
					}
				}
			}
		}
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ID) {
					for (Ship ship: star.orbit) {
						if (ship.base == Ship.BASE_FREIGHTER && ship.destination.equals(ship.position)) {
							ship.destination = getMostSuitableNeutralStar(galaxy);
							starsBeingColonized[ship.destination.x][ship.destination.y] = true;
						}
					}
				}
			}
		}
	}

	public int getNumberOfBusyTurns(Star star) {
		double totalProductionCost = star.constructionQueue.getTotalProductionCost();
		double turnlyProduction = star.getProductionProduced();
		double result = totalProductionCost / turnlyProduction;
		return Tools.roundTurns(result);
	}

	public Coordinates getMostIdleStar(Star[][] galaxy) {
		double minTurns = 0;
		boolean minTurnsInitialized = false;
		Coordinates result = new Coordinates((byte) 0, (byte) 0);
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == ID) {
					double turnsToFinish = getNumberOfBusyTurns(star);
					if (!minTurnsInitialized) {
						minTurns = turnsToFinish;
						result.x = star.coordinates.x;
						result.y = star.coordinates.y;
						minTurnsInitialized = true;
					}
					else {
						if (turnsToFinish < minTurns) {
							minTurns = turnsToFinish;
							result.x = star.coordinates.x;
							result.y = star.coordinates.y;
						}
					}
				}
			}
		}
		return result;
	}

	public void rushProductionInAttackedStars(Star[][] galaxy) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.isUnderAttack(ID)) {
					int firstShipIndex = star.constructionQueue.getFirstShipIndex();
					while (firstShipIndex != -1 && star.getProductionStored() != 0) {
						star.rushProduction(firstShipIndex);
						firstShipIndex = star.constructionQueue.getFirstShipIndex();
					}
				}
			}
		}
	}

	public void moveShipsToAttackEnemy(Star[][] galaxy) {
		Coordinates targetCoordinates = getAttackableStar(galaxy);
		if (targetCoordinates != null) {
			Star targetStar = galaxy[targetCoordinates.x][targetCoordinates.y];
			attack(targetStar, galaxy);
		}
	}
	
	public Coordinates getClosestValueableEnemyStar(Star[][] galaxy) {
		Coordinates result = new Coordinates((byte) 0, (byte) 0);
		Coordinates any = new Coordinates((byte) 0, (byte) 0);
		double minDistance = 50;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				byte ownerId = star.getOwnerId();
				if (ownerId != ID && ownerId != 0) {
					if (star.valueability() > 100) {
						double distance = star.distanceTo(ID);
						if (distance < minDistance) {
							minDistance = distance;
							result.x = star.coordinates.x;
							result.y = star.coordinates.y;
						}
					}
					else {
						any.x = star.coordinates.x;
						any.y = star.coordinates.y;
					}
				}
			}
		}
		if (result.x == 0 && result.y == 0) {
			if (any.x == 0 && any.y == 0) {
				return null;
			}
			return any;
		}
		return result;
	}

	public void invalidateTotalIncome(Star[][] galaxy) {
		totalIncome = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == ID) {
					totalIncome += star.getIncome();
					totalIncome -= star.getBuildingsCost();
					totalIncome -= star.getConstructionsCost();
				}
				if (star.getShipsOnOrbitOwnerId() == ID) {
					totalIncome -= star.getSumShipsCost();
				}
			}
		}
	}

	public void buildShips(Star[][] galaxy) {
		double totalDanger = getShipsDangerDelta(galaxy);
		if (totalDanger < 0) {
			Coordinates idleCoordinates = getMostIdleStar(galaxy);
			if (idleCoordinates.x != 0 || idleCoordinates.y != 0) {
				Star idleStar = galaxy[idleCoordinates.x][idleCoordinates.y];
				byte maxBuildableShip = getMaxBuildableShip();
				if (maxBuildableShip != -1) {
					addShip(idleStar, maxBuildableShip);
				}
			}
		}
	}

	public byte getMaxBuildableShip() {
		if (hasEnoughMoneyFor(Ship.getMaintenance(Ship.BASE_HUGE))) {
			return Ship.BASE_HUGE;
		}
		if (hasEnoughMoneyFor(Ship.getMaintenance(Ship.BASE_LARGE))) {
			return Ship.BASE_LARGE;
		}
		if (hasEnoughMoneyFor(Ship.getMaintenance(Ship.BASE_MEDIUM))) {
			return Ship.BASE_MEDIUM;
		}
		if (hasEnoughMoneyFor(Ship.getMaintenance(Ship.BASE_SMALL))) {
			return Ship.BASE_SMALL;
		}
		if (hasEnoughMoneyFor(Ship.getMaintenance(Ship.BASE_TINY))) {
			return Ship.BASE_TINY;
		}
		return -1;
	}

	public void addShip(Star star, int index) {
		if (star.getOwnerId() == ID) {
			star.constructionQueue.offer(Lib.ShipTemplates.getShipTemplate(index));
			totalIncome -= Ship.getMaintenance(index);
		}
		else {
			throw new AIPermissionException();
		}
	}

	public Coordinates getAttackableStar(Star[][] galaxy) {
		Coordinates result = null;
		double maxOurDanger = 0;
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() != ID && star.getOwnerId() != 0) {
					double powerByUs = star.getPowerBy(ID);
					double enemyPowerForUs = star.getEnemyPowerFor(ID);
					double power = powerByUs - enemyPowerForUs;
					i("poweeByUs = " + powerByUs);
					i("enemyPowerForUs = " + enemyPowerForUs);
					i("power = " + power);
					if (powerByUs >= enemyPowerForUs * 1.25 && power > maxOurDanger) {
						maxOurDanger = power;
						result = star.coordinates;
					}
				}
			}
		}
		if (maxOurDanger > 100) {
			return result;
		}
		else {
			return null;
		}
	}

	public void attack(Star starToAttack, Star[][] galaxy) {
		Coordinates starSector = Sector.sectorOf(starToAttack);
		int fromX = (int) Tools.max(starSector.x - 1, 0);
		int toX = (int) Tools.min(starSector.x + 1, Sectors.galaxy.length - 1);
		int fromY = (int) Tools.max(starSector.y - 1, 0);
		int toY = (int) Tools.min(starSector.y + 1, Sectors.galaxy[0].length - 1);
		for (int x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				ArrayList<Ship> ships = Sectors.galaxy[x][y].getFreeShips(ID);
				for (Ship ship: ships) {
					ship.destination = starToAttack.coordinates;
				}
			}
		}
	}

	private static void i(String arg) {
		Log.i(LOG_TAG, arg);
	}

	private static void w(String arg) {
		Log.w(LOG_TAG, arg);
	}
	
	public static void toast(String arg) {
		Toast.makeText(MainActivity.context, arg, Toast.LENGTH_SHORT).show();
	}
	
}
