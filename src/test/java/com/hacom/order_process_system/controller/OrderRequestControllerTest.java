package com.hacom.order_process_system.controller;

import com.hacom.order_process_system.model.request.OrderRequest;
import com.hacom.order_process_system.repository.OrderRepository;
import com.hacom.order_process_system.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderRequestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderServiceImpl orderServiceImpl;

    @Test
    void contextLoads() {
        // Verify that the Spring context loads correctly
    }

    @Test
    void findByOrderId_IntegrationTest_ShouldWorkEndToEnd() {
        // Given
        String orderId = "INTEGRATION-TEST-ORDER";
        OffsetDateTime timestamp = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        OrderRequest mockOrderRequest = createMockOrder(orderId, "PROCESSING", timestamp);

        when(orderServiceImpl.findByOrderId(orderId)).thenReturn(Mono.just(mockOrderRequest));

        // When & Then
        webTestClient.get()
                .uri("/api/orders/{orderId}/status", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId)
                .jsonPath("$.status").isEqualTo("PROCESSING")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void getOrderCountByDateRange_IntegrationTest_ShouldWorkEndToEnd() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endDate = OffsetDateTime.of(2024, 1, 31, 23, 59, 59, 0, ZoneOffset.UTC);

        Flux<OrderRequest> mockOrders = Flux.just(
                createMockOrder("ORDER-1", "COMPLETED", startDate.plusDays(1)),
                createMockOrder("ORDER-2", "PENDING", startDate.plusDays(5)),
                createMockOrder("ORDER-3", "SHIPPED", startDate.plusDays(10))
        );

        when(orderRepository.findByTsBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(mockOrders);

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/orders/count")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.totalOrders").isEqualTo(3)
                .jsonPath("$.startDate").exists()
                .jsonPath("$.endDate").exists();
    }

    @Test
    void getAllEndpoints_ShouldRequireProperParameters() {
        // Test that endpoints validate required parameters
        webTestClient.get()
                .uri("/api/orders/count")
                .exchange()
                .expectStatus().isBadRequest();
    }

    private OrderRequest createMockOrder(String orderId, String status, OffsetDateTime timestamp) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrderId(orderId);
        orderRequest.setStatus(status);
        orderRequest.setTs(timestamp);
        return orderRequest;
    }
}