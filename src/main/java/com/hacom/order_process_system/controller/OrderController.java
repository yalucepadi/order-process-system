package com.hacom.order_process_system.controller;

import com.hacom.order_process_system.model.Order;
import com.hacom.order_process_system.repository.OrderRepository;
import com.hacom.order_process_system.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;
    @Autowired
    private OrderService orderService;

    @Autowired
    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{orderId}/status")
    public Mono<ResponseEntity<Map<String, String>>> findByOrderId(@PathVariable String orderId) {
        logger.debug("Finding order by ID: {}", orderId);
        return orderService.findByOrderId(orderId)
                .map(order -> {
                    logger.info("Found order: {} with status: {}", orderId, order.getStatus());
                    return ResponseEntity.ok(Map.of(
                            "orderId", order.getOrderId(),
                            "status", order.getStatus(),
                            "timestamp", order.getTs().toString()

                    ));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    logger.error("Error retrieving order with ID {}: {}", orderId, error.getMessage(), error);
                    return Mono.just(ResponseEntity.internalServerError().body(
                            Map.of("error", "Internal server error", "details", error.getMessage())
                    ));
                });
    }


    @GetMapping("/count")
    public Mono<ResponseEntity<Map<String, ? extends Serializable>>> getOrderCountByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        logger.info("Getting order count between {} and {}", startDate, endDate);

        return orderRepository.findByTsBetween(startDate, endDate)
                .count()
                .doOnError(error -> logger.error("Error getting order count: {}", error.getMessage()))
                .map(count -> {
                    logger.info("Found {} orders between {} and {}", count, startDate, endDate);
                    return ResponseEntity.ok(Map.of(
                            "startDate", startDate.toString(),
                            "endDate", endDate.toString(),
                            "totalOrders", count
                    ));
                });
    }

    }