package com.hacom.order_process_system.controller;

import com.hacom.order_process_system.exception.OrderException;
import com.hacom.order_process_system.model.response.OrderCountResponse;
import com.hacom.order_process_system.model.response.OrderResponse;
import com.hacom.order_process_system.model.response.ResponseGeneralDto;
import com.hacom.order_process_system.repository.OrderRepository;
import com.hacom.order_process_system.service.impl.OrderServiceImpl;
import com.hacom.order_process_system.util.Constants;
import com.hacom.order_process_system.util.OderAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.OrderAdapter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    private OrderServiceImpl orderServiceImpl;

    @Autowired
    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @GetMapping("/{orderId}/status")
    public Mono<ResponseEntity<ResponseGeneralDto>> findByOrderId(@PathVariable String orderId) {
        logger.debug("Finding order by ID: {}", orderId);

        return orderServiceImpl.findByOrderId(orderId)
                .map(order -> {
                    logger.info("Found order: {} with status: {}", orderId, order.getStatus());

                    OrderResponse orderResponse = new OrderResponse();
                    orderResponse.setOrderId(order.getOrderId());
                    orderResponse.setStatus(order.getStatus());
                    orderResponse.setTimestamp(order.getTs());

                    ResponseGeneralDto response = OderAdapter.responseGeneral(
                            Constants.HTTP_200,
                            HttpStatus.OK.value(),
                            "Order found successfully",
                            orderResponse
                    );

                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(OderAdapter.responseGeneral(
                                Constants.HTTP_404,
                                HttpStatus.NOT_FOUND.value(),
                                Constants.messageProcessNotFound,
                                "Order not found with ID: " + orderId)))
                .onErrorResume(error -> {
                    logger.error("Error retrieving order with ID {}: {}", orderId, error.getMessage(), error);

                    // Manejo específico para OrderException
                    if (error instanceof OrderException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(OderAdapter.responseGeneral(
                                        Constants.HTTP_404,
                                        HttpStatus.NOT_FOUND.value(),
                                        Constants.messageProcessNotFound,
                                        error.getMessage())));
                    }

                    // Manejo para otras excepciones
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(OderAdapter.responseGeneral(
                                    Constants.HTTP_500,
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Internal server error",
                                    error.getMessage())));
                });
    }

    @GetMapping("/count")
    public Mono<ResponseEntity<ResponseGeneralDto>> getOrderCountByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        logger.info("Getting order count between {} and {}", startDate, endDate);

        return orderServiceImpl.countOrdersByDateRange(startDate, endDate)
                .map(count -> {
                    logger.info("Found {} orders between {} and {}", count, startDate, endDate);


                    OrderCountResponse countResponse = new OrderCountResponse();
                    countResponse.setStartDate(startDate);
                    countResponse.setEndDate(endDate);
                    countResponse.setTotalOrders(count);


                    ResponseGeneralDto response = OderAdapter.responseGeneral(
                            Constants.HTTP_200,
                            HttpStatus.OK.value(),
                            "Order count retrieved successfully",
                            countResponse
                    );

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    logger.error("Error getting order count: {}", error.getMessage(), error);

                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(OderAdapter.responseGeneral(
                                    Constants.HTTP_500,
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Error retrieving order count",
                                    error.getMessage())));
                });
    }

    }