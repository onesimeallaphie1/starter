package com.axia.starter.specification;

import com.axia.starter.request.ConditionGroup;
import com.axia.starter.request.FilterRequest;
import com.axia.starter.enums.LogicalOperator;
import com.axia.starter.enums.SearchOperator;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SpecificationBuilder<E> {

    private ConditionGroup rootGroup;
    private final GenericSpecificationBuilder<E> builder = new GenericSpecificationBuilder<>();
    private boolean distinct = true;

    private SpecificationBuilder() {}

    /**
     * Create a new SpecificationBuilder instance
     */
    public static <T> SpecificationBuilder<T> create() {
        return new SpecificationBuilder<>();
    }

    /**
     * Start with an AND group
     */
    public GroupBuilder<E> and() {
        this.rootGroup = ConditionGroup.builder()
                .operator(LogicalOperator.AND)
                .conditions(new ArrayList<>())
                .groups(new ArrayList<>())
                .build();
        return new GroupBuilder<>(this, rootGroup);
    }

    /**
     * Start with an OR group
     */
    public GroupBuilder<E> or() {
        this.rootGroup = ConditionGroup.builder()
                .operator(LogicalOperator.OR)
                .conditions(new ArrayList<>())
                .groups(new ArrayList<>())
                .build();
        return new GroupBuilder<>(this, rootGroup);
    }

    /**
     * Enable/disable distinct for queries (default: true)
     */
    public SpecificationBuilder<E> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Build the final Specification
     */
    public Specification<E> build() {
        Specification<E> spec = builder.build(rootGroup);
        if (!distinct) {
            return spec;
        }
        // Wrap with distinct if needed
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            return spec.toPredicate(root, query, cb);
        };
    }

    /**
     * GroupBuilder for building condition groups fluently
     */
    public static class GroupBuilder<E> {
        private final SpecificationBuilder<E> parent;
        private final ConditionGroup currentGroup;
        private GroupBuilder<E> parentGroup;

        public GroupBuilder(SpecificationBuilder<E> parent, ConditionGroup group) {
            this.parent = parent;
            this.currentGroup = group;
        }

        private GroupBuilder(SpecificationBuilder<E> parent, ConditionGroup group, GroupBuilder<E> parentGroup) {
            this.parent = parent;
            this.currentGroup = group;
            this.parentGroup = parentGroup;
        }

        // ============ Basic Operations ============

        public GroupBuilder<E> eq(String field, Object value) {
            addCondition(field, SearchOperator.EQUAL, value, null, null);
            return this;
        }

        public GroupBuilder<E> ne(String field, Object value) {
            addCondition(field, SearchOperator.NOT_EQUAL, value, null, null);
            return this;
        }

        public GroupBuilder<E> like(String field, String value) {
            addCondition(field, SearchOperator.LIKE, value, null, null);
            return this;
        }

        public GroupBuilder<E> notLike(String field, String value) {
            addCondition(field, SearchOperator.NOT_LIKE, value, null, null);
            return this;
        }

        public GroupBuilder<E> gt(String field, Comparable<?> value) {
            addCondition(field, SearchOperator.GREATER_THAN, value, null, null);
            return this;
        }

        public GroupBuilder<E> gte(String field, Comparable<?> value) {
            addCondition(field, SearchOperator.GREATER_THAN_OR_EQUAL, value, null, null);
            return this;
        }

        public GroupBuilder<E> lt(String field, Comparable<?> value) {
            addCondition(field, SearchOperator.LESS_THAN, value, null, null);
            return this;
        }

        public GroupBuilder<E> lte(String field, Comparable<?> value) {
            addCondition(field, SearchOperator.LESS_THAN_OR_EQUAL, value, null, null);
            return this;
        }

        public GroupBuilder<E> between(String field, Object start, Object end) {
            addCondition(field, SearchOperator.BETWEEN, start, end, null);
            return this;
        }

        public GroupBuilder<E> in(String field, Collection<?> values) {
            addCondition(field, SearchOperator.IN, null, null, new ArrayList<>(values));
            return this;
        }

        public GroupBuilder<E> in(String field, Object... values) {
            return in(field, List.of(values));
        }

        public GroupBuilder<E> notIn(String field, Collection<?> values) {
            addCondition(field, SearchOperator.NOT_IN, null, null, new ArrayList<>(values));
            return this;
        }

        public GroupBuilder<E> notIn(String field, Object... values) {
            return notIn(field, List.of(values));
        }

        public GroupBuilder<E> isNull(String field) {
            addCondition(field, SearchOperator.IS_NULL, null, null, null);
            return this;
        }

        public GroupBuilder<E> isNotNull(String field) {
            addCondition(field, SearchOperator.IS_NOT_NULL, null, null, null);
            return this;
        }

        // ============ Group Operations ============

        public GroupBuilder<E> andGroup() {
            ConditionGroup newGroup = ConditionGroup.builder()
                    .operator(LogicalOperator.AND)
                    .conditions(new ArrayList<>())
                    .groups(new ArrayList<>())
                    .build();
            currentGroup.addGroup(newGroup);
            return new GroupBuilder<>(parent, newGroup, this);
        }

        public GroupBuilder<E> orGroup() {
            ConditionGroup newGroup = ConditionGroup.builder()
                    .operator(LogicalOperator.OR)
                    .conditions(new ArrayList<>())
                    .groups(new ArrayList<>())
                    .build();
            currentGroup.addGroup(newGroup);
            return new GroupBuilder<>(parent, newGroup, this);
        }

        public GroupBuilder<E> end() {
            if (parentGroup == null) {
                throw new IllegalStateException("Cannot end root group");
            }
            return parentGroup;
        }

        public Specification<E> build() {
            return parent.build();
        }

        public Specification<E> buildAnd(Function<Specification<E>, Specification<E>> postProcessor) {
            Specification<E> spec = build();
            return postProcessor.apply(spec);
        }

        // ============ Private Helpers ============

        private void addCondition(String field, SearchOperator operation,
                                  Object value, Object secondValue, List<Object> values) {
            FilterRequest filter = FilterRequest.builder()
                    .field(field)
                    .operation(operation)
                    .value(value)
                    .secondValue(secondValue)
                    .values(values)
                    .build();
            currentGroup.addCondition(filter);
        }
    }
}