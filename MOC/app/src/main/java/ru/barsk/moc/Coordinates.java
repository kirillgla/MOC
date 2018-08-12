package ru.barsk.moc;

public final class Coordinates {

    public byte x;
    public byte y;

    public Coordinates(byte x, byte y) {
        this.x = x;
        this.y = y;
    }

	@Override
	public boolean equals(Object other) {
		if (other instanceof Coordinates) {
			Coordinates otherCoordinates = (Coordinates) other;
			return (this.x == otherCoordinates.x) && (this.y == otherCoordinates.y);
		}
		return false;
	}

	@Override
    public String toString() {
        return "(" + x + "; " + y + ")";
    }

	public Coordinates copy() {
		return new Coordinates(this.x, this.y);
	}

}
