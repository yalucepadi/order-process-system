package com.hacom.order_process_system.service;

import com.hacom.order_process_system.model.request.OrderRequest;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface OrderService {

    Mono<OrderRequest> findByOrderId(String orderId);

    Mono<Long> countOrdersByDateRange(OffsetDateTime startDate, OffsetDateTime endDate);

    void receiveOrder(OrderRequest orderRequest);

}
