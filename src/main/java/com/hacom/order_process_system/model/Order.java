package com.hacom.order_process_system.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;

@Document(collection = "orders")
public class Order {
    @Id
    private ObjectId _id;
    private String orderId;
    private String customerId;
    private String customerPhoneNumber;
    private String status;
    private List<String> items;
    private OffsetDateTime ts;

    public Order() {}

    public Order(String orderId, String customerId, String customerPhoneNumber,
                 String status, List<String> items, OffsetDateTime ts) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerPhoneNumber = customerPhoneNumber;
        this.status = status;
        this.items = items;
        this.ts = ts;
    }

    // Getters and Setters
    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public OffsetDateTime getTs() {
        return ts;
    }

    public void setTs(OffsetDateTime ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "Order{" +
                "_id=" + _id +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerPhoneNumber='" + customerPhoneNumber + '\'' +
                ", status='" + status + '\'' +
                ", items=" + items +
                ", ts=" + ts +
                '}';
    }
}