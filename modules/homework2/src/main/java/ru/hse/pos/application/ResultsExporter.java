package ru.hse.pos.application;

import java.io.IOException;
import java.util.List;
import ru.hse.pos.domain.StudentResult;

public interface ResultsExporter {
    void export(String destination, List<StudentResult> results) throws IOException;
}
