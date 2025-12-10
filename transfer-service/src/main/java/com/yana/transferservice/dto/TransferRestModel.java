package com.yana.transferservice.dto;

import java.math.BigDecimal;

public record TransferRestModel(
        String senderId,
        String recepientId,
        BigDecimal amount
) {
}
