package com.hacom.order_process_system.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class OrderException extends RuntimeException {

    private String message;

    public OrderException(String message) {
        super(message);
        this.message = message;
    }


}
