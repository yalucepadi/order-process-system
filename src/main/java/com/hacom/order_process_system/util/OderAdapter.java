package com.hacom.order_process_system.util;


import com.hacom.order_process_system.model.response.ResponseGeneralDto;

public class OderAdapter {
    public static ResponseGeneralDto responseGeneral(String code, Integer status, String message, Object data) {
        return ResponseGeneralDto.builder()
                .status(status)
                .code(code)
                .comment(message)
                .data(data)
                .build();



    }}
