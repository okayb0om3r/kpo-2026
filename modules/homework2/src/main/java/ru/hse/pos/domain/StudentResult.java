package ru.hse.pos.domain;

import java.math.BigDecimal;
import java.util.Optional;

public record StudentResult(Student student, Optional<BigDecimal> percentage, int evaluated, int totalCriteria) {
}
