package com.hacom.order_process_system.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.hacom.grpc.CreateOrderRequest;
import com.hacom.grpc.CreateOrderResponse;
import com.hacom.order_process_system.model.request.OrderRequest;
import com.hacom.order_process_system.repository.OrderRepository;
import com.hacom.order_process_system.service.proxy.sms.impl.SmsServiceImpl;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;

public class OrderProcessingActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingActor.class);

    private final OrderRepository orderRepository;
    private final SmsServiceImpl smsServiceImpl;
    private final Counter orderProcessedCounter;
    public static class ProcessOrderMessage {
        private final CreateOrderRequest request;
        private final StreamObserver<CreateOrderResponse> responseObserver;

        public ProcessOrderMessage(CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
            this.request = request;
            this.responseObserver = responseObserver;
        }

        public CreateOrderRequest getRequest() {
            return request;
        }

        public StreamObserver<CreateOrderResponse> getResponseObserver() {
            return responseObserver;
        }
    }

    public OrderProcessingActor(OrderRepository orderRepository, SmsServiceImpl smsServiceImpl, Counter orderProcessedCounter) {
        this.orderRepository = orderRepository;
        this.smsServiceImpl = smsServiceImpl;
        this.orderProcessedCounter = orderProcessedCounter;
    }

    public static Props props(OrderRepository orderRepository, SmsServiceImpl smsServiceImpl, Counter orderProcessedCounter) {
        return Props.create(OrderProcessingActor.class, () -> new OrderProcessingActor(orderRepository, smsServiceImpl,
                orderProcessedCounter));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProcessOrderMessage.class, this::processOrder)
                .build();
    }

    private void processOrder(ProcessOrderMessage message) {
        try {
            CreateOrderRequest request = message.getRequest();
            StreamObserver<CreateOrderResponse> responseObserver = message.getResponseObserver();

            logger.info("Processing order: {}", request.getOrderId());

            // Crear el objeto Order
            OrderRequest orderRequest = new OrderRequest(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getCustomerPhoneNumber(),
                    "PROCESSED",
                    List.copyOf(request.getItemsList()),
                    OffsetDateTime.now()
            );

            // Guardar en MongoDB
            orderRepository.save(orderRequest)
                    .subscribe(
                            savedOrder -> {
                                logger.info("Order saved successfully: {}", savedOrder.getOrderId());

                                orderProcessedCounter.increment();

                                // Enviar SMS
                                String smsMessage = "Your order " + request.getOrderId() + " has been processed";
                                smsServiceImpl.sendSms(request.getCustomerPhoneNumber(), smsMessage);

                                // Enviar respuesta gRPC
                                CreateOrderResponse response = CreateOrderResponse.newBuilder()
                                        .setOrderId(request.getOrderId())
                                        .setStatus("PROCESSED")
                                        .build();

                                responseObserver.onNext(response);
                                responseObserver.onCompleted();

                                logger.info("Order processing completed: {}", request.getOrderId());
                            },
                            error -> {
                                logger.error("Error saving order {}: {}", request.getOrderId(), error.getMessage());

                                responseObserver.onError(error);
                            }
                    );

        } catch (Exception e) {
            logger.error("Error processing order: {}", e.getMessage());
            message.getResponseObserver().onError(e);
        }
    }
}
