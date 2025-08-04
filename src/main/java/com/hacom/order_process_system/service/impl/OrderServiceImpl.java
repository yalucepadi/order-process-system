package com.hacom.order_process_system.service.impl;

import com.hacom.order_process_system.model.request.OrderRequest;
import com.hacom.order_process_system.repository.OrderRepository;
import com.hacom.order_process_system.service.OrderService;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final Counter orderReceivedCounter;
    private final Counter orderProcessedCounter;
    @Autowired
    private OrderRepository orderRepository;

    public OrderServiceImpl(Counter orderReceivedCounter, Counter orderProcessedCounter) {
        this.orderReceivedCounter = orderReceivedCounter;
        this.orderProcessedCounter = orderProcessedCounter;
    }

    @Override
    public Mono<OrderRequest> findByOrderId(String orderId) {
        logger.debug("Finding order by ID: {}", orderId);
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public Mono<Long> countOrdersByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        logger.debug("Counting orders between {} and {}", startDate, endDate);
        return orderRepository.findByTsBetween(startDate, endDate).count();
    }
    @Override
    public void receiveOrder(OrderRequest orderRequest) {
        orderReceivedCounter.increment();

        orderProcessedCounter.increment();
    }

}

