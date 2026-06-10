package com.krystianwoloszun.inv360.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateInventoryRequest(
        @NotNull @PositiveOrZero Integer quantity) {
}
