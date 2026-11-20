package ru.hse.pos.adapter.csv;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import ru.hse.pos.application.ResultsExporter;
import ru.hse.pos.domain.StudentResult;

public final class CsvResultsExporter implements ResultsExporter {
    @Override
    public void export(String destination, List<StudentResult> results) throws IOException {
        StringBuilder csv = new StringBuilder("student_id,student_name,percentage,evaluated,total_criteria\r\n");
        for (StudentResult result : results) {
            csv.append(escape(result.student().id())).append(',')
                    .append(escape(result.student().name())).append(',')
                    .append(result.percentage().map(BigDecimal::toPlainString).orElse("")).append(',')
                    .append(result.evaluated()).append(',')
                    .append(result.totalCriteria()).append("\r\n");
        }
        Path target = Path.of(destination).toAbsolutePath();
        if (target.getFileName() == null) {
            throw new IOException("Путь должен указывать на файл.");
        }
        Path temporary = Files.createTempFile(target.getParent(), ".pos-", ".csv");
        try {
            Files.writeString(temporary, csv, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String escape(String field) {
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}
