package com.axia.starter.specification;

public enum SearchOperation {
    EQUAL("eq"),
    NOT_EQUAL("neq"),
    LIKE("like"),
    NOT_LIKE("notLike"),
    GREATER_THAN("gt"),
    GREATER_THAN_OR_EQUAL("gte"),
    LESS_THAN("lt"),
    LESS_THAN_OR_EQUAL("lte"),
    IN("in"),
    NOT_IN("notIn"),
    IS_NULL("isNull"),
    IS_NOT_NULL("isNotNull"),
    BETWEEN("between");

    private final String value;

    SearchOperation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SearchOperation fromValue(String value) {
        for (SearchOperation op : values()) {
            if (op.value.equalsIgnoreCase(value)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Opérateur non supporté : " + value);
    }
}