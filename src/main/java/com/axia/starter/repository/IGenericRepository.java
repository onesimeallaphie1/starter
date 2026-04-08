package com.axia.starter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface IGenericRepository<E, ID> extends JpaRepository<E, ID>, JpaSpecificationExecutor<E> {}