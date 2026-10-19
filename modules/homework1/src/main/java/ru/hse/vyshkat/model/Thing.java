package ru.hse.vyshkat.model;

public abstract class Thing extends Asset {
    protected Thing(String number, String name) {
        super(number, name);
    }
}
