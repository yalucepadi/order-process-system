package com.hacom.order_process_system.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;
    private String status;
    private OffsetDateTime timestamp;


}
