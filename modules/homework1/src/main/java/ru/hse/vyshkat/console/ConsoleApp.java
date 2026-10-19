package ru.hse.vyshkat.console;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import ru.hse.vyshkat.model.Bicycle;
import ru.hse.vyshkat.model.ChargingStation;
import ru.hse.vyshkat.model.ElectricBicycle;
import ru.hse.vyshkat.model.ElectricScooter;
import ru.hse.vyshkat.model.Helmet;
import ru.hse.vyshkat.model.InventoryItem;
import ru.hse.vyshkat.model.Transport;
import ru.hse.vyshkat.service.FleetService;

public final class ConsoleApp {
    private final FleetService fleet;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleApp(FleetService fleet, Scanner input, PrintStream output) {
        this.fleet = fleet;
        this.input = input;
        this.output = output;
    }

    public void run() {
        output.println("ВышКат — учёт парка. Данные хранятся до завершения программы.");
        while (true) {
            output.println("1 — принять транспорт; 2 — добавить вещь; 3 — отчёт;");
            output.println("4 — транспорт для новичков; 5 — весь инвентарь; 0 — выход");
            if (!input.hasNextLine()) {
                return;
            }
            String command = input.nextLine().strip();
            try {
                switch (command) {
                    case "1" -> addTransport();
                    case "2" -> addThing();
                    case "3" -> report();
                    case "4" -> printItems(fleet.beginnerTransport());
                    case "5" -> printItems(fleet.inventory());
                    case "0" -> {
                        return;
                    }
                    default -> output.println("Неизвестная команда");
                }
            } catch (IllegalArgumentException exception) {
                output.println("Ошибка: " + exception.getMessage());
            } catch (IllegalStateException exception) {
                output.println("Ввод завершён. До свидания!");
                return;
            }
        }
    }

    private String read(String prompt) {
        output.println(prompt);
        if (!input.hasNextLine()) {
            throw new IllegalStateException("Конец ввода");
        }
        return input.nextLine().strip();
    }

    private int readInt(String prompt) {
        try {
            return Integer.parseInt(read(prompt));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Ожидалось целое число");
        }
    }

    private double readEnergy() {
        try {
            return Double.parseDouble(read("Суточный расход, кВт·ч:").replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Ожидалось число для расхода энергии");
        }
    }

    private void addTransport() {
        String type = read("Тип: 1 — электросамокат, 2 — велосипед, 3 — электровелосипед");
        if (!List.of("1", "2", "3").contains(type)) {
            throw new IllegalArgumentException("Неизвестный тип транспорта");
        }
        String number = read("Инвентарный номер:");
        String name = read("Название:");
        int ease = readInt("Простота управления (1–10):");
        String condition = read("По результатам проверки исправен? да/нет");
        if (!condition.equalsIgnoreCase("да") && !condition.equalsIgnoreCase("нет")) {
            throw new IllegalArgumentException("Введите да или нет");
        }
        boolean operational = condition.equalsIgnoreCase("да");
        Transport transport = switch (type) {
            case "1" -> new ElectricScooter(number, name, ease, operational, readEnergy());
            case "2" -> new Bicycle(number, name, ease, operational);
            default -> new ElectricBicycle(number, name, ease, operational, readEnergy());
        };
        printAdmission(transport);
    }

    private void addThing() {
        String type = read("Тип: 1 — шлем, 2 — зарядная станция");
        if (!List.of("1", "2").contains(type)) {
            throw new IllegalArgumentException("Неизвестный тип вещи");
        }
        String number = read("Инвентарный номер:");
        String name = read("Название:");
        if (type.equals("1")) {
            fleet.addThing(new Helmet(number, name));
        } else {
            fleet.addThing(new ChargingStation(number, name, readEnergy()));
        }
        output.println("Вещь добавлена на баланс");
    }

    private void printAdmission(Transport transport) {
        boolean accepted = fleet.admit(transport);
        output.println(transport.name() + (accepted ? ": принят в парк" : ": техосмотр отклонён"));
    }

    private void report() {
        output.printf(Locale.ROOT, "Транспорт: %d; вещи: %d; всего: %d%n",
                fleet.transportCount(), fleet.inventory().size() - fleet.transportCount(), fleet.inventory().size());
        output.printf(Locale.ROOT, "Суточное потребление: %.2f кВт·ч%n", fleet.dailyEnergyKwh());
    }

    private void printItems(List<? extends InventoryItem> items) {
        if (items.isEmpty()) {
            output.println("Список пуст");
        }
        for (InventoryItem item : items) {
            output.println(item.inventoryNumber() + " | " + item.name());
        }
    }

    /** Воспроизводимый сценарий для нового, пустого парка. */
    public void demo() {
        output.println("ВышКат — демонстрация");
        report();
        printItems(fleet.inventory());
        printAdmission(new ElectricScooter("S-001", "Самокат", 6, true, 2.5));
        printAdmission(new Bicycle("B-001", "Велосипед", 5, true));
        printAdmission(new ElectricBicycle("E-001", "Электровелосипед", 8, true, 1.5));
        printAdmission(new ElectricScooter("S-002", "Неисправный самокат", 9, false, 10));
        fleet.addThing(new Helmet("H-001", "Шлем"));
        fleet.addThing(new ChargingStation("C-001", "Зарядная станция", 3));
        try {
            fleet.addThing(new Helmet("S-001", "Дубликат номера"));
        } catch (IllegalArgumentException exception) {
            output.println("Ошибка: " + exception.getMessage());
        }
        report();
        output.println("Подходит новичкам:");
        printItems(fleet.beginnerTransport());
        output.println("Весь инвентарь:");
        printItems(fleet.inventory());
    }
}
