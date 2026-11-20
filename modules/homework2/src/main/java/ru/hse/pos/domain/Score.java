package ru.hse.pos.domain;

import java.math.BigDecimal;

public record Score(BigDecimal points) {
    public Score {
        if (points == null || points.signum() < 0) {
            throw new IllegalArgumentException("Балл должен быть неотрицательным.");
        }
        points = points.stripTrailingZeros();
    }
}
