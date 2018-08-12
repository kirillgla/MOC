package ru.barsk.moc;

import android.util.Log;

public final class ColonyManager {
	
	public static final String LOG_TAG = "ColonyManager";

    public double farmersPercentage;
    public double workersPercentage;
    public double scientistsPercentage;
    public double populationGrowthSpeed;
    public boolean farmersEnabled = false;
    public boolean workersEnabled = false;
    public boolean scientistsEnabled = false;
	public float tax; //should be from 0 to 1

    public void setPercentage(double farmersPercentage, double workersPercentage, double scientistsPercentage) {
	//	i("setPercentage(" + farmersPercentage + ", " + workersPercentage + ", " + scientistsPercentage + ")");
        double sum = farmersPercentage + workersPercentage + scientistsPercentage;
        if (sum > 1.01 || sum < 0.99) {
            setParts(farmersPercentage, workersPercentage, scientistsPercentage);
        }
        else {
            this.farmersPercentage = farmersPercentage;
            this.workersPercentage = workersPercentage;
            this.scientistsPercentage = scientistsPercentage;
        }
    }

    public void setParts(double farmersPart, double workersPart, double scientistsPart) {
	//	i("setParts(" + farmersPercentage + ", " + workersPercentage + ", " + scientistsPercentage + ")");
        double sum = farmersPart + workersPart + scientistsPart;
        double newScientistPercentage;
        double newWorkerPercentage;
        double newFarmerPercentage;
        if (sum != 0) {
            newFarmerPercentage = farmersPart / sum;
            newWorkerPercentage = workersPart / sum;
            newScientistPercentage = scientistsPart / sum;
        }
        else {
            newFarmerPercentage = 1 / 3.;
            newWorkerPercentage = 1 / 3.;
            newScientistPercentage = 1 / 3.;
        }
        this.setPercentage(newFarmerPercentage, newWorkerPercentage, newScientistPercentage);
    }

    public ColonyManager() {
        this(1 / 3., 1 / 3., 1 / 3., 0.05, 0.1f);
    }

    public ColonyManager(double farmers, double workers, double scientists, double populationGrowthSpeed, float tax) {
        this.setPercentage(farmers, workers, scientists);
        this.populationGrowthSpeed = populationGrowthSpeed;
		this.tax = tax;
    }
	
	public static void i(String arg) {
		Log.i(LOG_TAG, arg);
	}

}
