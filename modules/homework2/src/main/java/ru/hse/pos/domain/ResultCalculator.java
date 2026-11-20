package ru.hse.pos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ResultCalculator {
    private final ScoringStrategy strategy;

    public ResultCalculator(ScoringStrategy strategy) {
        this.strategy = strategy;
    }

    public StudentResult calculate(Gradebook gradebook, String studentId) {
        Student student = gradebook.student(studentId);
        List<Criterion> criteria = gradebook.criteria();
        Map<String, Score> scores = new HashMap<>();
        for (Evaluation evaluation : gradebook.evaluations(student.id())) {
            scores.put(evaluation.criterionId(), evaluation.score());
        }
        Optional<BigDecimal> percentage = criteria.isEmpty()
                ? Optional.empty()
                : Optional.of(strategy.percentage(criteria, Map.copyOf(scores)).setScale(2, RoundingMode.HALF_UP));
        return new StudentResult(student, percentage, scores.size(), criteria.size());
    }
}
