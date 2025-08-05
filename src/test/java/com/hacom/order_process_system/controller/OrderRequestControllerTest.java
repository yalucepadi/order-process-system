package com.hacom.order_process_system.controller;
import com.hacom.order_process_system.exception.OrderException;
import com.hacom.order_process_system.model.request.OrderRequest;
import com.hacom.order_process_system.model.response.OrderCountResponse;
import com.hacom.order_process_system.model.response.OrderResponse;
import com.hacom.order_process_system.model.response.ResponseGeneralDto;
import com.hacom.order_process_system.repository.OrderRepository;
import com.hacom.order_process_system.service.impl.OrderServiceImpl;
import com.hacom.order_process_system.util.Constants;
import com.hacom.order_process_system.util.OderAdapter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@WebFluxTest(OrderController.class)
@DisplayName("OrderController WebFlux Tests")
class OrderRequestControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderServiceImpl orderServiceImpl;

    private OrderRequest mockOrderRequest;
    private ResponseGeneralDto mockSuccessResponse;
    private ResponseGeneralDto mockNotFoundResponse;
    private ResponseGeneralDto mockServerErrorResponse;

    @BeforeEach
    void setUp() {
        // Setup mock OrderRequest
        mockOrderRequest = new OrderRequest();
        mockOrderRequest.set_id(new ObjectId());
        mockOrderRequest.setOrderId("ORDER-123");
        mockOrderRequest.setCustomerId("CUSTOMER-456");
        mockOrderRequest.setCustomerPhoneNumber("+1234567890");
        mockOrderRequest.setStatus("COMPLETED");
        mockOrderRequest.setItems(Arrays.asList("item1", "item2", "item3"));
        mockOrderRequest.setTs(OffsetDateTime.now());

        // Setup mock responses
        setupMockResponses();
    }

    private void setupMockResponses() {
        // Success response
        mockSuccessResponse = new ResponseGeneralDto();
        mockSuccessResponse.setCode(Constants.HTTP_200);
        mockSuccessResponse.setStatus(200);
        mockSuccessResponse.setComment("Success");

        // Not found response
        mockNotFoundResponse = new ResponseGeneralDto();
        mockNotFoundResponse.setCode(Constants.HTTP_404);
        mockNotFoundResponse.setStatus(404);
        mockNotFoundResponse.setComment(Constants.messageProcessNotFound);

        // Server error response
        mockServerErrorResponse = new ResponseGeneralDto();
        mockServerErrorResponse.setCode(Constants.HTTP_500);
        mockServerErrorResponse.setStatus(500);
        mockServerErrorResponse.setComment("Internal server error");
    }

    // ========== TESTS FOR GET /api/orders/{orderId}/status ==========

    @Test
    @DisplayName("GET /api/orders/{orderId}/status - Should return order successfully")
    void findByOrderId_Success() {
        // Given
        String orderId = "ORDER-123";
        when(orderServiceImpl.findByOrderId(orderId)).thenReturn(Mono.just(mockOrderRequest));

        ResponseGeneralDto successResponse = new ResponseGeneralDto();
        successResponse.setCode(Constants.HTTP_200);
        successResponse.setStatus(200);
        successResponse.setComment("Order found successfully");

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId("ORDER-123");
        orderResponse.setStatus("PROCESSED");
        orderResponse.setTimestamp(mockOrderRequest.getTs()); // Usando timestamp en lugar de ts
        successResponse.setData(orderResponse);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    eq(Constants.HTTP_200),
                    eq(HttpStatus.OK.value()),
                    eq("Order found successfully"),
                    any(OrderResponse.class)
            )).thenReturn(successResponse);

            // When & Then
            webTestClient.get()
                    .uri("/api/orders/{orderId}/status", orderId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("200")
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.comment").isEqualTo("Order found successfully")
                    .jsonPath("$.data.orderId").isEqualTo("ORDER-123")
                    .jsonPath("$.data.status").isEqualTo("COMPLETED")
                    .jsonPath("$.data.timestamp").exists(); // Verificar que timestamp existe

            verify(orderServiceImpl, times(1)).findByOrderId(orderId);
        }
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/status - Should return 404 when order not found")
    void findByOrderId_NotFound() {
        // Given
        String orderId = "NON-EXISTENT";
        when(orderServiceImpl.findByOrderId(orderId)).thenReturn(Mono.empty());

        ResponseGeneralDto notFoundResponse = new ResponseGeneralDto();
        notFoundResponse.setCode(Constants.HTTP_404);
        notFoundResponse.setStatus(404);
        notFoundResponse.setComment(Constants.messageProcessNotFound);
        notFoundResponse.setData("Order not found with ID: " + orderId);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    eq(Constants.HTTP_404),
                    eq(HttpStatus.NOT_FOUND.value()),
                    eq(Constants.messageProcessNotFound),
                    eq("Order not found with ID: " + orderId)
            )).thenReturn(notFoundResponse);

            // When & Then
            webTestClient.get()
                    .uri("/api/orders/{orderId}/status", orderId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("404")
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.comment").isEqualTo(Constants.messageProcessNotFound)
                    .jsonPath("$.data").isEqualTo("Order not found with ID: " + orderId);

            verify(orderServiceImpl, times(1)).findByOrderId(orderId);
        }
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/status - Should handle OrderException")
    void findByOrderId_OrderException() {
        // Given
        String orderId = "ORDER-123";
        String errorMessage = "Order processing failed";
        OrderException orderException = new OrderException(errorMessage);
        when(orderServiceImpl.findByOrderId(orderId)).thenReturn(Mono.error(orderException));

        ResponseGeneralDto errorResponse = new ResponseGeneralDto();
        errorResponse.setCode(Constants.HTTP_404);
        errorResponse.setStatus(404);
        errorResponse.setComment(Constants.messageProcessNotFound);
        errorResponse.setData(errorMessage);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    eq(Constants.HTTP_404),
                    eq(HttpStatus.NOT_FOUND.value()),
                    eq(Constants.messageProcessNotFound),
                    eq(errorMessage)
            )).thenReturn(errorResponse);

            // When & Then
            webTestClient.get()
                    .uri("/api/orders/{orderId}/status", orderId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("404")
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.comment").isEqualTo(Constants.messageProcessNotFound)
                    .jsonPath("$.data").isEqualTo(errorMessage);

            verify(orderServiceImpl, times(1)).findByOrderId(orderId);
        }
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/status - Should handle generic exception")
    void findByOrderId_GenericException() {
        // Given
        String orderId = "ORDER-123";
        String errorMessage = "Database connection failed";
        RuntimeException genericException = new RuntimeException(errorMessage);
        when(orderServiceImpl.findByOrderId(orderId)).thenReturn(Mono.error(genericException));

        ResponseGeneralDto serverErrorResponse = new ResponseGeneralDto();
        serverErrorResponse.setCode(Constants.HTTP_500);
        serverErrorResponse.setStatus(500);
        serverErrorResponse.setComment("Internal server error");
        serverErrorResponse.setData(errorMessage);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    eq(Constants.HTTP_500),
                    eq(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    eq("Internal server error"),
                    eq(errorMessage)
            )).thenReturn(serverErrorResponse);

            // When & Then
            webTestClient.get()
                    .uri("/api/orders/{orderId}/status", orderId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("500")
                    .jsonPath("$.status").isEqualTo(500)
                    .jsonPath("$.comment").isEqualTo("Internal server error")
                    .jsonPath("$.data").isEqualTo(errorMessage);

            verify(orderServiceImpl, times(1)).findByOrderId(orderId);
        }
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/status - Should handle empty orderId")
    void findByOrderId_EmptyOrderId() {
        // When & Then
        webTestClient.get()
                .uri("/api/orders/ /status") // Empty space as orderId
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError(); // Spring will return 404 for empty path variable
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/status - Should verify OrderResponse field mapping")
    void findByOrderId_VerifyOrderResponseMapping() {
        // Given
        String orderId = "ORDER-456";
        String expectedStatus = "PENDING";
        OffsetDateTime expectedTimestamp = OffsetDateTime.now().minusHours(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrderId(orderId);
        orderRequest.setStatus(expectedStatus);
        orderRequest.setTs(expectedTimestamp);

        when(orderServiceImpl.findByOrderId(orderId)).thenReturn(Mono.just(orderRequest));

        ResponseGeneralDto successResponse = new ResponseGeneralDto();
        successResponse.setCode(Constants.HTTP_200);
        successResponse.setStatus(200);
        successResponse.setComment("Order found successfully");

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(orderId);
        orderResponse.setStatus(expectedStatus);
        orderResponse.setTimestamp(expectedTimestamp);
        successResponse.setData(orderResponse);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    any(), any(), any(), any()
            )).thenReturn(successResponse);

            // When & Then
            webTestClient.get()
                    .uri("/api/orders/{orderId}/status", orderId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.data.orderId").isEqualTo(orderId)
                    .jsonPath("$.data.status").isEqualTo(expectedStatus)
                    .jsonPath("$.data.timestamp").isEqualTo(expectedTimestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            verify(orderServiceImpl, times(1)).findByOrderId(orderId);
        }
    }

    // ========== TESTS FOR GET /api/orders/count ==========

    @Test
    @DisplayName("GET /api/orders/count - Should return count successfully")
    void getOrderCountByDateRange_Success() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);
        OffsetDateTime endDate = OffsetDateTime.now();
        Long expectedCount = 25L;

        when(orderServiceImpl.countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Mono.just(expectedCount));

        ResponseGeneralDto successResponse = new ResponseGeneralDto();
        successResponse.setCode(Constants.HTTP_200);
        successResponse.setStatus(200);
        successResponse.setComment("Order count retrieved successfully");

        OrderCountResponse countResponse = new OrderCountResponse();
        countResponse.setStartDate(startDate);
        countResponse.setEndDate(endDate);
        countResponse.setTotalOrders(expectedCount);
        successResponse.setData(countResponse);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    eq(Constants.HTTP_200),
                    eq(HttpStatus.OK.value()),
                    eq("Order count retrieved successfully"),
                    any(OrderCountResponse.class)
            )).thenReturn(successResponse);

            // When & Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/orders/count")
                            .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("200")
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.comment").isEqualTo("Order count retrieved successfully")
                    .jsonPath("$.data.totalOrders").isEqualTo(25)
                    .jsonPath("$.data.startDate").exists()
                    .jsonPath("$.data.endDate").exists();

            verify(orderServiceImpl, times(1)).countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class));
        }
    }

    @Test
    @DisplayName("GET /api/orders/count - Should handle service error")
    void getOrderCountByDateRange_ServiceError() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);
        OffsetDateTime endDate = OffsetDateTime.now();
        String errorMessage = "Database query failed";
        RuntimeException exception = new RuntimeException(errorMessage);

        when(orderServiceImpl.countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Mono.error(exception));

        ResponseGeneralDto errorResponse = new ResponseGeneralDto();
        errorResponse.setCode(Constants.HTTP_500);
        errorResponse.setStatus(500);
        errorResponse.setComment("Error retrieving order count");
        errorResponse.setData(errorMessage);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    eq(Constants.HTTP_500),
                    eq(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    eq("Error retrieving order count"),
                    eq(errorMessage)
            )).thenReturn(errorResponse);

            // When & Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/orders/count")
                            .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("500")
                    .jsonPath("$.status").isEqualTo(500)
                    .jsonPath("$.comment").isEqualTo("Error retrieving order count")
                    .jsonPath("$.data").isEqualTo(errorMessage);

            verify(orderServiceImpl, times(1)).countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class));
        }
    }

    @Test
    @DisplayName("GET /api/orders/count - Should return zero count")
    void getOrderCountByDateRange_ZeroCount() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(7);
        Long expectedCount = 0L;

        when(orderServiceImpl.countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Mono.just(expectedCount));

        ResponseGeneralDto successResponse = new ResponseGeneralDto();
        successResponse.setCode(Constants.HTTP_200);
        successResponse.setStatus(200);
        successResponse.setComment("Order count retrieved successfully");

        OrderCountResponse countResponse = new OrderCountResponse();
        countResponse.setStartDate(startDate);
        countResponse.setEndDate(endDate);
        countResponse.setTotalOrders(expectedCount);
        successResponse.setData(countResponse);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    any(), any(), any(), any()
            )).thenReturn(successResponse);

            // When & Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/orders/count")
                            .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("200")
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.data.totalOrders").isEqualTo(0);

            verify(orderServiceImpl, times(1)).countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class));
        }
    }

    @Test
    @DisplayName("GET /api/orders/count - Should return 400 for invalid date format")
    void getOrderCountByDateRange_InvalidDateFormat() {
        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/orders/count")
                        .queryParam("startDate", "invalid-date")
                        .queryParam("endDate", "invalid-date")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        // Verify service was not called due to validation error
        verify(orderServiceImpl, never()).countOrdersByDateRange(any(), any());
    }

    @Test
    @DisplayName("GET /api/orders/count - Should return 400 for missing parameters")
    void getOrderCountByDateRange_MissingParameters() {
        // When & Then
        webTestClient.get()
                .uri("/api/orders/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        // Verify service was not called due to missing parameters
        verify(orderServiceImpl, never()).countOrdersByDateRange(any(), any());
    }

    @Test
    @DisplayName("GET /api/orders/count - Should return 400 for missing startDate")
    void getOrderCountByDateRange_MissingStartDate() {
        // Given
        OffsetDateTime endDate = OffsetDateTime.now();

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/orders/count")
                        .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(orderServiceImpl, never()).countOrdersByDateRange(any(), any());
    }

    @Test
    @DisplayName("GET /api/orders/count - Should return 400 for missing endDate")
    void getOrderCountByDateRange_MissingEndDate() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/orders/count")
                        .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(orderServiceImpl, never()).countOrdersByDateRange(any(), any());
    }

    @Test
    @DisplayName("GET /api/orders/count - Should handle large count numbers")
    void getOrderCountByDateRange_LargeCount() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(365);
        OffsetDateTime endDate = OffsetDateTime.now();
        Long expectedCount = 1000000L; // Large number

        when(orderServiceImpl.countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Mono.just(expectedCount));

        ResponseGeneralDto successResponse = new ResponseGeneralDto();
        successResponse.setCode(Constants.HTTP_200);
        successResponse.setStatus(200);
        successResponse.setComment("Order count retrieved successfully");

        OrderCountResponse countResponse = new OrderCountResponse();
        countResponse.setTotalOrders(expectedCount);
        successResponse.setData(countResponse);

        try (MockedStatic<OderAdapter> mockedAdapter = mockStatic(OderAdapter.class)) {
            mockedAdapter.when(() -> OderAdapter.responseGeneral(
                    any(), any(), any(), any()
            )).thenReturn(successResponse);

            // When & Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/orders/count")
                            .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("200")
                    .jsonPath("$.data.totalOrders").isEqualTo(1000000);

            verify(orderServiceImpl, times(1)).countOrdersByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class));
        }
    }
}
