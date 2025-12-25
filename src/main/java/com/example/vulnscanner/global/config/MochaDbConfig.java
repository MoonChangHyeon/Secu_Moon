package com.example.vulnscanner.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.vulnscanner.module.mocha.repository", entityManagerFactoryRef = "mochaEntityManagerFactory", transactionManagerRef = "mochaTransactionManager")
public class MochaDbConfig {

    @Bean(name = "mochaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mocha")
    public DataSource mochaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mochaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mochaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mochaDataSource") DataSource mochaDataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update"); // Or validate/none since it's read-only mostly? But user
                                                            // said OK to update.
        properties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

        return builder
                .dataSource(mochaDataSource)
                .packages("com.example.vulnscanner.module.mocha.entity")
                .persistenceUnit("mocha")
                .properties(properties)
                .build();
    }

    @Bean(name = "mochaTransactionManager")
    public PlatformTransactionManager mochaTransactionManager(
            @Qualifier("mochaEntityManagerFactory") LocalContainerEntityManagerFactoryBean mochaEntityManagerFactory) {
        return new JpaTransactionManager(mochaEntityManagerFactory.getObject());
    }
}