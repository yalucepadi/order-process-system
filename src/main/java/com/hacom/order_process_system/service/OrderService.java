package com.hacom.order_process_system.service;

import com.hacom.order_process_system.model.Order;
import com.hacom.order_process_system.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final Counter orderReceivedCounter;
    private final Counter orderProcessedCounter;
    @Autowired
    private OrderRepository orderRepository;

    public OrderService(Counter orderReceivedCounter, Counter orderProcessedCounter) {
        this.orderReceivedCounter = orderReceivedCounter;
        this.orderProcessedCounter = orderProcessedCounter;
    }

    public Mono<Order> findByOrderId(String orderId) {
        logger.debug("Finding order by ID: {}", orderId);
        return orderRepository.findByOrderId(orderId);
    }

    public Mono<Long> countOrdersByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        logger.debug("Counting orders between {} and {}", startDate, endDate);
        return orderRepository.findByTsBetween(startDate, endDate).count();
    }

    public void receiveOrder(Order order) {
        // Simula recepción de orden
        orderReceivedCounter.increment(); // <--- Incrementas aquí

        // Simula procesamiento de orden
        // ...
        orderProcessedCounter.increment(); // <--- Y aquí también
    }

}

