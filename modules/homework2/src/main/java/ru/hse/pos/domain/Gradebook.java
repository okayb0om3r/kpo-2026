package ru.hse.pos.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Gradebook {
    private record EvaluationKey(String studentId, String criterionId) {
    }

    private final Map<String, Student> students;
    private final Map<String, Criterion> criteria;
    private final Map<EvaluationKey, Evaluation> evaluations;

    public Gradebook() {
        students = new LinkedHashMap<>();
        criteria = new LinkedHashMap<>();
        evaluations = new LinkedHashMap<>();
    }

    private Gradebook(Gradebook source) {
        students = new LinkedHashMap<>(source.students);
        criteria = new LinkedHashMap<>(source.criteria);
        evaluations = new LinkedHashMap<>(source.evaluations);
    }

    public Gradebook copy() {
        return new Gradebook(this);
    }

    public void addStudent(String id, String name) {
        Student student = new Student(id, name);
        if (students.putIfAbsent(student.id(), student) != null) {
            throw new IllegalArgumentException("Студент с таким кодом уже есть.");
        }
    }

    public void addCriterion(String id, String name, BigDecimal maxScore, BigDecimal weight) {
        Criterion criterion = new Criterion(id, name, maxScore, weight);
        if (criteria.putIfAbsent(criterion.id(), criterion) != null) {
            throw new IllegalArgumentException("Критерий с таким кодом уже есть.");
        }
    }

    public void recordEvaluation(String studentId, String criterionId, BigDecimal points, String comment) {
        Student student = student(studentId);
        Criterion criterion = criterion(criterionId);
        Evaluation evaluation = Evaluation.create(student, criterion, points, comment);
        evaluations.put(new EvaluationKey(evaluation.studentId(), evaluation.criterionId()), evaluation);
    }

    public void cancelEvaluation(String studentId, String criterionId) {
        Student student = student(studentId);
        Criterion criterion = criterion(criterionId);
        if (evaluations.remove(new EvaluationKey(student.id(), criterion.id())) == null) {
            throw new IllegalArgumentException("Оценка по этому критерию ещё не выставлена.");
        }
    }

    public Student student(String id) {
        Student student = students.get(DomainText.required(id, "Код студента"));
        if (student == null) {
            throw new IllegalArgumentException("Студент с таким кодом не найден.");
        }
        return student;
    }

    private Criterion criterion(String id) {
        Criterion criterion = criteria.get(DomainText.required(id, "Код критерия"));
        if (criterion == null) {
            throw new IllegalArgumentException("Критерия с таким кодом нет в рубрикаторе.");
        }
        return criterion;
    }

    public List<Student> students() {
        return List.copyOf(students.values());
    }

    public List<Criterion> criteria() {
        return List.copyOf(criteria.values());
    }

    public List<Evaluation> evaluations(String studentId) {
        Student student = student(studentId);
        return criteria.keySet().stream()
                .map(id -> evaluations.get(new EvaluationKey(student.id(), id)))
                .filter(evaluation -> evaluation != null)
                .toList();
    }
}
