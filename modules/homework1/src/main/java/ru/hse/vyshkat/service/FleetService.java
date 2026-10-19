package ru.hse.vyshkat.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.hse.vyshkat.model.EnergyConsumer;
import ru.hse.vyshkat.model.InventoryItem;
import ru.hse.vyshkat.model.Thing;
import ru.hse.vyshkat.model.Transport;

/** Учёт в памяти. Изменение парка доступно только через операции приёмки. */
public final class FleetService {
    private static final int BEGINNER_THRESHOLD = 6;
    private final ServiceCenter serviceCenter;
    private final Map<String, InventoryItem> inventory = new LinkedHashMap<>();

    public FleetService(ServiceCenter serviceCenter) {
        this.serviceCenter = Objects.requireNonNull(serviceCenter);
    }

    public boolean admit(Transport transport) {
        requireUnique(transport);
        if (!serviceCenter.approve(transport)) {
            return false;
        }
        inventory.put(transport.inventoryNumber(), transport);
        return true;
    }

    public void addThing(Thing thing) {
        requireUnique(thing);
        inventory.put(thing.inventoryNumber(), thing);
    }

    private void requireUnique(InventoryItem item) {
        Objects.requireNonNull(item, "Объект не может быть null");
        if (inventory.containsKey(item.inventoryNumber())) {
            throw new IllegalArgumentException("Инвентарный номер уже занят: " + item.inventoryNumber());
        }
    }

    public List<InventoryItem> inventory() {
        return List.copyOf(inventory.values());
    }

    public long transportCount() {
        return inventory.values().stream().filter(Transport.class::isInstance).count();
    }

    public double dailyEnergyKwh() {
        return inventory.values().stream()
                .filter(EnergyConsumer.class::isInstance)
                .map(EnergyConsumer.class::cast)
                .mapToDouble(EnergyConsumer::dailyEnergyKwh)
                .sum();
    }

    public List<Transport> beginnerTransport() {
        return inventory.values().stream()
                .filter(Transport.class::isInstance)
                .map(Transport.class::cast)
                .filter(transport -> transport.beginnerEase() >= BEGINNER_THRESHOLD)
                .toList();
    }
}
