package ru.barsk.moc;

public final class ShipTemplate extends ArtificialConstruction<Ship> {

    public final int maxHp;
    public final byte base;
    public final byte attack;
    public final byte speed;

    public ShipTemplate(double productionCost, String name, byte base, byte attack, byte speed, int maxHp) {
		super(productionCost, name);
        if (maxHp <= 1 ) {
            this.maxHp = 1;
        }
        else {
            this.maxHp = maxHp;
        }
        if ((base < 0) || (base > 5)) {
            this.base = 0;
        }
        else {
            this.base = base;
        }
        if (attack <= 0) {
            this.attack = 0;
        }
        else {
            this.attack = attack;
        }
        if (speed < 0) {
            this.speed = 0;
        }
        else {
            this.speed = speed;
        }
    }

	public double danger() {
		return this.attack * this.maxHp;
	}

	@Override
    public Ship construct(byte ownerID, Coordinates position) {
        return new Ship(ownerID, this.name, this.base, this.attack, this.speed, this.maxHp, position);
    }

	public ShipTemplate(double initialProductionCost, double productionCost, String name, byte base, byte attack, byte speed, byte maxHp) {
		super(initialProductionCost, productionCost, name);
		this.maxHp = maxHp;
		this.base = base;
		this.attack = attack;
		this.speed = speed;
	}

}
