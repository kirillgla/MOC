package ru.barsk.moc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;

import static ru.barsk.moc.Building.*;
import android.util.Log;
import android.widget.Toast;

import ru.barsk.util.BuildingsTreeSet;
import ru.barsk.util.ConstructionQueue;
import ru.barsk.util.Lib;
import ru.barsk.util.LongDouble;
import ru.barsk.util.Tools;

public final class Star {

	public static final String LOG_TAG = "Star";

	public ColonyManager colonyManager;
    public String name;
    public final byte type;
    /* Visual feature.
     * 0=void, 1=small blue, 2=small red,
     * 3=purple, 4=red giant, 5 = blue giant,
     * 6=sun-like, 7=white
     */
    private double fertility;
    private double richness;
    public double mineralsExtractedCounter;
    private double scientificPotential;
    public double ItProducedThisTurn;
    private double maxPopulation;
    private double population;
    private byte ownerId;
    private double morale;
    private int maxStored;
    private double foodStored;
    private double productionStored;
	public final Coordinates coordinates;
	private boolean hasReachedOneBillion;
    //public double distanceToTheClosestEnemySystem; gonna be used by AI.
    //public double valueability; gonna be used by AI
	public BuildingsTreeSet buildings;
    public ArrayList<Ship> orbit;
    public static final byte SHIPYARD_LIMIT = 10;
    public ConstructionQueue constructionQueue;
	public boolean shouldFillStorage;

    public Star(byte type, String name, byte ownerId, ColonyManager colonyManager, byte x, byte y) {
		shouldFillStorage = true;
		buildings = new BuildingsTreeSet();
		orbit = new ArrayList<>();
		constructionQueue = new ConstructionQueue(SHIPYARD_LIMIT);
		this.coordinates = new Coordinates(x, y);
		if (colonyManager == null) {
			this.colonyManager = new ColonyManager();
		}
		else {
			this.colonyManager = colonyManager;
		}
        if (ownerId > 0) {
            this.type = 6;
        }
        else {
            if (type < 0 || type > 7 ) {
                this.type = 0;
            }
            else {
                this.type = type;
            }
        }
        if (this.type == 0) {
            this.name = "";
            this.setFertility(0);
            this.setRichness(0);
            this.setScientificPotential(0);
            this.setMaxPopulation(0);
            this.setPopulation(0);
            this.setOwnerId((byte) (0));
            this.setMorale(0);
            this.setMaxStored(0);
            this.setFoodStored(0);
            this.setProductionStored(0);
        }
        else {
            this.name = name;
            if (ownerId < 0) {
                this.setOwnerId((byte) (0));
            }
            else {
                this.setOwnerId(ownerId);
            }
            if (ownerId == 0) {
                switch (this.type) {
                    case 1: this.setFertility(Math.round(Math.random() * 100) / 100.0); break;
                    case 2: this.setFertility(Math.round(Math.random() * 200) / 100.0); break;
                    case 3: this.setFertility(Math.round(Math.random() * 300) / 100.0); break;
                    case 4: this.setFertility(Math.round(Math.random() * 200 + 100) / 100.0); break;
                    case 5: this.setFertility(Math.round(Math.random() * 200 + 200) / 100.0); break;
                    case 6: this.setFertility(Math.round(Math.random() * 200 + 200) / 100.0); break;
                    case 7: this.setFertility(Math.round(Math.random() * 200 + 300) / 100.0); break;
                    default: this.setFertility(0);
                }
                this.setRichness(Math.round(Math.random() * 500) / 100.0);
                this.setScientificPotential(1);
                this.setMaxPopulation(Math.round(fertility * 100) / 10.0);
                this.setPopulation(0);
                this.setMorale(0);
                this.setMaxStored(50);
                this.setFoodStored(0);
                this.setProductionStored(0);
				this.hasReachedOneBillion = false;
            }
            else {
                this.setFertility(3);
                this.setRichness(2);
                this.setScientificPotential(2);
                this.setMaxPopulation(30);
                this.setPopulation(this.getMaxPopulation());
                this.setMorale(10);
                this.setMaxStored(150);
                this.setFoodStored(5);
                this.setProductionStored(5);
				Collections.addAll(
                        orbit,
                    	Lib.ShipTemplates.getShipTemplate(0).construct(this.ownerId, coordinates.copy()),
						Lib.ShipTemplates.getShipTemplate(0).construct(this.ownerId, coordinates.copy()),
						Lib.ShipTemplates.getShipTemplate(2).construct(this.ownerId, coordinates.copy()),
						Lib.ShipTemplates.getShipTemplate(5).construct(this.ownerId, coordinates.copy())
                );
				this.hasReachedOneBillion = true;
				buildings.add(makeBuilding(ID_FACTORY, 1));
				buildings.add(makeBuilding(ID_FACTORY, 2));
				buildings.add(makeBuilding(ID_BANK, 1));
            }
        }
    }
	
	public Star(byte type, String name, byte ownerId, byte x, byte y) {
		this(type, name, ownerId, null, x, y);
	}

	public Star(
			double farmers,
			double workers,
			double scientists,
			String name,
			byte type,
			double fertility,
			double richness,
			double mineralsExtractedCounter,
			double scientificPotential,
			double maxPopulation,
			double population,
			byte ownerId,
			double morale,
			int maxStored,
			double foodStored,
			double productionStored,
			byte x,
			byte y,
			boolean shouldFillStorage,
			ArrayList<Ship> orbit,
			BuildingsTreeSet buildings,
			ConstructionQueue constructionQueue
	) {
		this.colonyManager = new ColonyManager();
		this.colonyManager.setPercentage(farmers, workers, scientists);
		this.name = name;
		this.type = type;
		this.fertility = fertility;
		this.richness = richness;
		this.mineralsExtractedCounter = mineralsExtractedCounter;
		this.scientificPotential = scientificPotential;
		this.maxPopulation = maxPopulation;
		this.population = population;
		this.ownerId = ownerId;
		this.morale = morale;
		this.maxStored = maxStored;
		this.foodStored = foodStored;
		this.productionStored = productionStored;
		this.coordinates = new Coordinates(x, y);
		this.hasReachedOneBillion = population < 1.0;
		this.buildings = new BuildingsTreeSet();
		this.orbit = new ArrayList<>();
		this.constructionQueue = new ConstructionQueue(SHIPYARD_LIMIT);
		this.shouldFillStorage = shouldFillStorage;
		this.orbit = orbit;
		this.buildings = buildings;
		this.constructionQueue = constructionQueue;
	}

	@Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        StringBuilder result = new StringBuilder();
		result.append(this.name);
        result.append(": f=");
        result.append(Tools.round(this.getFertility(), (byte) 1));
        result.append(", r=");
        result.append(Tools.round(this.getRichness(), (byte) 1));
        result.append(", s=");
        result.append(Tools.round(this.getScientificPotential(), (byte) 1));
        result.append(", p=");
        result.append(Tools.round(this.getPopulation(), (byte) 1));
        result.append("/");
        result.append(Tools.round(this.getMaxPopulation() , (byte) 1));
        result.append(" (");
        result.append(this.getOwnerId());
        result.append(")");
        return result.toString();
    }

    public double getFertility() {return this.fertility;}
    public double getRichness() {return this.richness;}
    public double getScientificPotential() {return this.scientificPotential;}
    public double getPopulation() {return this.population;}
    public byte getOwnerId() {return this.ownerId;}
    public double getMorale() {return this.morale;}
    public int getMaxStored() {return this.maxStored;}
    public double getFoodStored() {return this.foodStored;}
    public double getProductionStored() {return this.productionStored;}

	public double getMaxPopulation() {
		if (buildings.contains(ID_CITY)) {
			if (buildings.getById(ID_CITY).level == 1) {
				return maxPopulation + 2.5;
			}
			return maxPopulation + 5;
		}
		return this.maxPopulation;
	}

	public double getFoodProduced() {
		return getPopulation() * colonyManager.farmersPercentage * getAbsoluteFertility();
	}
	
	public double getAbsoluteFertility() {
		if (buildings.contains(ID_FARM)) {
			if (buildings.getById(ID_FARM).level == 1) {
				return getFertility() + 0.5;
			}
			return getFertility() + 1;
		}
		return getFertility();
	}

	public double getProductionProduced() {
		return (getPopulation() * colonyManager.workersPercentage * getMorale() / 10. * getAbsoluteRichness()) * LongDouble.getProductionModifier(ownerId);
	}
	
	public double getAbsoluteRichness() {
		if (buildings.contains(ID_FACTORY)) {
			if (buildings.getById(ID_FACTORY).level == 1) {
				return getRichness() + 0.5;
			}
			return getRichness() + 1;
		}
		return getRichness();
	}

	public double getIt() {
		double busyPopulation = getPopulation() * colonyManager.scientistsPercentage * getMorale() / 10.;
		if (buildings.contains(ID_RESEARCH_CENTER)) {
			if (buildings.getById(ID_RESEARCH_CENTER).level == 1) {
				return busyPopulation * (getScientificPotential() + 0.5);
			}
			return busyPopulation * (getScientificPotential() + 1);
		}
		return busyPopulation * getScientificPotential();
	}

	public double getTaxIncome() {
		if (buildings.contains(ID_BANK)) {
			return getPopulation() * (colonyManager.tax + 0.05) * (getMorale() / 10.);
		}
		return colonyManager.tax * this.getPopulation() * this.getMorale() / 10.;
	}
	
	public double getIncome() {
		if (shouldFillStorage  || constructionQueue.hasFirst()) {
			return getTaxIncome();
		}
		else {
			return getTaxIncome() + getTradeIncome(getProductionProduced());
		}
	}
	
	public double getBuildingsCost() {
		double result = 0;
		for (Building building: buildings) {
			if (building != null) {
				result += building.maintenance;
			}
		}
		return result;
	}

	public double getConstructionsCost() {
		double result = 0;
		for (ArtificialConstruction construction: constructionQueue) {
			if (construction instanceof BuildingTemplate) {
				result += ((Building) construction.construct(Player.ID, new Coordinates((byte) 0, (byte) 0))).maintenance;
			}
			if (construction instanceof ShipTemplate) {
				result += Ship.getMaintenance(((ShipTemplate) construction).base);
			}
		}
		return result;
	}

    public byte getShipsOnOrbitOwnerId() {
        if (this.orbit == null || this.orbit.size() == 0) {
            return 0;
        }
        for (Ship ship: orbit) {
            if (ship != null) {
                return ship.ownerId;
            }
        }
        return 0;
    }

	public double getSumShipsCost() {
		double result = 0;
		for (Ship ship: orbit) {
			if (ship != null) {
				result += ship.maintenance;
			}
		}
		return result;
	}

    public int getNumberOfShipsOnOrbit() {
        int number = 0;
        int size = this.orbit.size();
        for (byte i = 0; i < size; i++) {
            if (this.orbit.get(i) != null) {
                number++;
            }
        }
        return number;
    }

    public void setFertility(double value) {
        if (value <= 0) {
            this.fertility = 0;
        }
        else {
            if (value >= 5) {
                this.fertility = 5;
            }
            else {
                this.fertility = value;
            }
        }
    }

    public void setRichness(double value) {
        if (value <= 0) {
            this.richness = 0;
        }
        else {
            if (value >= 5) {
                this.richness = 5;
            }
            else {
                this.richness = value;
            }
        }
    }

    public void addMineralsExtracted(double value) {
        this.mineralsExtractedCounter += value;
        if (this.mineralsExtractedCounter >= 5000) {
            this.mineralsExtractedCounter -= 5000;
            this.setRichness(this.getRichness()-0.1);
        }
    }

    public void setScientificPotential(double value) {
        if (value <= 0) {
            this.scientificPotential = 0;
        }
        else {
            if (value >= 5) {
                this.scientificPotential = 5;
            }
            else this.scientificPotential = value;
        }
    }

    public void setMaxPopulation(double value) {
        if (value <= 0) {
            this.maxPopulation = 0;
        }
        else {
            if (value >= 50) {
                this.maxPopulation = 50;
            }
            else {
                this.maxPopulation = value;
            }
        }
    }

    public void setPopulation(double value) {
		if (value < this.getPopulation()) {
			Tools.notifyPopulationDecay(this);
		}
        if (value <= 0) {
            this.population = 0;
            exterminate();
			Tools.notifyStarDead(this);
        }
        else {
			this.population = value;
        }
    }

    public void setOwnerId(byte value) {
        if (value <= 0) {
            this.ownerId = 0;
        }
        else {
            this.ownerId = value;
        }
    }

    public void setMorale(double value) {
        if (value <= 0) {
            this.morale = 0;
        }
        else {
            if (value >= 10) {
                this.morale = 10;
            }
            else {
                this.morale = value;
            }
        }
    }

    public void setMaxStored(int value) {
        if (value <= 10) {
            this.maxStored = 10;
        }
        else {
            if (value >= 500) {
                this.maxStored = 500;
            }
            else {
                this.maxStored = value;
            }
        }
    }

    public void setFoodStored(double value) {
        if (value <= 0) {
            this.foodStored = 0;
        }
        else {
            if (value >= this.getMaxStored()) {
                this.foodStored = this.getMaxStored();
            }
            else {
                this.foodStored = value;
            }
        }
    }

    public void setProductionStored(double value) {
        if (value <= 0) {
            this.productionStored = 0;
        }
        else {
            if (value >= this.getMaxStored()) {
                this.productionStored = this.getMaxStored();
            }
            else {
                this.productionStored = value;
            }
        }
    }

    public void simplifyOrbit() {
        int i = 0;
        while (i < orbit.size()) {
			if (orbit.get(i) == null) {
				orbit.remove(i);
			}
			else {
				i++;
			}
		}
    }

    public void rushProduction(int index) {
		double productionToRush = Tools.min(10, getProductionStored(), constructionQueue.get(index).productionCost);
		if (productionToRush == 0) {
			throw new NoSuchElementException();
		}
		this.setProductionStored(getProductionStored() - productionToRush);
		Object produced = constructionQueue.investProduction(productionToRush, ownerId, coordinates, index);
		if (produced != null) {
			if (produced instanceof Ship) {
				orbit.add((Ship) produced);
			}
			else {
				buildings.add((Building) produced);
			}
		}
    }
	
	private void naturalPopulationGrowth(double populationGrowthSpeed) {
		double newPopulation;
		if (populationGrowthSpeed >= 0) {
			double born = populationGrowthSpeed*this.getPopulation();
			double dead = populationGrowthSpeed*this.getPopulation()*this.getPopulation()/this.getMaxPopulation();
			double deltaP = born - dead;
			if (deltaP > 0 && deltaP < 0.05) {
				deltaP = 0.05;
			}
			if (deltaP < 0 && deltaP > -0.05) {
				deltaP = -0.05;
			}
			newPopulation = this.getPopulation()+deltaP;
		}
		else {
			newPopulation = this.getPopulation() * (1 + 2 * populationGrowthSpeed);
		}
		if ((newPopulation >= this.getMaxPopulation() - 0.01) && (newPopulation <= this.getMaxPopulation() + 0.01)) {
			newPopulation = this.getMaxPopulation();
		}
		if (newPopulation < 0.01) {
			newPopulation = 0;
		}
		this.setPopulation(newPopulation);
	}

    public void exterminate() {
        this.setOwnerId((byte) (0));
        this.setMorale(0);
    }

    public void conquer(byte ownerID) {
		byte previousOwner = getOwnerId();
		this.setOwnerId(ownerID);
        this.setMorale(this.getMorale() * 0.4);
		if (previousOwner == Player.ID) {
			//noinspection StringBufferReplaceableByString
			StringBuilder content = new StringBuilder();
			content.append(MainActivity.context.getString(R.string.your));
			content.append(" ");
			content.append(MainActivity.context.getString(R.string.colony));
			content.append(" (");
			content.append(this.name);
			content.append(") ");
			content.append(MainActivity.context.getString(R.string.colony_conquered));
			String message = content.toString();
			TurnEvent event = new TurnEvent(TurnEvent.Tag.OUR_SYSTEM_CAPTURED, this, message);
			MainActivity.events.add(event);
		}
		if (ownerID == Player.ID) {
			//noinspection StringBufferReplaceableByString
			StringBuilder content = new StringBuilder();
			content.append(MainActivity.context.getString(R.string.enemy));
			content.append(" ");
			content.append(MainActivity.context.getString(R.string.colony));
			content.append(" (");
			content.append(this.name);
			content.append(") ");
			content.append(MainActivity.context.getString(R.string.colony_conquered));
			String message = content.toString();
			TurnEvent event = new TurnEvent(TurnEvent.Tag.ENEMY_SYSTEM_CAPTURED, this, message);
			MainActivity.events.add(event);
		}
    }

    public void colonize(byte ownerID) {
        if (type != 0) {
            this.setPopulation(0.05);
            this.setMorale(10);
            this.setFoodStored(5);
            this.setProductionStored(5);
            this.setOwnerId(ownerID);
			if (ownerID == Player.ID) {
				//noinspection StringBufferReplaceableByString
				StringBuilder content = new StringBuilder();
				content.append(MainActivity.context.getString(R.string.star));
				content.append(" ");
				content.append(this.name);
				content.append(" ");
				content.append(MainActivity.context.getString(R.string.colony_found));
				String message = content.toString();
				TurnEvent event = new TurnEvent(TurnEvent.Tag.COLONY_FOUND, this, message);
				MainActivity.events.add(event);
			}
        }
    }

    public void onTurnPassed() {
        double populationGrowthSpeed = colonyManager.populationGrowthSpeed;
        double foodProduced = getFoodProduced();
        if (this.getFoodStored() + foodProduced >= this.getPopulation()) {
            this.setFoodStored(this.getFoodStored()+foodProduced-this.getPopulation());
            this.setMorale(this.getMorale() * 1.1);
        }
        else {
            if (this.getFoodStored()+foodProduced == 0) {
                populationGrowthSpeed = -2 * populationGrowthSpeed;
                this.setMorale(this.getMorale() * 0.9);
            }
            else {
                double food = this.getFoodStored() + foodProduced;
                populationGrowthSpeed = populationGrowthSpeed * (3 * food / this.getPopulation() - 2);
                this.setFoodStored(0);
                double newMorale = this.getMorale() * (food / (5 * this.getPopulation()) + 0.9);
                this.setMorale(newMorale);
            }
        }
        this.naturalPopulationGrowth(populationGrowthSpeed);
        double productionProduced = getProductionProduced();
        addMineralsExtracted(productionProduced);
		if (constructionQueue.hasFirst()) {
			double productionToInvest = Tools.min(productionProduced, constructionQueue.getFirstProductionLeft());
			setProductionStored(getProductionStored() + productionProduced - productionToInvest);
			Object produced = constructionQueue.investProduction(productionToInvest, ownerId, coordinates.copy());
			if (produced != null) {
				if (produced instanceof Ship) {
					orbit.add((Ship) produced);
					//noinspection StringBufferReplaceableByString
					StringBuilder content = new StringBuilder();
					content.append(MainActivity.context.getString(R.string.colony));
					content.append(" ");
					content.append(this.name);
					content.append(" ");
					content.append(MainActivity.context.getString(R.string.ship_constructed));
					content.append(" (");
					content.append(((Ship) produced).name);
					content.append(").");
					String message = content.toString();
					TurnEvent event = new TurnEvent(TurnEvent.Tag.SHIP_COMPLETE, this, message);
					MainActivity.events.add(event);
				}
				else {
					buildings.add((Building) produced);
					StringBuilder content = new StringBuilder();
					content.append(MainActivity.context.getString(R.string.colony));
					content.append(" ");
					content.append(this.name);
					content.append(" ");
					content.append(MainActivity.context.getString(R.string.building_constructed));
					content.append(" ");
					if (((Building) produced).typeId == Building.ID_FARM) {
						content.append(MainActivity.context.getString(R.string.farm2));
					}
					else {
						content.append(((Building) produced).getName());
					}
					content.append(".");
					String message = content.toString();
					TurnEvent event = new TurnEvent(TurnEvent.Tag.BUILDING_COMPLETE, this, message);
					MainActivity.events.add(event);
				}
			}
		}
		else {
			if (shouldFillStorage) {
				this.setProductionStored(this.getProductionStored() + productionProduced);
			}
		}
		ItProducedThisTurn = getIt();
		for (Ship ship:orbit) {
			ship.determineNewPosition();
		}
		if (this.getOwnerId() == Player.ID && colonyManager.workersPercentage != 0 && !constructionQueue.hasFirst() && shouldFillStorage) {
			//noinspection StringBufferReplaceableByString
			StringBuilder content = new StringBuilder();
			content.append(MainActivity.context.getString(R.string.colony));
			content.append(" ");
			content.append(this.name);
			content.append(" ");
			content.append(MainActivity.context.getString(R.string.colony_idle));
			String message = content.toString();
			TurnEvent event = new TurnEvent(TurnEvent.Tag.COLONY_IDLE, this, message);
			MainActivity.events.add(event);
		}
    }

	public void removeOneColonizer() {
		for (Ship ship: orbit) {
			if (ship.canColonize) {
				orbit.remove(ship);
				return;
			}
		}
		throw new NoSuchElementException("Colonizer not found!");
	}

	public boolean isColonizeable() {
		boolean isStar = type != 0;
		boolean isNeutral = getOwnerId() == 0;
		boolean player = getShipsOnOrbitOwnerId() == Player.ID;
		boolean hasColonist = hasColonistOnOrbit();
		return isStar && isNeutral && player && hasColonist;
	}
	
	public boolean hasColonistOnOrbit() {
		for (Ship ship: orbit) {
			if (ship.canColonize) {
				return true;
			}
		}
		return false;
	}

	public boolean anyShipSelected() {
		for (Ship ship: orbit) {
			if (ship.isSelectedByPlayer) {
				return true;
			}
		}
		return false;
	}

	public boolean hasDifferentShipsOnOrbit() {
		byte mainOwnerId = -1;
		for (Ship ship: orbit) {
			if (ship != null) {
				if (mainOwnerId == -1) {
					mainOwnerId = ship.ownerId;
				}
				else {
					if (ship.ownerId != mainOwnerId) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public ArrayList<Ship> getCombatSideOne() {
		ArrayList<Ship> result;
		result = new ArrayList<>();
		byte ownerId = -1;
		for (Ship ship: orbit) {
			if (ship != null) {
				if (ownerId == -1) {
					ownerId = ship.ownerId;
					result.add(ship);
				}
				else {
					if (ship.ownerId == ownerId) {
						result.add(ship);
					}
				}
			}
		}
		return result;
	}

	public ArrayList<Ship> getCombatSideTwo() {
		ArrayList<Ship> result;
		result = new ArrayList<>();
		byte firstOwnerId = -1;
		byte secondOwnerId = -1;
		for (Ship ship: orbit) {
			if (ship != null) {
				if (firstOwnerId == -1) {
					firstOwnerId = ship.ownerId;
				}
				else {
					if (ship.ownerId != firstOwnerId) {
						if (secondOwnerId == -1) {
							secondOwnerId = ship.ownerId;
							result.add(ship);
						}
						else {
							if (ship.ownerId == secondOwnerId) {
								result.add(ship);
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	public double getTradeIncome(double production) {
		return production * 0.05;
	}

	public double getDefencePower() {
		double result = 0;
		int fromX = (int) Tools.max(coordinates.x - 8, 0);
		int toX = (int) Tools.min(coordinates.x + 8, MainActivity.galaxySize - 1);
		int fromY = (int) Tools.max(coordinates.y - 8, 0);
		int toY = (int) Tools.min(coordinates.y + 8, MainActivity.galaxySize - 1);
		for (int x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				if (MainActivity.galaxy[x][y].getShipsOnOrbitOwnerId() == this.ownerId) {
					ArrayList<Ship> orbit = MainActivity.galaxy[x][y].orbit;
					for (Ship ship: orbit) {
						if (ship.destination.equals(this.coordinates)) {
							result += ship.danger();
						}
					}
				}
			}
		}
		return result;
	}

	/**@see #isUnderAttack(byte)*/
	public double getAttackPower() {
		double result = 0;
		int fromX = (int) Tools.max(coordinates.x - 8, 0);
		int toX = (int) Tools.min(coordinates.x + 8, MainActivity.galaxySize - 1);
		int fromY = (int) Tools.max(coordinates.y - 8, 0);
		int toY = (int) Tools.min(coordinates.y + 8, MainActivity.galaxySize - 1);
		for (int x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				if (MainActivity.galaxy[x][y].getShipsOnOrbitOwnerId() != this.ownerId) {
					ArrayList<Ship> orbit = MainActivity.galaxy[x][y].orbit;
					for (Ship ship: orbit) {
						if (ship.destination.equals(this.coordinates)) {
							result += ship.danger();
						}
					}
				}
			}
		}
		return result;
	}
	
	public boolean isUnderAttack(byte ownerId) {
		int fromX = (int) Tools.max(coordinates.x - 8, 0);
		int toX = (int) Tools.min(coordinates.x + 8, MainActivity.galaxySize - 1);
		int fromY = (int) Tools.max(coordinates.y - 8, 0);
		int toY = (int) Tools.min(coordinates.y + 8, MainActivity.galaxySize - 1);
		for (int x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				if (MainActivity.galaxy[x][y].getShipsOnOrbitOwnerId() != ownerId) {
					ArrayList<Ship> orbit = MainActivity.galaxy[x][y].orbit;
					for (Ship ship: orbit) {
						if (ship.destination.equals(this.coordinates)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param ownerId owner thinking about danger
	 * @return 1, if danger level is average
	 * 		   value greater then 1, if danger level is very high
	 * 		   value less then 1, if danger level is low enough
	 */
	public double getDangerFor(byte ownerId) {
		double enemyDangerFor = getEnemyDangerFor(ownerId);
		double dangerBy = getDangerBy(ownerId);
		if (dangerBy == 0) {
			dangerBy = 1;
		}
		if (enemyDangerFor == 0) {
			enemyDangerFor = 1;
		}
		return enemyDangerFor / dangerBy;
	}

	public double getEnemyDangerFor(double ownerId) {
		double result = 0;
		int fromX = (int) Tools.max(coordinates.x - 8, 0);
		int toX = (int) Tools.min(coordinates.x + 8, MainActivity.galaxySize - 1);
		int fromY = (int) Tools.max(coordinates.y - 8, 0);
		int toY = (int) Tools.min(coordinates.y + 8, MainActivity.galaxySize - 1);
		for (int x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				if (MainActivity.galaxy[x][y].getShipsOnOrbitOwnerId() == ownerId) {
					continue;
				}
				ArrayList<Ship> orbit = MainActivity.galaxy[x][y].orbit;
				double modifier = 1;
				if (x >= coordinates.x - 2 && x <= coordinates.x + 2 && y >= coordinates.y - 2 && y <= coordinates.y + 2) {
					modifier = 2;
				}
				if (x < coordinates.x - 4 || x > coordinates.x + 4 || y < coordinates.y - 4 || y > coordinates.y + 4) {
					modifier = 0.2;
				}
				for (Ship ship: orbit) {
					double danger = ship.danger() * modifier;
					result += danger;
				}
			}
		}
		return result;
	}

	public double getDangerBy(double ownerId) {
		double result = 0;
		int fromX = (int) Tools.max(coordinates.x - 4, 0);
		int toX = (int) Tools.min(coordinates.x + 4, MainActivity.galaxySize - 1);
		int fromY = (int) Tools.max(coordinates.y - 4, 0);
		int toY = (int) Tools.min(coordinates.y + 4, MainActivity.galaxySize - 1);
		for (int x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				if (MainActivity.galaxy[x][y].getShipsOnOrbitOwnerId() != ownerId) {
					continue;
				}
				ArrayList<Ship> orbit = MainActivity.galaxy[x][y].orbit;
				double modifier = 1;
				if (x >= coordinates.x - 2 && x <= coordinates.x + 2 && y >= coordinates.y - 2 && y <= coordinates.y + 2) {
					modifier = 2;
				}
				for (Ship ship: orbit) {
					double danger = ship.danger() * modifier;
					result += danger;
				}
			}
		}
		return result;
	}

	public double valueability() {
		return getAbsoluteFertility() * getAbsoluteRichness() * getScientificPotential() * (getMaxPopulation() + 2 * getPopulation());
	}

	public double suitability(byte ownerId) {
		double distance = distanceTo(ownerId);
		double valueability = valueability();
		if (valueability >= 200) {
			return valueability / Math.sqrt(distance);
		}
		return valueability / distance;
	}

	public double getShipPower() {
		double result = 0;
		for (Ship ship: orbit) {
			result += ship.danger();
		}
		return result;
	}
	
	public double distanceTo(byte ownerId) {
		ArrayList<Coordinates> closest = new ArrayList<>();
		recursiveSeekFor(ownerId, 1, closest);
		double[] distance = new double[closest.size()];
		if (distance.length == 0) {
			return -1;
		}
		for (int i = 0; i < distance.length; i++) {
			Coordinates current = closest.get(i);
			int deltaX = coordinates.x - current.x;
			int deltaY = coordinates.y - current.y;
			distance[i] = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		}
		return Tools.min(distance);
	}
	
	public void recursiveSeekFor(byte ownerId, int distance, ArrayList<Coordinates> to) {
		int fromX = coordinates.x - distance;
		int toX = coordinates.x + distance;
		int fromY = coordinates.y - distance;
		int toY = coordinates.y + distance;
		boolean recurse = false;
		if (fromX >= 0) {
			recurse = true;
			for (int y = (int) Tools.max(fromY, 0); y <= Tools.min(toY, MainActivity.galaxySize - 1); y++) {
				if (MainActivity.galaxy[fromX][y].getOwnerId() == ownerId) {
					to.add(MainActivity.galaxy[fromX][y].coordinates);
				}
			}
		}
		if (toX < MainActivity.galaxySize) {
			recurse = true;
			for (int y = (int) Tools.max(fromY, 0); y <= Tools.min(toY, MainActivity.galaxySize - 1); y++) {
				if (MainActivity.galaxy[toX][y].getOwnerId() == ownerId) {
					to.add(MainActivity.galaxy[toX][y].coordinates);
				}
			}
		}
		if (fromY >= 0) {
			recurse = true;
			for (int x = (int) Tools.max(fromX + 1, 0); x <= Tools.min(toX - 1, MainActivity.galaxySize - 1); x++) {
				if (MainActivity.galaxy[x][fromY].getOwnerId() == ownerId) {
					to.add(MainActivity.galaxy[x][fromY].coordinates);
				}
			}
		}
		if (toY < MainActivity.galaxySize) {
			recurse = true;
			for (int x = (int) Tools.max(fromX + 1, 0); x <= Tools.min(toX - 1, MainActivity.galaxySize - 1); x++) {
				if (MainActivity.galaxy[x][toY].getOwnerId() == ownerId) {
					to.add(MainActivity.galaxy[x][toY].coordinates);
				}
			}
		}
		if (to.size() == 0 && recurse) {
			recursiveSeekFor(ownerId, distance + 1, to);
		}
	}
	
	
	public double getPowerBy(byte ownerId) {
		i("getting power by " + ownerId  + "...");
		double result = 0;
		Coordinates sectorCoordinates = Sector.sectorOf(this);
		int fromX = (int) Tools.max(sectorCoordinates.x - 1, 0);
		int toX = (int) Tools.min(sectorCoordinates.x + 1, Sectors.galaxy.length - 1);
		int fromY = (int) Tools.max(sectorCoordinates.y - 1, 0);
		int toY = (int) Tools.min(sectorCoordinates.y + 1, Sectors.galaxy.length - 1);
		i("fromX = " + fromX);
		i("toX = " + toX);
		i("fromY = " + fromY);
		i("toY = " + toY);
		for (int x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				Sector sector = Sectors.galaxy[x][y];
				for (Star[] stars: sector) {
					for (Star star: stars) {
						if (star.getShipsOnOrbitOwnerId() == ownerId) {
							for (Ship ship: star.orbit) {
								if (ship.base != Ship.BASE_FREIGHTER) {
									double danger = ship.danger();
									result += danger;
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	public double getEnemyPowerFor(byte ownerId) {
		double result = 0;
		Coordinates sectorCoordinates = Sector.sectorOf(this);
		Sector sector = Sectors.galaxy[sectorCoordinates.x][sectorCoordinates.y];
		for (Star[] stars : sector) {
			for (Star star : stars) {
				if (star.getShipsOnOrbitOwnerId() != ownerId) {
					for (Ship ship : star.orbit) {
						if (ship.base != Ship.BASE_FREIGHTER) {
							double danger = ship.danger();
							result += danger;
						}
					}
				}
			}
		}
		return result;
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
