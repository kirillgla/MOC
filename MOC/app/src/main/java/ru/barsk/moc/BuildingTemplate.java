package ru.barsk.moc;

public class BuildingTemplate extends ArtificialConstruction<Building> {

	public final int typeId;
	public final int level;

	public BuildingTemplate(double productionCost, int typeId, int level, String name) {
		super(productionCost, name);
		this.typeId = typeId;
		this.level = level;
	}

	public BuildingTemplate(double initialProductionCost, double productionCost, int typeId, int level, String name) {
		super(initialProductionCost, productionCost, name);
		this.typeId = typeId;
		this.level = level;
	}

	@Override
	public Building construct(byte ownerId, Coordinates position) {
		return Building.makeBuilding(typeId, level);
	}

}
