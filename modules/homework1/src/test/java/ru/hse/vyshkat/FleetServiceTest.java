package ru.hse.vyshkat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.vyshkat.model.Bicycle;
import ru.hse.vyshkat.model.ChargingStation;
import ru.hse.vyshkat.model.ElectricBicycle;
import ru.hse.vyshkat.model.ElectricScooter;
import ru.hse.vyshkat.model.Helmet;
import ru.hse.vyshkat.model.Transport;
import ru.hse.vyshkat.service.BasicServiceCenter;
import ru.hse.vyshkat.service.FleetService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetServiceTest {
    @Test
    void emptyFleetHasEmptyReports() {
        var fleet = new FleetService(new BasicServiceCenter());
        assertEquals(0, fleet.transportCount());
        assertEquals(0, fleet.dailyEnergyKwh());
        assertTrue(fleet.inventory().isEmpty());
        assertTrue(fleet.beginnerTransport().isEmpty());
    }

    @Test
    void inspectionHappensBeforeAcceptanceAndRejectionDoesNotReserveNumber() {
        var calls = new AtomicInteger();
        var fleet = new FleetService(transport -> calls.incrementAndGet() > 1);
        var scooter = new ElectricScooter("S1", "Самокат", 8, true, 2);
        assertFalse(fleet.admit(scooter));
        assertTrue(fleet.inventory().isEmpty());
        assertEquals(0, fleet.dailyEnergyKwh());
        assertTrue(fleet.beginnerTransport().isEmpty());
        assertTrue(fleet.admit(scooter));
        assertEquals(2, calls.get());
        assertSame(scooter, fleet.inventory().get(0));
    }

    @Test
    void reportIncludesEnergyOfTransportAndThingsButNotRejectedTransport() {
        var fleet = new FleetService(new BasicServiceCenter());
        fleet.admit(new ElectricScooter("S1", "Самокат", 6, true, 2.5));
        fleet.admit(new Bicycle("B1", "Велосипед", 5, true));
        fleet.admit(new ElectricBicycle("E1", "Электровелосипед", 8, true, 1.5));
        assertFalse(fleet.admit(new ElectricScooter("S2", "Сломан", 10, false, 100)));
        fleet.addThing(new Helmet("H1", "Шлем"));
        fleet.addThing(new ChargingStation("C1", "Станция", 3));
        assertEquals(3, fleet.transportCount());
        assertEquals(5, fleet.inventory().size());
        assertEquals(7, fleet.dailyEnergyKwh(), 0.000001);
        assertEquals(2, fleet.beginnerTransport().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 6, 10})
    void beginnerRuleUsesInclusiveThreshold(int ease) {
        var fleet = new FleetService(transport -> true);
        var bicycle = new Bicycle("B1", "Велосипед", ease, true);
        fleet.admit(bicycle);
        assertEquals(ease >= 6, fleet.beginnerTransport().contains(bicycle));
    }

    @Test
    void duplicateNumbersAreRejectedAcrossBothCategoriesBeforeInspection() {
        var calls = new AtomicInteger();
        var fleet = new FleetService(transport -> {
            calls.incrementAndGet();
            return true;
        });
        fleet.addThing(new Helmet("A1", "Шлем"));
        assertThrows(IllegalArgumentException.class,
                () -> fleet.admit(new Bicycle("A1", "Велосипед", 5, true)));
        assertEquals(0, calls.get());
        assertThrows(IllegalArgumentException.class, () -> fleet.addThing(new Helmet("A1", "Другой шлем")));
        fleet.admit(new Bicycle("B1", "Велосипед", 6, true));
        assertThrows(IllegalArgumentException.class, () -> fleet.addThing(new Helmet("B1", "Шлем")));
        assertEquals(2, fleet.inventory().size());
    }

    @Test
    void returnedListsCannotBypassInspection() {
        var fleet = new FleetService(transport -> true);
        fleet.admit(new Bicycle("B1", "Велосипед", 6, true));
        var snapshot = fleet.inventory();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, () -> fleet.beginnerTransport().clear());
        fleet.addThing(new Helmet("H1", "Шлем"));
        assertEquals(1, snapshot.size());
        assertEquals(2, fleet.inventory().size());
    }

    @Test
    void failedInspectionDoesNotPartiallyAddTransport() {
        var fleet = new FleetService(transport -> {
            throw new IllegalStateException("Техосмотр недоступен");
        });
        assertThrows(IllegalStateException.class, () -> fleet.admit(new Bicycle("B1", "Велосипед", 6, true)));
        assertTrue(fleet.inventory().isEmpty());
    }

    @Test
    void newTransportTypeWorksWithoutChangingFleetService() {
        class Skateboard extends Transport {
            Skateboard() {
                super("SK1", "Скейтборд", 6, true);
            }
        }
        var fleet = new FleetService(new BasicServiceCenter());
        var board = new Skateboard();
        assertTrue(fleet.admit(board));
        assertTrue(fleet.beginnerTransport().contains(board));
        assertEquals(1, fleet.transportCount());
    }

    @Test
    void nullDependenciesAndItemsAreRejected() {
        assertThrows(NullPointerException.class, () -> new FleetService(null));
        var fleet = new FleetService(transport -> true);
        assertThrows(NullPointerException.class, () -> fleet.admit(null));
        assertThrows(NullPointerException.class, () -> fleet.addThing(null));
    }
}
