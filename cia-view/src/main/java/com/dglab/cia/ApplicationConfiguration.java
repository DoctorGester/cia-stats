package com.dglab.cia;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * User: kartemov
 * Date: 25.10.2016
 * Time: 19:11
 */
@Configuration
@ComponentScan("com.dglab.cia")
public class ApplicationConfiguration {
    @Value("${server.http.port}")
    private int httpPort;

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return container -> {
            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory containerFactory =
                        (TomcatEmbeddedServletContainerFactory) container;

                Connector connector = new Connector(TomcatEmbeddedServletContainerFactory.DEFAULT_PROTOCOL);
                connector.setPort(httpPort);
                containerFactory.addAdditionalTomcatConnectors(connector);
            }
        };
    }
}
