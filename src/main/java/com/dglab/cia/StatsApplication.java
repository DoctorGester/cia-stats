package com.dglab.cia;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static spark.Spark.port;
import static spark.Spark.threadPool;

/**
 * @author doc
 */
public class StatsApplication {
	private AnnotationConfigApplicationContext context;

	public StatsApplication() {
		port(80);
		threadPool(16);

		context = new AnnotationConfigApplicationContext();
		context.getEnvironment().setActiveProfiles("read");
		context.register(PersistenceConfig.class);
		context.refresh();

	}
}
