package ru.hse.pos.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface ScoringStrategy {
    BigDecimal percentage(List<Criterion> criteria, Map<String, Score> scores);
}
