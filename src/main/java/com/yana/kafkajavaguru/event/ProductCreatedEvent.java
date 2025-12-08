package com.yana.kafkajavaguru.event;

import java.math.BigDecimal;

public record ProductCreatedEvent (
        String productId,
        String title,
        BigDecimal price,
        Integer quantity
) {
}
