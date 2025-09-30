package com.suprice.suprice.configuracion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.HsqlDbDialect;

@Configuration
public class ConfiguracionSqliteDialect {

    @Bean
    @ConditionalOnMissingBean(Dialect.class)
    @SuppressWarnings("removal")
    public Dialect sqliteDialect() {
        return HsqlDbDialect.INSTANCE;
    }
}
