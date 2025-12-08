package com.yana.productmicroservice.dto;

import java.util.Date;

public record ErrorMessage(
        Date timestamp,
        String message
) {
}
