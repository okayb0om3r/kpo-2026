package ru.hse.pos.application;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import ru.hse.pos.domain.Criterion;
import ru.hse.pos.domain.Evaluation;
import ru.hse.pos.domain.Gradebook;
import ru.hse.pos.domain.GradebookRepository;
import ru.hse.pos.domain.ResultCalculator;
import ru.hse.pos.domain.Student;
import ru.hse.pos.domain.StudentResult;

public final class PosFacade {
    private final GradebookRepository repository;
    private final ResultCalculator calculator;
    private final ResultsExporter exporter;

    public PosFacade(GradebookRepository repository, ResultCalculator calculator, ResultsExporter exporter) {
        this.repository = repository;
        this.calculator = calculator;
        this.exporter = exporter;
    }

    public void addStudent(String id, String name) {
        update(book -> book.addStudent(id, name));
    }

    public void addCriterion(String id, String name, BigDecimal maxScore, BigDecimal weight) {
        update(book -> book.addCriterion(id, name, maxScore, weight));
    }

    public void recordEvaluation(String studentId, String criterionId, BigDecimal score, String comment) {
        update(book -> book.recordEvaluation(studentId, criterionId, score, comment));
    }

    public void cancelEvaluation(String studentId, String criterionId) {
        update(book -> book.cancelEvaluation(studentId, criterionId));
    }

    public List<Student> students() {
        return repository.load().students();
    }

    public List<Criterion> criteria() {
        return repository.load().criteria();
    }

    public List<Evaluation> evaluations(String studentId) {
        return repository.load().evaluations(studentId);
    }

    public StudentResult result(String studentId) {
        return calculator.calculate(repository.load(), studentId);
    }

    public void exportResults(String destination) throws IOException {
        Gradebook book = repository.load();
        List<StudentResult> results = book.students().stream()
                .map(student -> calculator.calculate(book, student.id()))
                .toList();
        exporter.export(destination, results);
    }

    private void update(Consumer<Gradebook> change) {
        Gradebook book = repository.load();
        change.accept(book);
        repository.save(book);
    }
}
