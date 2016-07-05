package com.dglab.cia;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static spark.Spark.*;

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

		get("/index", (request, response) -> {
			return "Hello world!";
		});
	}
}
