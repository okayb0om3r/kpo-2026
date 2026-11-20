package ru.hse.pos.domain;

import java.math.BigDecimal;

public record Criterion(String id, String name, BigDecimal maxScore, BigDecimal weight) {
    public Criterion {
        id = DomainText.required(id, "Код критерия");
        name = DomainText.required(name, "Название критерия");
        if (maxScore == null || maxScore.signum() <= 0) {
            throw new IllegalArgumentException("Максимальный балл должен быть больше нуля.");
        }
        if (weight == null || weight.signum() <= 0) {
            throw new IllegalArgumentException("Вес критерия должен быть больше нуля.");
        }
    }
}
