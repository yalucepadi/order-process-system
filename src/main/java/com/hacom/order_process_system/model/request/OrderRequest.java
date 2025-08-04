package com.hacom.order_process_system.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class OrderRequest {
    @Id
    private ObjectId _id;
    private String orderId;
    private String customerId;
    private String customerPhoneNumber;
    private String status;
    private List<String> items;
    private OffsetDateTime ts;

    public OrderRequest(String orderId, String customerId, String customerPhoneNumber,
                        String status, List<String> items, OffsetDateTime ts) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerPhoneNumber = customerPhoneNumber;
        this.status = status;
        this.items = items;
        this.ts = ts;
    }


}