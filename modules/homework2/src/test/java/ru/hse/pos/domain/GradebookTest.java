package ru.hse.pos.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GradebookTest {
    private final Gradebook book = new Gradebook();

    @BeforeEach
    void prepare() {
        book.addStudent("s1", "Анна");
        book.addCriterion("lab", "Лабораторная", BigDecimal.TEN, BigDecimal.ONE);
    }

    @Test
    void rejectsInvalidStudentsAndCriteria() {
        assertThrows(IllegalArgumentException.class, () -> book.addStudent(" s1 ", "Другое имя"));
        assertThrows(IllegalArgumentException.class, () -> book.addStudent("s2", " "));
        assertThrows(IllegalArgumentException.class, () -> book.addStudent("", "Имя"));
        assertThrows(IllegalArgumentException.class,
                () -> book.addCriterion("lab", "Другое название", BigDecimal.TEN, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> book.addCriterion("new", "Критерий", BigDecimal.ZERO, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> book.addCriterion("new", "Критерий", BigDecimal.TEN, BigDecimal.ZERO));
        assertEquals(List.of(new Student("s1", "Анна")), book.students());
        assertEquals(1, book.criteria().size());
    }

    @Test
    void validatesStudentCriterionAndBoundsBeforeChangingExistingEvaluation() {
        book.recordEvaluation("s1", "lab", BigDecimal.TEN, "Готово");
        assertThrows(IllegalArgumentException.class,
                () -> book.recordEvaluation("s1", "missing", BigDecimal.ONE, ""));
        assertThrows(IllegalArgumentException.class,
                () -> book.recordEvaluation("missing", "lab", BigDecimal.ONE, ""));
        assertThrows(IllegalArgumentException.class,
                () -> book.recordEvaluation("s1", "lab", new BigDecimal("10.01"), ""));
        assertThrows(IllegalArgumentException.class,
                () -> book.recordEvaluation("s1", "lab", new BigDecimal("-0.01"), ""));
        Evaluation evaluation = book.evaluations(" s1 ").get(0);
        assertEquals(new Score(BigDecimal.TEN), evaluation.score());
        assertEquals("Готово", evaluation.comment());
        assertEquals(1, book.evaluations("s1").size());
    }

    @Test
    void correctsAndCancelsEvaluationWithoutAffectingAnotherStudent() {
        book.addStudent("s2", "Илья");
        book.recordEvaluation("s1", "lab", BigDecimal.ONE, "Первичная оценка");
        book.recordEvaluation("s2", "lab", BigDecimal.TEN, null);
        book.recordEvaluation("s1", "lab", BigDecimal.ZERO, "  Исправлено  ");
        List<Evaluation> evaluations = book.evaluations("s1");
        assertEquals(1, evaluations.size());
        assertEquals(new Score(BigDecimal.ZERO), evaluations.get(0).score());
        assertEquals("Исправлено", evaluations.get(0).comment());

        book.cancelEvaluation("s1", "lab");
        assertTrue(book.evaluations("s1").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> book.cancelEvaluation("s1", "lab"));
        assertEquals(new Score(BigDecimal.TEN), book.evaluations("s2").get(0).score());
    }

    @Test
    void scoreEqualityIgnoresDecimalScale() {
        assertEquals(new Score(new BigDecimal("3.00")), new Score(new BigDecimal("3.0")));
    }
}
