package ru.hse.vyshkat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.vyshkat.model.Bicycle;
import ru.hse.vyshkat.model.ChargingStation;
import ru.hse.vyshkat.model.ElectricBicycle;
import ru.hse.vyshkat.model.ElectricScooter;
import ru.hse.vyshkat.model.Helmet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void inventoryNumberAndNameAreRequired(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Helmet(value, "Шлем"));
        assertThrows(IllegalArgumentException.class, () -> new Helmet("H1", value));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 11})
    void easeMustBeBetweenOneAndTen(int ease) {
        assertThrows(IllegalArgumentException.class, () -> new Bicycle("B1", "Велосипед", ease, true));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void energyMustBeFiniteAndNonNegative(double energy) {
        assertThrows(IllegalArgumentException.class, () -> new ChargingStation("C1", "Станция", energy));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricScooter("S1", "Самокат", 6, true, energy));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricBicycle("E1", "Электровелосипед", 6, true, energy));
    }

    @Test
    void textIsTrimmedAndZeroConsumptionIsAllowed() {
        var station = new ChargingStation(" C1 ", " Станция ", 0);
        assertEquals("C1", station.inventoryNumber());
        assertEquals("Станция", station.name());
        assertEquals(0, station.dailyEnergyKwh());
    }
}
