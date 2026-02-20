package com.axia.starter.service;

import com.axia.starter.repository.IGenericRepository;
import com.axia.starter.mapper.BaseMapper;
import com.axia.starter.specification.FilterRequest;
import com.axia.starter.specification.GenericSpecificationBuilder;
import com.axia.starter.export.Exporter;
import com.axia.starter.specification.LogicalOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
public abstract class GenericService<E, D, ID> implements IService<E, D, ID> {

    protected IGenericRepository<E, ID> repository;
    protected BaseMapper<E, D> mapper;
    protected GenericSpecificationBuilder<E> specificationBuilder;
    protected Exporter<E> exporter;

    public GenericService(IGenericRepository<E, ID> repository,
                          BaseMapper<E, D> mapper,
                          GenericSpecificationBuilder<E> specificationBuilder,
                          Exporter<E> exporter) {
        this.repository = repository;
        this.mapper = mapper;
        this.specificationBuilder = specificationBuilder;
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

    @Override
    public List<D> list() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .map(this::afterFetch)
                .collect(Collectors.toList());
    }

    @Override
    public Page<D> paginate(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    @Override
    public Page<D> paginate(Pageable pageable, Specification<E> spec) {
        return repository.findAll(spec, pageable)
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    @Override
    public Page<D> search(List<FilterRequest> filters, Pageable pageable) {
        Specification<E> spec = specificationBuilder.build(filters);
        return repository.findAll(spec, pageable)
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    @Override
    public Page<D> search(List<FilterRequest> filters, LogicalOperator operator, Pageable pageable) {
        Specification<E> spec = specificationBuilder.build(filters, operator);
        return repository.findAll(spec, pageable)
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    @Override
    public byte[] export() {
        List<E> entities = repository.findAll();
        return exporter.export(entities);
    }

    // Hooks
    protected void beforeCreate(D dto) {}
    protected void beforeUpdate(E entity, D dto) {}
    protected D afterFetch(D dto) {
        return dto;
    }
}