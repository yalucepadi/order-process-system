package com.hacom.order_process_system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFluxConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebFluxConfig.class);

    @Value("${app.api.port}")
    private int apiPort;

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> webServerFactoryCustomizer() {
        logger.info("Configuring WebFlux server port: {}", apiPort);
        return factory -> factory.setPort(apiPort);
    }
}