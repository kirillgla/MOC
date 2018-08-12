package ru.barsk.moc;

public class AttackedStar {
	
	public Coordinates coordinates;
	public double attackPower;
	
	public AttackedStar(byte x, byte y, double attackPower) {
		this(new Coordinates(x, y), attackPower);
	}
	
	public AttackedStar(Coordinates coordinates, double attackPower) {
		this.coordinates = coordinates;
		this.attackPower = attackPower;
	}
	
	public AttackedStar(Star star) {
		coordinates = star.coordinates;
		attackPower = star.getAttackPower() - star.getDefencePower();
	}

	@Override
	public String toString() {
		return coordinates.toString() + ":" + attackPower;
	}
	
}
