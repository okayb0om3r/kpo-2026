package ru.hse.vyshkat.model;

public abstract class Transport extends Asset {
    private final int beginnerEase;
    private final boolean operational;

    protected Transport(String number, String name, int beginnerEase, boolean operational) {
        super(number, name);
        if (beginnerEase < 1 || beginnerEase > 10) {
            throw new IllegalArgumentException("Простота управления должна быть от 1 до 10");
        }
        this.beginnerEase = beginnerEase;
        this.operational = operational;
    }

    public final int beginnerEase() {
        return beginnerEase;
    }

    public final boolean isOperational() {
        return operational;
    }
}
