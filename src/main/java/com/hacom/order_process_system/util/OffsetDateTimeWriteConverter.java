package com.hacom.order_process_system.util;

import org.springframework.core.convert.converter.Converter;

import org.springframework.data.convert.WritingConverter;

import java.time.Instant;
import java.time.OffsetDateTime;

@WritingConverter
public class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Instant> {
    @Override
    public Instant convert(OffsetDateTime source) {
        return source != null ? source.toInstant() : null;
    }
}
