package ru.barsk.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import ru.barsk.moc.Building;
import ru.barsk.moc.Coordinates;
import ru.barsk.moc.MainActivity;
import ru.barsk.moc.Player;
import ru.barsk.moc.R;
import ru.barsk.moc.Ship;
import ru.barsk.moc.Star;
import ru.barsk.moc.TurnEvent;
import android.util.Log;

public /*final*/ abstract class Tools {

	public static final String LOG_TAG = "Tools";

	public static void analyze(Context context, Exception e) {
		Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		StackTraceElement[] elements = e.getStackTrace();
		for (StackTraceElement element: elements) {
			Toast.makeText(context, element.toString(), Toast.LENGTH_LONG).show();
		}
	}
	
	public static Ship[][] adaptArray(ArrayList<Ship> args) {
		final int inARow = 4;
		int size = args.size();
		int arraySize = size / inARow;
		if (arraySize * inARow != size) {
			arraySize++;
		}
		Ship[][] result = new Ship[arraySize][inARow];
		for (int i = 0; i < arraySize; i++) {
			for (int j = 0; j < inARow; j++) {
				int index = i * inARow + j;
				if (index < size){
					result[i][j] = args.get(index);
				}
				else {
					result[i][j] = null;
				}
			}
		}
		return result;
    }
	
	public static double round(double inputDouble, byte pow) {
		double result;
		result = inputDouble;
		result = result * Math.pow(10, pow);
		result = Math.round(result);
		result = result / Math.pow(10, pow);
		return result;
    }

	public static void notifyStarDead(Star star) {
		if (star.getOwnerId() == Player.ID) {
			StringBuilder message = new StringBuilder();
			message.append(MainActivity.context.getString(R.string.star_population));
			message.append(" ");
			message.append(MainActivity.context.getString(R.string.of_your));
			message.append(" ");
			message.append(MainActivity.context.getString(R.string.colony));
			message.append(" (");
			message.append(star.name);
			message.append(") ");
			message.append(MainActivity.context.getString(R.string.colony_died_out));
			TurnEvent event = new TurnEvent(TurnEvent.Tag.COLONY_DIED, star, message.toString());
			MainActivity.events.add(event);
		}
	}

	public static void notifyPopulationDecay(Star star) {
		if (star.getOwnerId() == Player.ID) {
			StringBuilder message = new StringBuilder();
			message.append(MainActivity.context.getString(R.string.star_population));
			message.append(" ");
			message.append(MainActivity.context.getString(R.string.of_your));
			message.append(" ");
			message.append(MainActivity.context.getString(R.string.of_colony));
			message.append(" (");
			message.append(star.name);
			message.append(") ");
			message.append(MainActivity.context.getString(R.string.colony_population_decreased));
			TurnEvent event = new TurnEvent(TurnEvent.Tag.POPULATION_DIE_OUT, star, message.toString());
			MainActivity.events.add(event);
		}
	}

	public static void notifyEconomyCollapsing() {
		String message = MainActivity.context.getString(R.string.economy_collapsing);
		TurnEvent event = new TurnEvent(TurnEvent.Tag.ECONOMY_WARNING, null, message);
		MainActivity.events.add(event);
	}

	public static void notifyShipAbandoned(Ship ship) {
		Toast.makeText(MainActivity.context, "called notifyShipAbandoned()!", Toast.LENGTH_SHORT).show();
		StringBuilder message = new StringBuilder();
		message.append(MainActivity.context.getString(R.string.abandoned));
		message.append(' ');
		message.append(MainActivity.context.getString(R.string.ship));
		message.append(" (");
		message.append(ship.name);
		message.append(") ");
		message.append(MainActivity.context.getString(R.string.because_of_economy));
		TurnEvent event = new TurnEvent(TurnEvent.Tag.ECONOMY_WARNING, MainActivity.galaxy[ship.position.x][ship.position.y], message.toString());
		MainActivity.events.add(event);
	}

	public static void notifyBuildingAbandoned(Building building, Coordinates coordinates) {
		Toast.makeText(MainActivity.context, "called notifyBuildingAbandoned()!", Toast.LENGTH_SHORT).show();
		StringBuilder message = new StringBuilder();
		message.append(MainActivity.context.getString(R.string.abandoned));
		message.append(' ');
		message.append(MainActivity.context.getString(R.string.construction));
		message.append(" (");
		message.append(building.getName());
		message.append(") ");
		message.append(MainActivity.context.getString(R.string.because_of_economy));
		TurnEvent event = new TurnEvent(TurnEvent.Tag.ECONOMY_WARNING, MainActivity.galaxy[coordinates.x][coordinates.y], message.toString());
		MainActivity.events.add(event);
	}

	@Deprecated
	public static void notifyGreatPopulation(Star star) {
		StringBuilder message = new StringBuilder();
		message.append(MainActivity.context.getString(R.string.star_population));
		message.append(" ");
		message.append(MainActivity.context.getString(R.string.of_your));
		message.append(" ");
		message.append(MainActivity.context.getString(R.string.colony));
		message.append(" (");
		message.append(star.name);
		message.append(") ");
		message.append(MainActivity.context.getString(R.string.has_reached_billion));
		TurnEvent event = new TurnEvent(TurnEvent.Tag.COLONY_GROWN, star, message.toString());
		MainActivity.events.add(event);
	}

	public static double round (double inputDouble) {
		return round(inputDouble, (byte) 1);
	}
	
	public static void setColoredValue(double value, TextView link, boolean withPlus, boolean money) {
		StringBuilder newText = new StringBuilder("");
		if (value > 0) {
			if (withPlus) {
				newText.append("+");
			}
			link.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.text_color_good));
		}
		else {
			if (value < 0) {
				link.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.text_color_bad));
			}
			else {
				link.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.text_color_base));
			}
		}
		newText.append(value);
		if (money) {
			newText.append("$");
		}
		link.setText(newText);
    }

	public static double min(@NonNull double... args) {
		double result = args[0];
		for (double d: args) {
			if (d < result) {
				result = d;
			}
		}
		return result;
	}

	public static double max(@NonNull double... args) {
		double result = args[0];
		for (double d: args) {
			if (d > result) {
				result = d;
			}
		}
		return result;
	}
	
	public static Star[][] adaptGalaxy(ArrayList<Star> src, int diameter) {
		i("adpting galaxy...");
		i("diameter = " + diameter);
		i("number of stars = " + src.size());
		Star[][] result = new Star[diameter][diameter];
		for (Star star: src) {
			i("adding " + star.coordinates + "...");
			result[star.coordinates.x][star.coordinates.y] = star;
		}
		return  result;
	}

	public static void removeSomething(Star[][] galaxy, byte ownerId) {
		Random r = new Random();
		boolean gonnaRemoveBuilding = r.nextBoolean() && r.nextBoolean();
		if (gonnaRemoveBuilding) {
			boolean resultOfRemovingBuilding = removeRandomBuilding(galaxy, ownerId);
			if (!resultOfRemovingBuilding) {
				removeRandomShip(galaxy, ownerId);
			}
		}
		else {
			boolean resultOfRemovingShip = removeRandomShip(galaxy, ownerId);
			if (!resultOfRemovingShip) {
				removeRandomBuilding(galaxy, ownerId);
			}
		}
	}

	public static boolean removeRandomBuilding(Star[][] galaxy, byte ownerId) {
		ArrayList<Star> src = new ArrayList<>();
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.type != 0) {
					if (star.getOwnerId() == ownerId) {
						if (star.buildings.size() != 0) {
							src.add(star);
						}
					}
				}
			}
		}
		if (src.isEmpty()) {
			return false;
		}
		int index = (int) (Math.random() * src.size());
		Star star = src.get(index);
		star.buildings.removeRandom(star.coordinates);
		return true;
	}

	public static boolean removeRandomShip(Star[][] galaxy, byte ownerId) {
		ArrayList<ArrayList<Ship>> orbits = new ArrayList<>();
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getShipsOnOrbitOwnerId() == ownerId) {
					orbits.add(star.orbit);
				}
			}
		}
		if (orbits.isEmpty()) {
			return false;
		}
		int index = (int) (Math.random() * orbits.size());
		remove(orbits.get(index));
		return true;
	}

	public static void remove(ArrayList<Ship> orbit) {
		int index = (int) (Math.random() * orbit.size());
		notifyShipAbandoned(orbit.get(index));
		orbit.remove(index);
	}

	public static int roundTurns(double input) {
		int integerPart = (int) input;
		double fractionalPart = input - integerPart;
		if (fractionalPart == 0) {
			return integerPart;
		}
		else {
			return integerPart + 1;
		}
	}

	public static String stringify(Collection src) {
		if (src.size() == 0) {
			return "[]";
		}
		StringBuilder result;
		result = new StringBuilder();
		result.append('[');
		for (Object o: src) {
			result.append(o.toString());
			result.append(',');
			result.append(' ');
		}
		result.deleteCharAt(result.length() - 1);
		result.deleteCharAt(result.length() - 1);
		result.append(']');
		return result.toString();
	}

	public static String stringify(Object... array) {
		if (array.length == 0) {
			return "[]";
		}
		StringBuilder result;
		result = new StringBuilder();
		result.append('[');
		for (Object o: array) {
			result.append(o.toString());
			result.append(',');
			result.append(' ');
		}
		result.deleteCharAt(result.length() - 1);
		result.deleteCharAt(result.length() - 1);
		result.append(']');
		return result.toString();
	}

	public static String stringify(double[][] arg) {
		if (arg.length == 0) {
			return "[]";
		}
		StringBuilder result;
		result = new StringBuilder();
		result.append('[');
		for (double[] doubles: arg) {
			result.append('[');
			for (double d: doubles) {
				result.append((long) d);
				result.append(',');
				result.append(' ');
			}
			result.deleteCharAt(result.length() - 1);
			result.deleteCharAt(result.length() - 1);
			result.append(']');
			result.append(',');
			result.append(' ');
		}
		result.deleteCharAt(result.length() - 1);
		result.deleteCharAt(result.length() - 1);
		result.append(']');
		return result.toString();
	}

	public static double sum(double[][] args) {
		double result = 0;
		for (double[] ds: args) {
			for (double d: ds) {
				result += d;
			}
		}
		return result;
	}
	
	public static void i(String arg) {
		Log.i(LOG_TAG, arg);
	}

}
