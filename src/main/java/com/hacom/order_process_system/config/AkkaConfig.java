package com.hacom.order_process_system.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.hacom.order_process_system.actor.OrderProcessingActor;
import com.hacom.order_process_system.repository.OrderRepository;
import com.hacom.order_process_system.service.SmsService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AkkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(AkkaConfig.class);

    private ActorSystem actorSystem;

    @Bean
    public ActorSystem actorSystem() {
        logger.info("Creating Akka ActorSystem");
        actorSystem = ActorSystem.create("OrderProcessingSystem");
        return actorSystem;
    }

    @Bean
    public ActorRef orderProcessingActor(ActorSystem actorSystem, OrderRepository orderRepository, SmsService smsService) {
        logger.info("Creating OrderProcessingActor");
        return actorSystem.actorOf(OrderProcessingActor.props(orderRepository, smsService), "orderProcessingActor");
    }

    @PreDestroy
    public void destroy() {
        if (actorSystem != null) {
            logger.info("Terminating Akka ActorSystem");
            actorSystem.terminate();
        }
    }
}
