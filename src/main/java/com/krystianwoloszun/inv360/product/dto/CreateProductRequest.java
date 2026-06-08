package com.krystianwoloszun.inv360.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 1000) String description,
        @NotBlank @Size(max = 100) String sku,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal price

) {
}
