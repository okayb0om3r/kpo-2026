package ru.hse.pos.adapter.memory;

import ru.hse.pos.domain.Gradebook;
import ru.hse.pos.domain.GradebookRepository;

public final class InMemoryGradebookRepository implements GradebookRepository {
    private Gradebook book = new Gradebook();

    @Override
    public Gradebook load() {
        return book.copy();
    }

    @Override
    public void save(Gradebook gradebook) {
        book = gradebook.copy();
    }
}
