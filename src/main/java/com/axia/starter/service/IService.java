package com.axia.starter.service;


import com.axia.starter.request.CustomPageRequest;
import com.axia.starter.specification.FilterRequest;
import com.axia.starter.specification.LogicalOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface IService<E, D, ID> {
    D findById(ID id);
    D create(D dto);
    D update(ID id, D dto);
    void delete(ID id);
    List<D> list(List<FilterRequest> filters);
    Page<D> search(List<FilterRequest> filters, CustomPageRequest pageRequest);
    Page<D> search(List<FilterRequest> filters, LogicalOperator operator, CustomPageRequest pageRequest);

    byte[] export(List<FilterRequest> filters);
}