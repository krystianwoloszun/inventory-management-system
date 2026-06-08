package com.krystianwoloszun.inv360.warehouse.dto;

public record WarehouseResponse(

        String name,

        String street,
        String buildingNumber,
        String city,
        String postalCode

) {
}