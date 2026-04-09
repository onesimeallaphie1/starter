package com.axia.starter.request;

import com.axia.starter.enums.LogicalOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionGroup {
    private LogicalOperator operator;  // AND ou OR entre les conditions de ce groupe
    private List<FilterRequest> conditions;  // Conditions simples
    private List<ConditionGroup> groups;  // Sous-groupes (pour la récursivité)

    // Helper method to check if group is empty
    public boolean isEmpty() {
        return (conditions == null || conditions.isEmpty()) &&
                (groups == null || groups.isEmpty());
    }

    // Helper method to add a condition
    public void addCondition(FilterRequest condition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(condition);
    }

    // Helper method to add a subgroup
    public void addGroup(ConditionGroup group) {
        if (this.groups == null) {
            this.groups = new ArrayList<>();
        }
        this.groups.add(group);
    }
}