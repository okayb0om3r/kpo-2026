package ru.hse.vyshkat.model;

public final class ChargingStation extends Thing implements EnergyConsumer {
    private final double dailyEnergyKwh;

    public ChargingStation(String number, String name, double energy) {
        super(number, name);
        dailyEnergyKwh = requireEnergy(energy);
    }

    @Override
    public double dailyEnergyKwh() {
        return dailyEnergyKwh;
    }
}
