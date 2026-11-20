package ru.hse.pos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hse.pos.adapter.console.ConsoleApp;
import ru.hse.pos.application.PosFacade;
import ru.hse.pos.config.AppConfig;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 2 || (args.length > 0 && !args[0].equals("--demo"))) {
            System.err.println("Использование: homework2 [--demo [путь-к-CSV]]");
            System.exit(2);
        }
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            var output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            var console = new ConsoleApp(context.getBean(PosFacade.class), input, output);
            if (args.length == 0) {
                console.run();
            } else {
                console.runDemo(args.length == 2 ? args[1] : "pos-demo.csv");
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }
}
