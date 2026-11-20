package ru.hse.pos.domain;

public interface GradebookRepository {
    Gradebook load();

    void save(Gradebook gradebook);
}
