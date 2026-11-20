package ru.hse.pos.adapter.console;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import ru.hse.pos.application.PosFacade;
import ru.hse.pos.domain.Criterion;
import ru.hse.pos.domain.Evaluation;
import ru.hse.pos.domain.Student;
import ru.hse.pos.domain.StudentResult;

public final class ConsoleApp {
    private static final String MENU = """
            1. Добавить студента
            2. Показать студентов
            3. Добавить критерий
            4. Показать рубрикатор
            5. Выставить или исправить оценку
            6. Показать оценки студента
            7. Отменить оценку
            8. Показать итог студента
            9. Экспортировать итоги в CSV
            0. Выход""";

    private final PosFacade pos;
    private final BufferedReader input;
    private final PrintWriter output;

    public ConsoleApp(PosFacade pos, BufferedReader input, PrintWriter output) {
        this.pos = pos;
        this.input = input;
        this.output = output;
    }

    public void run() throws IOException {
        output.println("ПОС — Преподавательская Оценка Студентов");
        output.println(MENU);
        while (true) {
            try {
                String command = read("Команда (? — меню)");
                if (command.equals("0")) {
                    return;
                }
                execute(command);
            } catch (EOFException e) {
                return;
            } catch (IllegalArgumentException e) {
                output.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void execute(String command) throws IOException {
        switch (command) {
            case "1" -> addStudent();
            case "2" -> showStudents();
            case "3" -> addCriterion();
            case "4" -> showCriteria();
            case "5" -> recordEvaluation();
            case "6" -> showEvaluations(read("Код студента"));
            case "7" -> cancelEvaluation();
            case "8" -> showResult(read("Код студента"));
            case "9" -> exportFromMenu();
            case "?" -> output.println(MENU);
            case "" -> { }
            default -> output.println("Неизвестная команда. Введите ? для меню.");
        }
    }

    private void addStudent() throws IOException {
        pos.addStudent(read("Код студента"), read("Имя студента"));
        output.println("Студент добавлен.");
    }

    private void showStudents() {
        List<Student> students = pos.students();
        if (students.isEmpty()) {
            output.println("Студентов пока нет.");
        }
        for (Student student : students) {
            output.printf("%s — %s%n", student.id(), student.name());
        }
    }

    private void addCriterion() throws IOException {
        pos.addCriterion(read("Код критерия"), read("Название критерия"),
                number("Максимальный балл"), number("Вес"));
        output.println("Критерий добавлен.");
    }

    private void showCriteria() {
        List<Criterion> criteria = pos.criteria();
        if (criteria.isEmpty()) {
            output.println("Рубрикатор пуст.");
        }
        for (Criterion criterion : criteria) {
            output.printf("%s — %s (%s баллов, вес %s)%n", criterion.id(), criterion.name(),
                    criterion.maxScore().toPlainString(), criterion.weight().toPlainString());
        }
    }

    private void recordEvaluation() throws IOException {
        pos.recordEvaluation(read("Код студента"), read("Код критерия"), number("Балл"),
                read("Комментарий (Enter — пропустить)"));
        output.println("Оценка сохранена.");
    }

    private void showEvaluations(String studentId) {
        List<Evaluation> evaluations = pos.evaluations(studentId);
        if (evaluations.isEmpty()) {
            output.println("Оценок пока нет.");
        }
        for (Evaluation evaluation : evaluations) {
            String comment = evaluation.comment().isEmpty() ? "" : " — " + evaluation.comment();
            output.printf("%s: %s%s%n", evaluation.criterionId(), evaluation.score().points().toPlainString(),
                    comment);
        }
    }

    private void cancelEvaluation() throws IOException {
        pos.cancelEvaluation(read("Код студента"), read("Код критерия"));
        output.println("Оценка отменена.");
    }

    private void showResult(String studentId) {
        StudentResult result = pos.result(studentId);
        String summary = result.percentage().map(value -> value.toPlainString() + " %")
                .orElse("рубрикатор пуст");
        output.printf("%s — %s, оценено %d из %d критериев%n", result.student().name(), summary,
                result.evaluated(), result.totalCriteria());
    }

    private void exportFromMenu() throws IOException {
        String destination = read("Путь к CSV (существующий файл будет перезаписан)");
        try {
            exportResults(destination);
        } catch (IOException e) {
            output.println("Не удалось записать CSV: " + e.getMessage());
        }
    }

    private void exportResults(String destination) throws IOException {
        if (destination.isEmpty()) {
            throw new IllegalArgumentException("Укажите путь к CSV.");
        }
        pos.exportResults(destination);
        output.println("Итоги экспортированы: " + destination);
    }

    public void runDemo(String destination) throws IOException {
        output.println("ПОС — демонстрация");
        pos.addStudent("s1", "Анна Смирнова");
        pos.addStudent("s2", "Илья Ким");
        pos.addCriterion("lab", "Лабораторная", BigDecimal.TEN, BigDecimal.valueOf(40));
        pos.addCriterion("project", "Проект", BigDecimal.valueOf(20), BigDecimal.valueOf(60));
        showStudents();
        showCriteria();

        pos.recordEvaluation("s1", "lab", BigDecimal.valueOf(8), "Работа принята");
        pos.recordEvaluation("s1", "project", BigDecimal.valueOf(16), "");
        try {
            pos.recordEvaluation("s1", "lab", BigDecimal.valueOf(12), "");
        } catch (IllegalArgumentException e) {
            output.println("Ожидаемый отказ: " + e.getMessage());
        }
        pos.recordEvaluation("s1", "project", BigDecimal.valueOf(18), "Оценка уточнена");
        pos.recordEvaluation("s2", "lab", BigDecimal.TEN, "");
        pos.recordEvaluation("s2", "project", BigDecimal.TEN, "");
        pos.cancelEvaluation("s2", "project");
        showEvaluations("s1");
        showResult("s1");
        showResult("s2");
        exportResults(destination);
    }

    private String read(String prompt) throws IOException {
        output.print(prompt + ": ");
        output.flush();
        String line = input.readLine();
        if (line == null) {
            throw new EOFException();
        }
        return line.strip();
    }

    private BigDecimal number(String prompt) throws IOException {
        String value = read(prompt).replace(',', '.');
        if (!value.matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")) {
            throw new IllegalArgumentException("Введите число, например 7 или 7,5.");
        }
        return new BigDecimal(value);
    }
}
