package com.yana.kafkajavaguru.dto;

import java.util.Date;

public record ErrorMessage(
        Date timestamp,
        String message
) {
}
