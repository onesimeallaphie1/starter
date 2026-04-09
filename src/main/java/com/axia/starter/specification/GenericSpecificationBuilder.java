package com.axia.starter.specification;

import com.axia.starter.exceptions.SpecificationException;
import com.axia.starter.request.FilterRequest;
import com.axia.starter.request.ConditionGroup;
import com.axia.starter.enums.LogicalOperator;
import com.axia.starter.enums.SearchOperator;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class GenericSpecificationBuilder<E> {

    /**
     * Build specification from condition group (supports nested AND/OR)
     */
    public Specification<E> build(ConditionGroup conditionGroup) {
        if (conditionGroup == null || conditionGroup.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true); // Avoid duplicates with joins
            }
            return buildPredicateFromGroup(conditionGroup, root, cb);
        };
    }

    /**
     * Build specification from simple filter list (backward compatible)
     */
    public Specification<E> build(List<FilterRequest> filters) {
        return build(filters, LogicalOperator.AND);
    }

    /**
     * Build specification from filter list with custom operator (backward compatible)
     */
    public Specification<E> build(List<FilterRequest> filters, LogicalOperator operator) {
        if (CollectionUtils.isEmpty(filters)) {
            return (root, query, cb) -> cb.conjunction();
        }

        ConditionGroup group = ConditionGroup.builder()
                .operator(operator)
                .conditions(filters)
                .build();
        return build(group);
    }

    /**
     * Recursively build predicate from condition group
     */
    private Predicate buildPredicateFromGroup(ConditionGroup group, Root<E> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Process simple conditions
        if (!CollectionUtils.isEmpty(group.getConditions())) {
            for (FilterRequest filter : group.getConditions()) {
                try {
                    Predicate predicate = createPredicate(filter, root, cb);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                } catch (Exception e) {
                    throw new SpecificationException("Failed to create predicate for field: " + filter.getField(), e);
                }
            }
        }

        // Process nested groups recursively
        if (!CollectionUtils.isEmpty(group.getGroups())) {
            for (ConditionGroup subGroup : group.getGroups()) {
                Predicate subPredicate = buildPredicateFromGroup(subGroup, root, cb);
                if (subPredicate != null) {
                    predicates.add(subPredicate);
                }
            }
        }

        if (predicates.isEmpty()) {
            return cb.conjunction();
        }

        // Apply logical operator
        if (group.getOperator() == LogicalOperator.OR) {
            return cb.or(predicates.toArray(new Predicate[0]));
        } else {
            return cb.and(predicates.toArray(new Predicate[0]));
        }
    }

    /**
     * Create a single predicate from filter request
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate createPredicate(FilterRequest filter, Root<E> root, CriteriaBuilder cb) {
        String field = filter.getField();
        SearchOperator op = filter.getOperation();
        Object value = filter.getValue();
        Object secondValue = filter.getSecondValue();
        List<Object> values = filter.getValues();

        Path<Object> path = getPath(root, field);

        return switch (op) {
            case EQUAL -> handleNullValue(path, cb, value, true);
            case NOT_EQUAL -> handleNullValue(path, cb, value, false);
            case LIKE -> {
                if (value == null) yield null;
                yield cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            }
            case NOT_LIKE -> {
                if (value == null) yield null;
                yield cb.notLike(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            }
            case GREATER_THAN -> {
                if (value == null) yield null;
                yield cb.greaterThan(path.as(Comparable.class), (Comparable) value);
            }
            case GREATER_THAN_OR_EQUAL -> {
                if (value == null) yield null;
                yield cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            }
            case LESS_THAN -> {
                if (value == null) yield null;
                yield cb.lessThan(path.as(Comparable.class), (Comparable) value);
            }
            case LESS_THAN_OR_EQUAL -> {
                if (value == null) yield null;
                yield cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            }
            case IN -> {
                if (CollectionUtils.isEmpty(values)) yield null;
                yield handleInOperation(path, values, cb);
            }
            case NOT_IN -> {
                if (CollectionUtils.isEmpty(values)) yield null;
                yield cb.not(handleInOperation(path, values, cb));
            }
            case IS_NULL -> cb.isNull(path);
            case IS_NOT_NULL -> cb.isNotNull(path);
            case BETWEEN -> {
                if (value == null || secondValue == null) yield null;
                yield cb.between(path.as(Comparable.class), (Comparable) value, (Comparable) secondValue);
            }
        };
    }

    /**
     * Handle IN operation with proper type conversion
     */
    private Predicate handleInOperation(Path<Object> path, List<Object> values, CriteriaBuilder cb) {
        CriteriaBuilder.In<Object> in = cb.in(path);
        for (Object val : values) {
            if (val != null) {
                in.value(val);
            }
        }
        return in;
    }

    /**
     * Handle null values for EQUAL/NOT_EQUAL operations
     */
    private Predicate handleNullValue(Path<Object> path, CriteriaBuilder cb, Object value, boolean isEqual) {
        if (value == null) {
            return isEqual ? cb.isNull(path) : cb.isNotNull(path);
        }
        return isEqual ? cb.equal(path, value) : cb.notEqual(path, value);
    }

    /**
     * Resolve JPA path supporting nested fields (e.g., "address.city")
     */
    private Path<Object> getPath(Root<E> root, String field) {
        if (field == null || field.trim().isEmpty()) {
            throw new SpecificationException("Field name cannot be null or empty");
        }

        if (!field.contains(".")) {
            return root.get(field);
        }

        String[] parts = field.split("\\.");
        Path<Object> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }
}