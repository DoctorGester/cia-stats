package com.dglab.cia;

import com.dglab.cia.persistence.MatchService;
import com.dglab.cia.persistence.MatchServiceImpl;
import com.dglab.cia.persistence.PlayerNameService;
import com.dglab.cia.persistence.PlayerNameServiceImpl;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;


/**
 * @author doc
 */

@Configuration
@EnableTransactionManagement
@ComponentScan({"com.dglab.cia.persistence"})
public class PersistenceConfig {
	@Bean
	@Profile("readWrite")
	public DataSource readWriteDataSource() {
		HikariDataSource dataSource = new HikariDataSource();

		dataSource.setDataSourceClassName("org.hsqldb.jdbc.JDBCDataSource");
		dataSource.addDataSourceProperty("url", "file:data/database;shutdown=true;hsqldb.write_delay=false;");
		dataSource.addDataSourceProperty("user", "sa");
		dataSource.addDataSourceProperty("password", "");

		return dataSource;
	}

	@Bean
	@Profile("read")
	public DataSource readDataSource() {
		HikariDataSource dataSource = new HikariDataSource();

		dataSource.setDataSourceClassName("org.hsqldb.jdbc.JDBCDataSource");
		dataSource.addDataSourceProperty("url", "file:data/database;shutdown=true;readonly=true;");
		dataSource.addDataSourceProperty("user", "sa");
		dataSource.addDataSourceProperty("password", "");
		dataSource.setReadOnly(true);

		return dataSource;
	}

	@Bean
	@Autowired
	public LocalSessionFactoryBean sessionFactory(
			DataSource dataSource, @Qualifier("dataSourceProperties") Properties properties
	) {
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		sessionFactory.setPackagesToScan("com.dglab.cia.database");
		sessionFactory.setHibernateProperties(properties);

		return sessionFactory;
	}

	@Bean
	@Autowired
	public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
		return new HibernateTransactionManager(sessionFactory);
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
				setProperty("hibernate.show_sql", "true");
				setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
			}
		};
	}

	@Bean(name = "dataSourceProperties")
	@Profile("read")
	public Properties readProperties() {
		return new Properties() {
			{
				setProperty("hibernate.show_sql", "true");
				setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
			}
		};
	}

	@Bean
	public MatchService createMatchService() {
		return new MatchServiceImpl();
	}

	@Bean
	public PlayerNameService createPlayerNameService() {
		return new PlayerNameServiceImpl();
	}

	@Bean
	public ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

		return mapper;
	}

	@Bean
	public JsonUtil createJsonUtil() {
		return new JsonUtil();
	}
}
