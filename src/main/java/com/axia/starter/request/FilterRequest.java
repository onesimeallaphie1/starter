package com.axia.starter.request;

import com.axia.starter.enums.SearchOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FilterRequest {
    private String field;          // Nom du champ (ex: "name", "address.city")
    private SearchOperator operation;
    private Object value;           // Pour les opérations simples
    private Object secondValue;     // Pour BETWEEN (valeur haute)
    private List<Object> values;    // Pour IN / NOT_IN
}