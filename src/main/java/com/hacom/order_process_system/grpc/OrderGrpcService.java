package com.hacom.order_process_system.grpc;

import akka.actor.ActorRef;
import com.hacom.grpc.CreateOrderRequest;
import com.hacom.grpc.CreateOrderResponse;
import com.hacom.grpc.OrderServiceGrpc;
import com.hacom.order_process_system.actor.OrderProcessingActor;
import net.devh.boot.grpc.server.service.GrpcService;
import akka.actor.ActorRef;

import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(OrderGrpcService.class);

    private final ActorRef orderProcessingActor;
    private final Counter orderCounter;

    @Autowired
    public OrderGrpcService(ActorRef orderProcessingActor, MeterRegistry meterRegistry) {
        this.orderProcessingActor = orderProcessingActor;
        this.orderCounter = Counter.builder("orders.created")
                .description("Number of orders created")
                .register(meterRegistry);
    }

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
        logger.info("Received gRPC request to create order: {}", request.getOrderId());

        // Incrementar contador de Prometheus
        orderCounter.increment();

        // Enviar mensaje al actor para procesamiento
        OrderProcessingActor.ProcessOrderMessage message =
                new OrderProcessingActor.ProcessOrderMessage(request, responseObserver);

        orderProcessingActor.tell(message, ActorRef.noSender());

        logger.info("Order processing message sent to actor for order: {}", request.getOrderId());
    }
}