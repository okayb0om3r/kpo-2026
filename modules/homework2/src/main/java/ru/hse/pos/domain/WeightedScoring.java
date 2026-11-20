package ru.hse.pos.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;

public final class WeightedScoring implements ScoringStrategy {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Override
    public BigDecimal percentage(List<Criterion> criteria, Map<String, Score> scores) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedPoints = BigDecimal.ZERO;
        for (Criterion criterion : criteria) {
            totalWeight = totalWeight.add(criterion.weight());
            Score score = scores.get(criterion.id());
            if (score != null) {
                BigDecimal contribution = score.points().multiply(criterion.weight())
                        .divide(criterion.maxScore(), MathContext.DECIMAL128);
                weightedPoints = weightedPoints.add(contribution);
            }
        }
        return weightedPoints.multiply(HUNDRED).divide(totalWeight, MathContext.DECIMAL128);
    }
}
