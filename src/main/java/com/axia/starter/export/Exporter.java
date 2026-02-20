package com.axia.starter.export;

import java.util.List;

public interface Exporter<E> {
    byte[] export(List<E> entities);
}