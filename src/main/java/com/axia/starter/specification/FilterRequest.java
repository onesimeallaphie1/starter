package com.axia.starter.specification;

import java.util.List;

public class FilterRequest {
    private String field;          // Nom du champ (ex: "name", "address.city")
    private SearchOperation operation;
    private Object value;           // Pour les opérations simples
    private Object secondValue;     // Pour BETWEEN (valeur haute)
    private List<Object> values;    // Pour IN / NOT_IN

    // Constructeurs
    public FilterRequest() {}

    public FilterRequest(String field, SearchOperation operation, Object value) {
        this.field = field;
        this.operation = operation;
        this.value = value;
    }

    // Getters et setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public SearchOperation getOperation() { return operation; }
    public void setOperation(SearchOperation operation) { this.operation = operation; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    public Object getSecondValue() { return secondValue; }
    public void setSecondValue(Object secondValue) { this.secondValue = secondValue; }

    public List<Object> getValues() { return values; }
    public void setValues(List<Object> values) { this.values = values; }
}