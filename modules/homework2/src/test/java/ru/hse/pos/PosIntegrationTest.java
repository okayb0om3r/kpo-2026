package ru.hse.pos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hse.pos.adapter.console.ConsoleApp;
import ru.hse.pos.application.PosFacade;
import ru.hse.pos.config.AppConfig;
import ru.hse.pos.domain.GradebookRepository;

class PosIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void demoUsesProductionContainerAndExportsBothResults() throws IOException {
        Path csv = directory.resolve("demo.csv");
        Main.main(new String[]{"--demo", csv.toString()});

        assertEquals("""
                student_id,student_name,percentage,evaluated,total_criteria
                "s1","Анна Смирнова",86.00,2,2
                "s2","Илья Ким",40.00,1,2
                """.replace("\n", "\r\n"), Files.readString(csv));
    }

    @Test
    void acceptsInteractiveInputAndContinuesAfterErrors() throws IOException {
        Path csv = directory.resolve("interactive.csv");
        String commands = """
                ?
                2
                4
                1
                s
                Анна, "студентка"
                3
                lab
                Лабораторная
                10
                2
                5
                s
                lab
                NaN
                5
                s
                lab
                12

                5
                s
                lab
                7,5
                принято
                6
                s
                8
                s
                9
                %s
                7
                s
                lab
                6
                s
                9
                %s
                0
                """.formatted(csv, directory.resolve("missing/file.csv"));
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            StringWriter output = new StringWriter();
            var console = new ConsoleApp(context.getBean(PosFacade.class),
                    new BufferedReader(new StringReader(commands)), new PrintWriter(output, true));
            console.run();

            String transcript = output.toString();
            assertTrue(transcript.contains("Студентов пока нет."));
            assertTrue(transcript.contains("Рубрикатор пуст."));
            assertTrue(transcript.contains("Введите число"));
            assertTrue(transcript.contains("Балл превышает максимум 10."));
            assertTrue(transcript.contains("lab: 7.5 — принято"));
            assertTrue(transcript.contains("75.00 %"));
            assertTrue(transcript.contains("Оценок пока нет."));
            assertTrue(transcript.contains("Не удалось записать CSV"));
            assertTrue(Files.readString(csv).contains("\"s\",\"Анна, \"\"студентка\"\"\",75.00,1,1\r\n"));
        }
    }

    @Test
    void eofDuringStudentEntryDoesNotSavePartialData() throws IOException {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PosFacade pos = context.getBean(PosFacade.class);
            var console = new ConsoleApp(pos, new BufferedReader(new StringReader("1\ns\n")),
                    new PrintWriter(new StringWriter(), true));
            console.run();
            assertTrue(pos.students().isEmpty());
        }
    }

    @Test
    void exportsBlankPercentageWithoutRubricAndKeepsRepositoryStateOnFailure() throws IOException {
        Path csv = directory.resolve("empty-rubric.csv");
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PosFacade pos = context.getBean(PosFacade.class);
            pos.addStudent("s", "Анна");
            assertThrows(IllegalArgumentException.class,
                    () -> pos.recordEvaluation("s", "missing", BigDecimal.ONE, ""));
            assertTrue(pos.evaluations("s").isEmpty());

            GradebookRepository repository = context.getBean(GradebookRepository.class);
            repository.load().addStudent("unsaved", "Илья");
            assertEquals(1, pos.students().size());

            Files.writeString(csv, "предыдущий экспорт");
            pos.exportResults(csv.toString());
            assertTrue(Files.readString(csv).endsWith("\"s\",\"Анна\",,0,0\r\n"));
        }
    }
}
