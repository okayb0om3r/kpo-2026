package ru.hse.vyshkat.model;

public final class ElectricScooter extends Transport implements EnergyConsumer {
    private final double dailyEnergyKwh;

    public ElectricScooter(String number, String name, int ease, boolean operational, double energy) {
        super(number, name, ease, operational);
        dailyEnergyKwh = requireEnergy(energy);
    }

    @Override
    public double dailyEnergyKwh() {
        return dailyEnergyKwh;
    }
}
