package com.dglab.cia;

import com.dglab.cia.json.util.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.FileUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


/**
 * @author doc
 */

@Configuration
@EnableTransactionManagement
@EnableScheduling
@ComponentScan({"com.dglab.cia.persistence"})
public class PersistenceConfig {
	@Bean
	@Profile("readWrite")
	public DataSource readWriteDataSource() throws IOException {
        HikariDataSource dataSource = new HikariDataSource();
        String password = FileUtils.readFileToString(new File("private.key"));

        dataSource.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        dataSource.addDataSourceProperty("user", "cia");
        dataSource.addDataSourceProperty("password", password);
        dataSource.addDataSourceProperty("databaseName", "ciadb");
        dataSource.addDataSourceProperty("portNumber", "5432");
        dataSource.addDataSourceProperty("serverName", "127.0.0.1");

		return dataSource;
	}

	@Bean
	@Autowired
	public EntityManagerFactory createEntityManagerFactory(
			DataSource dataSource, @Qualifier("dataSourceProperties") Properties properties
	) {
		LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
		bean.setDataSource(dataSource);
		bean.setPackagesToScan("com.dglab.cia.database");
		bean.setJpaDialect(new HibernateJpaDialect());
		bean.setJpaProperties(properties);
		bean.setPersistenceProvider(new HibernatePersistenceProvider());
		bean.afterPropertiesSet();

		return bean.getNativeEntityManagerFactory();
	}

	@Bean
	@Autowired
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Bean(name = "dataSourceProperties")
	@Profile("readWrite")
	public Properties readWriteProperties() {
		return new Properties() {
			{
				setProperty("hibernate.hbm2ddl.auto", "update");
				setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL94Dialect");
			}
		};
	}

	@Bean(name = "dataSourceProperties")
	@Profile("read")
	public Properties readProperties() {
		return new Properties() {
			{
				setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL94Dialect");
			}
		};
	}

	@Bean
	public ObjectMapper createObjectMapper() {
		return ObjectMapperFactory.createObjectMapper();
	}

	@Bean
	public JsonUtil createJsonUtil() {
		return new JsonUtil();
	}
}
