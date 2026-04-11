package com.axia.starter.service;

import com.axia.starter.repository.IGenericRepository;
import com.axia.starter.mapper.BaseMapper;
import com.axia.starter.request.CustomPageRequest;
import com.axia.starter.request.ConditionGroup;
import com.axia.starter.specification.SpecificationBuilder;
import com.axia.starter.specification.GenericSpecificationBuilder;
import com.axia.starter.export.Exporter;
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
        D savedDto = mapper.toDto(saved);
        afterCreate(saved, savedDto);
        return savedDto;
    }

    @Override
    public D update(ID id, D dto) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found"));
        mapper.updateEntityFromDto(dto, entity);
        beforeUpdate(entity, dto);
        E updated = repository.save(entity);
        D updatedDto = mapper.toDto(updated);
        afterUpdate(updated, updatedDto);
        return updatedDto;
    }

    @Override
    public void delete(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found"));
        beforeDelete(entity);
        repository.deleteById(id);
        afterDelete(entity);
    }

    // ============ Méthodes avec SpecificationBuilder fluide (recommandé) ============

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

    @Override
    public Page<D> paginate(Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer,
                          CustomPageRequest pageRequest) {
        SpecificationBuilder<E> builder = SpecificationBuilder.create();
        builderConsumer.accept(builder.and());
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
    public Page<D> paginate(ConditionGroup conditionGroup, CustomPageRequest pageRequest) {
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

    // Hooks
    protected void beforeCreate(D dto) {}
    protected void afterCreate(E entity, D dto) {}
    protected void beforeUpdate(E entity, D dto) {}
    protected void afterUpdate(E entity, D dto) {}
    protected void beforeDelete(E entity) {}
    protected void afterDelete(E entity) {}
    protected D afterFetch(D dto) {
        return dto;
    }
}