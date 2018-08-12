package ru.barsk.moc;

public /*final*/ abstract class Settings {
	
	private static byte volume = 100;
	public static boolean doVibrate = true;
	public static boolean doShowDamage = false;
	public static boolean doShowTurnEvents = true;
	private static byte combatLength = 1;
	
	public static byte getVolume() {
		return volume;
	}

	public static byte getCombatLength() {
		return combatLength;
	}

	public static void setVolume(int value) {
		if (value > 100) {
			volume = 100;
		}
		else {
			if (volume < 0) {
				volume = 0;
			}
			else {
				volume = (byte) value;
			}
		}
	}

	public static void setVolume(String value) {
		int newVolume;
        try {
            newVolume = Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            newVolume = (int) Double.parseDouble(value);
        }
        setVolume(newVolume);
	}

	public static void setCombatLength(byte value) {
		if (value >= 3) {
			combatLength = 3;
		}
		else {
			if (value <= 1) {
				combatLength = 1;
			}
			else {
				combatLength = 2;
			}
		}
	}

}
