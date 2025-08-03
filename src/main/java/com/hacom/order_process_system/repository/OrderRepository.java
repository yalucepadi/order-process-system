package com.hacom.order_process_system.repository;

import com.hacom.order_process_system.model.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, ObjectId> {

    Mono<Order> findByOrderId(String orderId);

    @Query("{ 'ts' : { $gte: ?0, $lte: ?1 } }")
    Flux<Order> findByTsBetween(OffsetDateTime startDate, OffsetDateTime endDate);
}