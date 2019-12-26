package com.astel.security.main;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.util.ClassUtils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

@Log4j
@ConditionalOnClass(DataSource.class)
@PropertySource("classpath:h2.properties")
public class DbDefaultConfigurration {
    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnProperty(
            name = "useh2",
            havingValue = "local")
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String username = "dbuser";
        String password = randomString();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:h2:file:~/db");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        log.info("Database: username: " + username + "password: " + password);

        return dataSource;
    }

    @Bean
    @ConditionalOnBean(name = "dataSource")
    @ConditionalOnMissingBean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.astel.security.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        if (additionalProperties() != null) {
            em.setJpaProperties(additionalProperties());
        }
        return em;
    }

    @Bean
    @ConditionalOnMissingBean(type = "JpaTransactionManager")
    JpaTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @ConditionalOnResource(resources = "classpath:mysql.properties")
    @Conditional(HibernateCondition.class)
    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("h2-hibernate.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("h2-hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("h2-hibernate.show_sql") != null ? env.getProperty("h2-hibernate.show_sql") : "false");

        return hibernateProperties;
    }

    static class HibernateCondition extends SpringBootCondition {

        private static final String[] CLASS_NAMES = { "org.hibernate.ejb.HibernateEntityManager", "org.hibernate.jpa.HibernateEntityManager" };

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition("Hibernate");

            return Arrays.stream(CLASS_NAMES)
                    .filter(className -> ClassUtils.isPresent(className, context.getClassLoader()))
                    .map(className -> ConditionOutcome.match(message.found("class")
                            .items(ConditionMessage.Style.NORMAL, className)))
                    .findAny()
                    .orElseGet(() -> ConditionOutcome.noMatch(message.didNotFind("class", "classes")
                            .items(ConditionMessage.Style.NORMAL, Arrays.asList(CLASS_NAMES))));
        }
    }

    private String randomString() {
        byte[] array = new byte[20];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }
}
