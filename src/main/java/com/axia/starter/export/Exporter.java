package com.axia.starter.export;

import java.util.List;

public interface Exporter<D> {
    byte[] export(List<D> entities);
}