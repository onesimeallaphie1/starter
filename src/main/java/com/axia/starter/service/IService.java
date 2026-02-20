package com.axia.starter.service;


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
    List<D> list();
    Page<D> paginate(Pageable pageable);
    Page<D> paginate(Pageable pageable, Specification<E> spec);
    Page<D> search(List<FilterRequest> filters, Pageable pageable);
    Page<D> search(List<FilterRequest> filters, LogicalOperator operator, Pageable pageable);
    byte[] export(); // Export générique
}