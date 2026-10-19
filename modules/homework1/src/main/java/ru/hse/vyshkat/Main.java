package ru.hse.vyshkat;

import java.util.Scanner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hse.vyshkat.config.AppConfig;
import ru.hse.vyshkat.console.ConsoleApp;
import ru.hse.vyshkat.service.FleetService;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            var app = new ConsoleApp(context.getBean(FleetService.class), new Scanner(System.in), System.out);
            if (args.length == 1 && "--demo".equals(args[0])) {
                app.demo();
            } else if (args.length == 0) {
                app.run();
            } else {
                System.out.println("Запуск: без аргументов (меню) или --demo (демонстрация)");
            }
        }
    }
}
