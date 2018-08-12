package ru.barsk.moc;

import android.support.annotation.Nullable;

import ru.barsk.util.LongDouble;

/**
 * Я создал этот класс для того, чтобы была возможность
 * написать новый класс-наследник AbstractAI
 * и использовать его вместо AI путём изменения всего 1 строчки
 * инициализации MainActivity.AIs.
 */
public abstract class AbstractAI {

	public double treasury;
	public double income;
	public LongDouble IT;
    public final byte ID;
	public final byte colorId;
	public final String name;

    public void manageGalaxy(Star[][] galaxy) {
		for (Star[] stars: galaxy) {
			for (Star star: stars) {
				if (star.getOwnerId() == this.ID) {
					manageStar(star);
				}
			}
		}
		manageShips(galaxy);
		manageScience();
	}

    public abstract void manageStar(Star star);
	
	public abstract void manageShips(Star[][] galaxy);

	public abstract void manageScience();

	public AbstractAI(byte ID, byte colorId, String name) {
		this(ID, colorId, 100, name, null);
	}

	public AbstractAI(byte ID, byte colorId, double treasury, String name, @Nullable LongDouble IT) {
		this.ID = ID;
		this.colorId = colorId;
		this.treasury = treasury;
		this.name = name;
		if (IT == null) {
			this.IT = new LongDouble();
		}
		else {
			this.IT = IT;
		}
	}

	public static class AIPermissionException extends RuntimeException {
		/**
		 * Thrown when children of ru.barsk.moc.AbstractAI
		 * try to do something illegally.
		 */
		public AIPermissionException() {
		}
		public AIPermissionException(String detailMessage) {
			super(detailMessage);
		}
	}

}
