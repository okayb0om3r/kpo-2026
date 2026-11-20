package ru.hse.pos.domain;

import java.math.BigDecimal;

public final class Evaluation {
    private final String studentId;
    private final String criterionId;
    private final Score score;
    private final String comment;

    private Evaluation(String studentId, String criterionId, Score score, String comment) {
        this.studentId = studentId;
        this.criterionId = criterionId;
        this.score = score;
        this.comment = comment;
    }

    static Evaluation create(Student student, Criterion criterion, BigDecimal points, String comment) {
        Score score = new Score(points);
        if (score.points().compareTo(criterion.maxScore()) > 0) {
            throw new IllegalArgumentException("Балл превышает максимум " + criterion.maxScore().toPlainString() + ".");
        }
        return new Evaluation(student.id(), criterion.id(), score, comment == null ? "" : comment.strip());
    }

    public String studentId() {
        return studentId;
    }

    public String criterionId() {
        return criterionId;
    }

    public Score score() {
        return score;
    }

    public String comment() {
        return comment;
    }
}
