package com.axia.starter.service;

import com.axia.starter.repository.IGenericRepository;
import com.axia.starter.mapper.BaseMapper;
import com.axia.starter.request.CustomPageRequest;
import com.axia.starter.request.SearchRequest;
import com.axia.starter.request.ConditionGroup;
import com.axia.starter.request.FilterRequest;
import com.axia.starter.specification.SpecificationBuilder;
import com.axia.starter.specification.GenericSpecificationBuilder;
import com.axia.starter.export.Exporter;
import com.axia.starter.enums.LogicalOperator;
import com.axia.starter.enums.SearchOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Transactional
public abstract class GenericService<E, D, ID> implements IService<E, D, ID> {

    protected IGenericRepository<E, ID> repository;
    protected BaseMapper<E, D> mapper;
    protected GenericSpecificationBuilder<E> genericSpecificationBuilder;
    protected Exporter<D> exporter;

    public GenericService(IGenericRepository<E, ID> repository,
                          BaseMapper<E, D> mapper,
                          GenericSpecificationBuilder<E> genericSpecificationBuilder,
                          Exporter<D> exporter) {
        this.repository = repository;
        this.mapper = mapper;
        this.genericSpecificationBuilder = genericSpecificationBuilder;
        this.exporter = exporter;
    }

    @Override
    public D findById(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found"));
        D dto = mapper.toDto(entity);
        afterFetch(dto);
        return dto;
    }

    @Override
    public D create(D dto) {
        beforeCreate(dto);
        E entity = mapper.toEntity(dto);
        E saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public D update(ID id, D dto) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found"));
        beforeUpdate(entity, dto);
        mapper.updateEntityFromDto(dto, entity);
        E updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }

    // ============ Méthodes avec SpecificationBuilder fluide (recommandé) ============

    /**
     * List entities using fluent SpecificationBuilder
     *
     * Example:
     * list(builder -> builder
     *     .eq("status", "ACTIVE")
     *     .gt("age", 18)
     * )
     */
    @Override
    public List<D> list(Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer) {
        SpecificationBuilder<E> builder = SpecificationBuilder.create();
        builderConsumer.accept(builder.and());
        Specification<E> spec = builder.build();

        return repository.findAll(spec).stream()
                .map(mapper::toDto)
                .map(this::afterFetch)
                .collect(Collectors.toList());
    }

    /**
     * List entities with custom root operator (AND/OR)
     *
     * Example:
     * list(LogicalOperator.OR, builder -> builder
     *     .eq("status", "ACTIVE")
     *     .eq("status", "PENDING")
     * )
     */
    @Override
    public List<D> list(LogicalOperator rootOperator, Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer) {
        SpecificationBuilder<E> builder = SpecificationBuilder.create();
        if (rootOperator == LogicalOperator.OR) {
            builderConsumer.accept(builder.or());
        } else {
            builderConsumer.accept(builder.and());
        }
        Specification<E> spec = builder.build();

        return repository.findAll(spec).stream()
                .map(mapper::toDto)
                .map(this::afterFetch)
                .collect(Collectors.toList());
    }

    /**
     * Search entities with pagination using fluent SpecificationBuilder
     *
     * Example:
     * search(builder -> builder
     *     .eq("status", "ACTIVE")
     *     .gt("age", 18),
     *     pageRequest
     * )
     */
    @Override
    public Page<D> search(Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer,
                          CustomPageRequest pageRequest) {
        SpecificationBuilder<E> builder = SpecificationBuilder.create();
        builderConsumer.accept(builder.and());
        Specification<E> spec = builder.build();

        return repository.findAll(spec, pageRequest.toPageable())
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    /**
     * Search with custom root operator and pagination
     */
    @Override
    public Page<D> search(LogicalOperator rootOperator,
                          Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer,
                          CustomPageRequest pageRequest) {
        SpecificationBuilder<E> builder = SpecificationBuilder.create();
        if (rootOperator == LogicalOperator.OR) {
            builderConsumer.accept(builder.or());
        } else {
            builderConsumer.accept(builder.and());
        }
        Specification<E> spec = builder.build();

        return repository.findAll(spec, pageRequest.toPageable())
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    /**
     * Export entities using fluent SpecificationBuilder
     */
    @Override
    public byte[] export(Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer) {
        SpecificationBuilder<E> builder = SpecificationBuilder.create();
        builderConsumer.accept(builder.and());
        Specification<E> spec = builder.build();

        List<D> entities = repository.findAll(spec).stream()
                .map(mapper::toDto)
                .map(this::afterFetch)
                .collect(Collectors.toList());
        return exporter.export(entities);
    }

    // ============ Méthodes avec ConditionGroup (pour requêtes complexes venant d'API) ============

    /**
     * List entities using ConditionGroup (useful for REST APIs)
     */
    @Override
    public List<D> list(ConditionGroup conditionGroup) {
        Specification<E> spec = genericSpecificationBuilder.build(conditionGroup);
        return repository.findAll(spec).stream()
                .map(mapper::toDto)
                .map(this::afterFetch)
                .collect(Collectors.toList());
    }

    /**
     * Search with ConditionGroup and pagination
     */
    @Override
    public Page<D> search(ConditionGroup conditionGroup, CustomPageRequest pageRequest) {
        Specification<E> spec = genericSpecificationBuilder.build(conditionGroup);
        return repository.findAll(spec, pageRequest.toPageable())
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    /**
     * Export with ConditionGroup
     */
    @Override
    public byte[] export(ConditionGroup conditionGroup) {
        Specification<E> spec = genericSpecificationBuilder.build(conditionGroup);
        List<D> entities = repository.findAll(spec).stream()
                .map(mapper::toDto)
                .map(this::afterFetch)
                .collect(Collectors.toList());
        return exporter.export(entities);
    }

    // ============ Méthodes utilitaires ============

    /**
     * Create a new SpecificationBuilder instance
     */
    protected SpecificationBuilder<E> createSpecificationBuilder() {
        return SpecificationBuilder.create();
    }

    /**
     * Build specification from SearchRequest (legacy)
     */
    protected Specification<E> buildSpecificationFromSearchRequest(SearchRequest searchRequest) {
        if (searchRequest == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        // Use ConditionGroup if present
        if (searchRequest.getConditionGroup() != null) {
            return genericSpecificationBuilder.build(searchRequest.getConditionGroup());
        }

        // Fallback to simple filters
        if (searchRequest.getFilters() != null && !searchRequest.getFilters().isEmpty()) {
            LogicalOperator operator = searchRequest.getOperator() != null ?
                    searchRequest.getOperator() : LogicalOperator.AND;
            return genericSpecificationBuilder.build(searchRequest.getFilters(), operator);
        }

        return (root, query, cb) -> cb.conjunction();
    }

    /**
     * Helper method to create simple equality filter
     */
    protected FilterRequest eq(String field, Object value) {
        return FilterRequest.builder()
                .field(field)
                .operation(SearchOperator.EQUAL)
                .value(value)
                .build();
    }

    /**
     * Helper method to create simple like filter
     */
    protected FilterRequest like(String field, String value) {
        return FilterRequest.builder()
                .field(field)
                .operation(SearchOperator.LIKE)
                .value(value)
                .build();
    }

    /**
     * Helper method to create simple greater than filter
     */
    protected FilterRequest gt(String field, Comparable<?> value) {
        return FilterRequest.builder()
                .field(field)
                .operation(SearchOperator.GREATER_THAN)
                .value(value)
                .build();
    }

    // Hooks
    protected void beforeCreate(D dto) {}
    protected void beforeUpdate(E entity, D dto) {}
    protected D afterFetch(D dto) {
        return dto;
    }
}