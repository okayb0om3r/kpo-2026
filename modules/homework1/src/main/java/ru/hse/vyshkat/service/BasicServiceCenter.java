package ru.hse.vyshkat.service;

import ru.hse.vyshkat.model.Transport;

/** Учебный техосмотр: решение по результату проверки исправности устройства. */
public final class BasicServiceCenter implements ServiceCenter {
    @Override
    public boolean approve(Transport transport) {
        return transport.isOperational();
    }
}
