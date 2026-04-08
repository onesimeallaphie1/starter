package com.axia.starter.service;

import com.axia.starter.repository.IGenericRepository;
import com.axia.starter.mapper.BaseMapper;
import com.axia.starter.request.CustomPageRequest;
import com.axia.starter.specification.FilterRequest;
import com.axia.starter.specification.GenericSpecificationBuilder;
import com.axia.starter.export.Exporter;
import com.axia.starter.specification.LogicalOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
public abstract class GenericService<E, D, ID> implements IService<E, D, ID> {

    protected IGenericRepository<E, ID> repository;
    protected BaseMapper<E, D> mapper;
    protected GenericSpecificationBuilder<E> specificationBuilder;
    protected Exporter<D> exporter;

    public GenericService(IGenericRepository<E, ID> repository,
                          BaseMapper<E, D> mapper,
                          GenericSpecificationBuilder<E> specificationBuilder,
                          Exporter<D> exporter) {
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
    public List<D> list(List<FilterRequest> filters) {
            Specification<E> spec = specificationBuilder.build(filters);
            return repository.findAll(spec).stream()
                    .map(mapper::toDto)
                    .map(this::afterFetch)
                    .collect(Collectors.toList());
    }

    @Override
    public Page<D> search(List<FilterRequest> filters, CustomPageRequest pageRequest) {
        Specification<E> spec = specificationBuilder.build(filters);
        return repository.findAll(spec, pageRequest.toPageable())
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    @Override
    public Page<D> search(List<FilterRequest> filters, LogicalOperator operator, CustomPageRequest pageRequest) {
        Specification<E> spec = specificationBuilder.build(filters, operator);
        return repository.findAll(spec, pageRequest.toPageable())
                .map(mapper::toDto)
                .map(this::afterFetch);
    }

    @Override
    public byte[] export(List<FilterRequest> filters) {
        Specification<E> spec = specificationBuilder.build(filters);
        List<D> entities = repository.findAll(spec).stream()
                .map(mapper::toDto)
                .map(this::afterFetch)
                .collect(Collectors.toList());
        return exporter.export(entities);
    }

    // Hooks
    protected void beforeCreate(D dto) {}
    protected void beforeUpdate(E entity, D dto) {}
    protected D afterFetch(D dto) {
        return dto;
    }
}