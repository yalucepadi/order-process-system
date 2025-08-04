package com.hacom.order_process_system.config;

import com.hacom.order_process_system.util.DateToOffsetDateTimeConverter;
import com.hacom.order_process_system.util.OffsetDateTimeToDateConverter;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${app.mongodb.database}")
    private String mongodbDatabase;

    @Value("${app.mongodb.uri}")
    private String mongodbUri;

    @Override
    protected String getDatabaseName() {
        logger.info("Configuring MongoDB database: {}", mongodbDatabase);
        return mongodbDatabase;
    }

    @Override
    @Bean
    public MongoClient reactiveMongoClient() {
        logger.info("Creating MongoDB client with URI: {}", mongodbUri);
        return MongoClients.create(mongodbUri);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        logger.info("Creating ReactiveMongoTemplate");
        return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = List.of(
                new DateToOffsetDateTimeConverter(),
                new OffsetDateTimeToDateConverter()
        );
        return new MongoCustomConversions(converters);
    }
}
