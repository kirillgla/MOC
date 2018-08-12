package ru.barsk.util;

import ru.barsk.moc.MainActivity;
import ru.barsk.moc.Player;

public class LongDouble {

	private long longValue;
	private double doubleValue;

	public LongDouble() {
		this(0, 0);
	}

	public LongDouble(long longValue) {
		this(longValue, 0);
	}

	public LongDouble(double doubleValue) {
		this(0, doubleValue);
	}

	public LongDouble(long longValue, double doubleValue) {
		this.longValue = longValue;
		if (doubleValue >= 1) {
			int temp = (int) doubleValue;
			doubleValue -= temp;
			this.longValue += temp;
		}
		this.doubleValue = doubleValue;
	}

	public long round() {
		if (doubleValue >= 0.5) {
			return longValue + 1;
		}
		return longValue;
	}

	public double doubleValue() {
		return (double) longValue + doubleValue;
	}

	public long longValue() {
		return longValue;
	}

	public void inc() {
		longValue++;
	}

	public void clear() {
		longValue = 0;
		doubleValue = 0;
	}

	public void add(double value) {
		doubleValue += value;
		int temp = (int) doubleValue;
		doubleValue -= temp;
		longValue += temp;
	}

	@Override
	public String toString() {
		String ofLong = String.valueOf(longValue);
		StringBuilder ofDouble = new StringBuilder(String.valueOf(doubleValue));
		ofDouble.deleteCharAt(0);
		return ofLong + ofDouble.toString();
	}

	public static LongDouble parseLongDouble(String arg) {
		long longValue = 0;
		double doubleValue = 0;
		int i;
		for (i = 0; i < arg.length(); i++) {
			char current = arg.charAt(i);
			if (current == '.' || current == ',') {
				break;
			}
			longValue *= 10;
			longValue += asByte(current);
		}
		StringBuilder doubleValueBuilder = new StringBuilder(arg);
		for (int j = 0; j < i; j++) {
			doubleValueBuilder.deleteCharAt(0);
		}
		doubleValueBuilder.insert(0, '0');
		doubleValue = Double.parseDouble(doubleValueBuilder.toString());
		return new LongDouble(longValue, doubleValue);
	}

	public static byte asByte(char c) {
		switch (c) {
			case '0': return 0;
			case '1': return 1;
			case '2': return 2;
			case '3': return 3;
			case '4': return 4;
			case '5': return 5;
			case '6': return 6;
			case '7': return 7;
			case '8': return 8;
			case '9': return 9;
			default: throw new NumberFormatException();
		}
	}

	public int ITLevel() {
		int i = 1;
		while (getItFor(i) <= round()) {
			i++;
		}
		return i - 1;
	}

	public int toNextLevel() {
		int level = ITLevel();
		int nextLevel = level + 1;
		long necessaryIt = getItFor(nextLevel);
		return (int) (necessaryIt - round());
	}

	public static long getItFor(int level) {
		return (200 + 100 * (level - 1)) * level / 2;
	}

	public static double getProductionModifier(byte ownerId) {
		if(ownerId == 0) {
			return 0;
		}
		else {
			if (ownerId == Player.ID) {
				return 1 + 0.075 * Player.IT.ITLevel();
			}
			else {
				return 1 + 0.075 * MainActivity.AIs[ownerId - 2].IT.ITLevel();
			}
		}
	}

	public static double getAttackModifier(byte ownerId) {
		if (ownerId == 0) {
			return 0;
		}
		else {
			if (ownerId == Player.ID) {
				return 1 + 0.1 * Player.IT.ITLevel();
			}
			else {
				return 1 + 0.1 * MainActivity.AIs[ownerId - 2].IT.ITLevel();
			}
		}
	}

	public static double getHpModifier(byte ownerId) {
		if (ownerId == 0) {
			return 0;
		}
		else {
			if (ownerId == Player.ID) {
				return 1 + 0.1 * Player.IT.ITLevel();
			}
			else {
				return 1 + 0.1 * MainActivity.AIs[ownerId - 2].IT.ITLevel();
			}
		}
	}

}
