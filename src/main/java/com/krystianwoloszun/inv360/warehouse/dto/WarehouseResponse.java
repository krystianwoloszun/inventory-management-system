package com.krystianwoloszun.inv360.warehouse.dto;

public record WarehouseResponse(

        Long id,
        String name,
        String description,
        AddressResponse address

) {
}