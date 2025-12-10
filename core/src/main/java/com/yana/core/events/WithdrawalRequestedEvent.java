package com.yana.core.events;

import java.math.BigDecimal;

public record WithdrawalRequestedEvent(
        String senderId,
        String recepientId,
        BigDecimal amount
) {
}
