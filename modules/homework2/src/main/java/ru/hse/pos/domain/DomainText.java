package ru.hse.pos.domain;

final class DomainText {
    private DomainText() {
    }

    static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Заполните поле «" + field + "».");
        }
        return value.strip();
    }
}
