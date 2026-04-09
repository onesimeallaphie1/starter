package com.axia.starter.service;


import com.axia.starter.request.ConditionGroup;
import com.axia.starter.request.CustomPageRequest;
import com.axia.starter.request.FilterRequest;
import com.axia.starter.enums.LogicalOperator;
import com.axia.starter.request.SearchRequest;
import com.axia.starter.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Consumer;

public interface IService<E, D, ID> {
    D findById(ID id);
    D create(D dto);
    D update(ID id, D dto);
    void delete(ID id);
    List<D> list(Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer);
    List<D> list(LogicalOperator rootOperator, Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer);
    Page<D> search(Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer, CustomPageRequest pageRequest);
    Page<D> search(LogicalOperator rootOperator, Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer, CustomPageRequest pageRequest);
    byte[] export(Consumer<SpecificationBuilder.GroupBuilder<E>> builderConsumer);
    List<D> list(ConditionGroup conditionGroup);
    Page<D> search(ConditionGroup conditionGroup, CustomPageRequest pageRequest);
    byte[] export(ConditionGroup conditionGroup);
}