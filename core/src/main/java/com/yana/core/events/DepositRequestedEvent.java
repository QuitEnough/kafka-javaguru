package com.yana.core.events;

import java.math.BigDecimal;

public record DepositRequestedEvent(
        String sellerId,
        String recepientId,
        BigDecimal amount
) {
}
