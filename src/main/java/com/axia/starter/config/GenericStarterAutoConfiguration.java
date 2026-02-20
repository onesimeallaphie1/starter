package com.axia.starter.config;

import com.axia.starter.export.Exporter;
import com.axia.starter.export.PoiExporter;
import com.axia.starter.specification.GenericSpecificationBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class GenericStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GenericSpecificationBuilder<?> genericSpecificationBuilder() {
        return new GenericSpecificationBuilder<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public Exporter<?> defaultExporter() {
        return new PoiExporter<>(Object.class);
    }

    // On ne peut pas créer un bean pour GenericService car il est abstrait
    // C'est à l'utilisateur de l'étendre, donc pas de bean automatique.
}