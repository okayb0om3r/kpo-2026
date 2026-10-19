package ru.hse.vyshkat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.vyshkat.console.ConsoleApp;
import ru.hse.vyshkat.service.BasicServiceCenter;
import ru.hse.vyshkat.service.FleetService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleAppTest {
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final FleetService fleet = new FleetService(new BasicServiceCenter());

    private ConsoleApp app(String input) {
        return new ConsoleApp(fleet, new Scanner(input), new PrintStream(bytes, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void demonstrationShowsAcceptanceRejectionReportsAndDuplicate() {
        app("").demo();
        assertTrue(output().contains("Список пуст"));
        assertTrue(output().contains("Самокат: принят в парк"));
        assertTrue(output().contains("Неисправный самокат: техосмотр отклонён"));
        assertTrue(output().contains("Инвентарный номер уже занят: S-001"));
        assertTrue(output().contains("Транспорт: 3; вещи: 2; всего: 5"));
        assertTrue(output().contains("Суточное потребление: 7.00 кВт·ч"));
        String beginnerSection = output().split("Подходит новичкам:")[1].split("Весь инвентарь:")[0];
        assertTrue(beginnerSection.contains("S-001 | Самокат"));
        assertTrue(beginnerSection.contains("E-001 | Электровелосипед"));
        assertEquals(2, beginnerSection.lines().filter(line -> line.contains(" | ")).count());
    }

    @Test
    void menuAcceptsEveryTypeAndPrintsReports() {
        app(String.join("\n", "1", "1", "S1", "Самокат", "6", "да", "2,5",
                "1", "2", "B1", "Велосипед", "5", "да",
                "1", "3", "E1", "Электровелосипед", "8", "да", "1.5",
                "1", "2", "B2", "Сломан", "9", "нет",
                "2", "1", "H1", "Шлем", "2", "2", "C1", "Станция", "3",
                "3", "4", "5", "0")).run();
        assertEquals(5, fleet.inventory().size());
        assertEquals(7, fleet.dailyEnergyKwh());
        assertTrue(output().contains("Транспорт: 3; вещи: 2; всего: 5"));
        assertTrue(output().contains("Сломан: техосмотр отклонён"));
        assertTrue(output().contains("H1 | Шлем"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1\n7\n0", "2\n7\n0", "1\n2\nB1\nВелосипед\nabc\n0",
        "1\n2\nB1\nВелосипед\n6\nmaybe\n0", "2\n2\nC1\nСтанция\nabc\n0",
        "1\n2\nB1\nВелосипед\n11\nда\n0", "2\n2\nC1\nСтанция\n-1\n0"
    })
    void invalidInputDoesNotChangeInventoryAndReturnsToMenu(String input) {
        app(input).run();
        assertTrue(output().contains("Ошибка:"));
        assertTrue(fleet.inventory().isEmpty());
    }

    @Test
    void unknownCommandAndEmptyReportsAreHandled() {
        app("oops\n3\n4\n5\n0").run();
        assertTrue(output().contains("Неизвестная команда"));
        assertTrue(output().contains("Список пуст"));
        assertTrue(output().contains("Суточное потребление: 0.00"));
    }

    @Test
    void endOfInputInFormExitsWithoutPartialAddition() {
        app("1\n2\nB1\n").run();
        assertTrue(output().contains("Ввод завершён"));
        assertTrue(fleet.inventory().isEmpty());
    }

    @Test
    void endOfInputAtMenuExits() {
        app("").run();
        assertTrue(fleet.inventory().isEmpty());
    }
}
