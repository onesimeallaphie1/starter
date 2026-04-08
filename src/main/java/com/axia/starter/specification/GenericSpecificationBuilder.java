package com.axia.starter.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class GenericSpecificationBuilder<E> {

    /**
     * Construit une Specification en combinant les filtres avec AND.
     */
    public Specification<E> build(List<FilterRequest> filters) {
        return  build(filters, LogicalOperator.AND);
    }

    /**
     * Construit une Specification avec un opérateur logique personnalisé (AND/OR).
     */
    public Specification<E> build(List<FilterRequest> filters, LogicalOperator operator) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (FilterRequest filter : filters) {
                Predicate predicate = createPredicate(filter, root, criteriaBuilder);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (operator == LogicalOperator.OR) {
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    /**
     * Crée un prédicat pour un filtre donné.
     */
    @SuppressWarnings("unchecked")
    private Predicate createPredicate(FilterRequest filter, Root<E> root, CriteriaBuilder cb) {
        String field = filter.getField();
        SearchOperation op = filter.getOperation();
        Object value = filter.getValue();
        Object secondValue = filter.getSecondValue();
        List<Object> values = filter.getValues();

        Path<Object> path = getPath(root, field);

        switch (op) {
            case EQUAL:
                return cb.equal(path, value);
            case NOT_EQUAL:
                return cb.notEqual(path, value);
            case LIKE:
                return cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            case NOT_LIKE:
                return cb.notLike(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            case GREATER_THAN:
                return cb.greaterThan(path.as(Comparable.class), (Comparable) value);
            case GREATER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case LESS_THAN:
                return cb.lessThan(path.as(Comparable.class), (Comparable) value);
            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case IN:
                if (!CollectionUtils.isEmpty(values)) {
                    return path.in(values);
                }
                return null;
            case NOT_IN:
                if (!CollectionUtils.isEmpty(values)) {
                    return cb.not(path.in(values));
                }
                return null;
            case IS_NULL:
                return cb.isNull(path);
            case IS_NOT_NULL:
                return cb.isNotNull(path);
            case BETWEEN:
                if (value != null && secondValue != null) {
                    return cb.between(path.as(Comparable.class), (Comparable) value, (Comparable) secondValue);
                }
                return null;
            default:
                throw new UnsupportedOperationException("Opérateur non supporté : " + op);
        }
    }

    /**
     * Résout le chemin JPA, en gérant les jointures implicites (ex: "adresse.ville").
     */
    private Path<Object> getPath(Root<E> root, String field) {
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