package com.axia.starter.config;

import com.axia.starter.export.Exporter;
import com.axia.starter.export.PoiExporter;
import com.axia.starter.specification.GenericSpecificationBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class GenericStarterAutoConfiguration {
    @Bean
    @ConditionalOnClass(name = "org.apache.poi.ss.usermodel.Workbook")
    public Exporter<?> defaultExporter() {
        return new PoiExporter<>(Object.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public GenericSpecificationBuilder<?> genericSpecificationBuilder() {
        return new GenericSpecificationBuilder<>();
    }

}