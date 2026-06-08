package com.krystianwoloszun.inv360.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price
) {
}