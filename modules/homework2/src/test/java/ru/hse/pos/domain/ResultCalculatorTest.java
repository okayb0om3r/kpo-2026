package ru.hse.pos.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ResultCalculatorTest {
    private final ResultCalculator calculator = new ResultCalculator(new WeightedScoring());

    @Test
    void recomputesAgainstCurrentRubricAndEvaluations() {
        Gradebook book = new Gradebook();
        book.addStudent("s", "Анна");
        assertTrue(calculator.calculate(book, "s").percentage().isEmpty());

        book.addCriterion("lab", "Лабораторная", BigDecimal.TEN, BigDecimal.valueOf(2));
        book.recordEvaluation("s", "lab", BigDecimal.valueOf(8), null);
        assertResult(book, "80.00", 1, 1);

        book.addCriterion("project", "Проект", BigDecimal.valueOf(20), BigDecimal.valueOf(3));
        assertResult(book, "32.00", 1, 2);
        book.recordEvaluation("s", "project", BigDecimal.ZERO, null);
        assertResult(book, "32.00", 2, 2);
        book.recordEvaluation("s", "project", BigDecimal.valueOf(18), null);
        assertResult(book, "86.00", 2, 2);
        book.cancelEvaluation("s", "project");
        assertResult(book, "32.00", 1, 2);
        book.cancelEvaluation("s", "lab");
        assertResult(book, "0.00", 0, 2);
    }

    @Test
    void roundsTheFinalPercentageToTwoDecimalPlaces() {
        Gradebook book = new Gradebook();
        book.addStudent("s", "Анна");
        book.addCriterion("lab", "Лабораторная", BigDecimal.valueOf(3), new BigDecimal("0.25"));
        book.recordEvaluation("s", "lab", BigDecimal.valueOf(2), "");
        assertResult(book, "66.67", 1, 1);
    }

    @Test
    void calculatorUsesTheInjectedStrategy() {
        Gradebook book = new Gradebook();
        book.addStudent("s", "Анна");
        book.addCriterion("lab", "Лабораторная", BigDecimal.TEN, BigDecimal.ONE);
        book.recordEvaluation("s", "lab", BigDecimal.ONE, "");
        ResultCalculator alternative = new ResultCalculator((criteria, scores) -> {
            assertEquals("lab", criteria.get(0).id());
            assertEquals(new Score(BigDecimal.ONE), scores.get("lab"));
            return new BigDecimal("57.235");
        });

        assertEquals(Optional.of(new BigDecimal("57.24")), alternative.calculate(book, "s").percentage());
    }

    private void assertResult(Gradebook book, String expected, int evaluated, int criteria) {
        StudentResult result = calculator.calculate(book, "s");
        assertEquals(Optional.of(new BigDecimal(expected)), result.percentage());
        assertEquals(evaluated, result.evaluated());
        assertEquals(criteria, result.totalCriteria());
    }
}
