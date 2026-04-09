package com.axia.starter.request;

import com.axia.starter.enums.LogicalOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    // Pour l'ancienne API (simple filters)
    private List<FilterRequest> filters;
    private LogicalOperator operator;

    // Pour la nouvelle API (requêtes complexes)
    private ConditionGroup conditionGroup;

    // Helper method to check if using complex query
    public boolean isComplexQuery() {
        return conditionGroup != null && !conditionGroup.isEmpty();
    }

    // Helper method to check if using simple query
    public boolean isSimpleQuery() {
        return filters != null && !filters.isEmpty();
    }
}