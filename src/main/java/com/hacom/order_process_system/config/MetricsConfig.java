package com.hacom.order_process_system.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {


    @Bean
    public Counter orderProcessedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("orders.processed")
                .description("Number of orders successfully processed")
                .register(meterRegistry);
    }
}
