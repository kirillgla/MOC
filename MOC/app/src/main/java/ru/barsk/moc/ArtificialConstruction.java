package ru.barsk.moc;

public abstract class ArtificialConstruction<E> {
	public abstract E construct(byte ownerId, Coordinates position);
	public final double initialProductionCost;
	public double productionCost;
	public final String name;
	public ArtificialConstruction(double productionCost, String name) {
		this.initialProductionCost = productionCost;
		this.productionCost = productionCost;
		this.name = name;
	}
	public ArtificialConstruction(double initialProductionCost, double productionCost, String name) {
		this.initialProductionCost = initialProductionCost;
		this.productionCost = productionCost;
		this.name = name;
	}
}
