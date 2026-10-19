package ru.hse.vyshkat.model;

/** Общие данные объектов на балансе. Сотрудники не являются Asset. */
public abstract class Asset implements InventoryItem {
    private final String inventoryNumber;
    private final String name;

    protected Asset(String inventoryNumber, String name) {
        this.inventoryNumber = requireText(inventoryNumber, "Инвентарный номер");
        this.name = requireText(name, "Название");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не может быть пустым");
        }
        return value.strip();
    }

    protected static double requireEnergy(double energy) {
        if (!Double.isFinite(energy) || energy < 0) {
            throw new IllegalArgumentException("Расход энергии должен быть конечным и неотрицательным");
        }
        return energy;
    }

    @Override
    public final String inventoryNumber() {
        return inventoryNumber;
    }

    @Override
    public final String name() {
        return name;
    }
}
