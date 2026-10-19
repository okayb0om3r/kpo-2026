package ru.hse.vyshkat.service;

import ru.hse.vyshkat.model.Transport;

@FunctionalInterface
public interface ServiceCenter {
    boolean approve(Transport transport);
}
