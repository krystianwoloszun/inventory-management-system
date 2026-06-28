package com.krystianwoloszun.inv360.warehouse.dto;

import com.krystianwoloszun.inv360.warehouse.Address;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 1000) String description,
        @NotNull @Valid Address address)
        {
}
