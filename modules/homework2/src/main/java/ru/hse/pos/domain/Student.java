package ru.hse.pos.domain;

public record Student(String id, String name) {
    public Student {
        id = DomainText.required(id, "Код студента");
        name = DomainText.required(name, "Имя студента");
    }
}
