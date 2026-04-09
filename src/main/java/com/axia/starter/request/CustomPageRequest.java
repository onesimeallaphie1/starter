package com.axia.starter.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.Min;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomPageRequest {
    @Min(0)
    private int page = 0;
    @Min(1)
    private int size = 10;
    private String sortBy = "id";
    private Sort.Direction direction = Sort.Direction.ASC;
    public Pageable toPageable() {
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}