package com.axia.starter.controllers;

import com.axia.starter.enums.LogicalOperator;
import com.axia.starter.enums.SearchOperator;
import com.axia.starter.request.ConditionGroup;
import com.axia.starter.request.CustomPageRequest;
import com.axia.starter.request.FilterRequest;
import com.axia.starter.service.IService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Supplier;

public abstract class GenericReactiveController<E, D, ID> {

    protected abstract List<String> getSearchFields();

    protected abstract IService<E, D, ID> getService();

    // ============ CRUD de base ============

    @PostMapping
    public Mono<ResponseEntity<D>> create(@RequestBody D dto) {
        return wrapBlocking(() -> getService().create(dto))
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<D>> update(@PathVariable("id") ID id, @RequestBody D dto) {
        return wrapBlocking(() -> getService().update(id, dto))
                .map(updated -> ResponseEntity.status(HttpStatus.OK).body(updated));
    }

    @DeleteMapping
    public Mono<ResponseEntity<?>> delete(@RequestParam("id") ID id) {
        return wrapBlockingVoid(() -> getService().delete(id))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(id));
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<D>> findById(@PathVariable("id") ID id) {
        return wrapBlocking(() -> getService().findById(id))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    // ============ Recherche / pagination / export génériques ============

    @PostMapping("list")
    public Mono<ResponseEntity<List<D>>> list(@RequestBody ConditionGroup conditionGroup,
                                              @RequestParam(name = "query", required = false, defaultValue = "") String query) {
        return wrapBlocking(() -> getService().list(buildSearchGroup(conditionGroup, query)))
                .map(ResponseEntity::ok);
    }

    @PostMapping("paginate")
    public Mono<ResponseEntity<Page<D>>> paginate(@RequestBody ConditionGroup conditionGroup,
                                                  @ModelAttribute CustomPageRequest pageRequest) {
        return wrapBlocking(() -> getService().paginate(buildSearchGroup(conditionGroup, pageRequest.getQuery()), pageRequest))
                .map(ResponseEntity::ok);
    }

    @PostMapping("export")
    public Mono<ResponseEntity<byte[]>> export(@RequestBody ConditionGroup conditionGroup) {
        return wrapBlocking(() -> getService().export(conditionGroup))
                .map(this::buildExportResponse);
    }

    // ============ Helpers protégés (réutilisables par les sous-classes) ============

    protected ConditionGroup buildSearchGroup(ConditionGroup conditionGroup, String query) {
        ConditionGroup searchGroup = ConditionGroup.builder()
                .operator(LogicalOperator.OR)
                .conditions(getSearchFields().stream().map( field ->
                            FilterRequest.builder().field(field).operation(SearchOperator.LIKE).value("%" + query + "%").build()
                        ).toList()
                )
                .build();
        return ConditionGroup.builder()
                .operator(LogicalOperator.AND)
                .groups(List.of(searchGroup, conditionGroup))
                .build();
    }

    protected ResponseEntity<byte[]> buildExportResponse(byte[] data) {
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=export.xlsx")
                .body(data);
    }

    protected <T> Mono<T> wrapBlocking(Supplier<T> supplier) {
        return Mono.fromCallable(supplier::get)
                .subscribeOn(Schedulers.boundedElastic());
    }

    protected Mono<Void> wrapBlockingVoid(Runnable runnable) {
        return Mono.fromRunnable(runnable)
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}