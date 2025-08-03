package com.hacom.order_process_system.util;

import org.springframework.data.convert.ReadingConverter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.core.convert.converter.Converter;

@ReadingConverter
public class OffsetDateTimeReadConverter implements Converter<Instant, OffsetDateTime> {
    @Override
    public OffsetDateTime convert(Instant source) {
        return source != null ? source.atOffset(ZoneOffset.UTC) : null;
    }
}
