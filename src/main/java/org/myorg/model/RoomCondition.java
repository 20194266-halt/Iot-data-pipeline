package org.myorg.model;
public class RoomCondition {
    private double humidityValue;
    private double tempValue;
    private double aqi;
    private double energyConsumption;

    // Getters
    public double getHumidityValue() {
        return humidityValue;
    }

    public double getTempValue() {
        return tempValue;
    }

    public double getAqi() {
        return aqi;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }

    // Setters
    public void setHumidityValue(double humidityValue) {
        this.humidityValue = humidityValue;
    }

    public void setTempValue(double tempValue) {
        this.tempValue = tempValue;
    }

    public void setAqi(double aqi) {
        this.aqi = aqi;
    }

    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }
}
