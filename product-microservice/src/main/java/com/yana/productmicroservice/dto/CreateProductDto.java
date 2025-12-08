package com.yana.productmicroservice.dto;

import java.math.BigDecimal;

public record CreateProductDto(
        String title,
        BigDecimal price,
        Integer quantity
) {
}
