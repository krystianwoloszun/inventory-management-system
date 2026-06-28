package com.krystianwoloszun.inv360.warehouse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class WarehouseRepositoryTest {

    @Autowired
    private WarehouseRepository warehouseRepository;

    private Warehouse persistWarehouse(String name) {
        return warehouseRepository.save(Warehouse.builder()
                .name(name)
                .description("desc")
                .address(Address.builder()
                        .street("Main St")
                        .buildingNumber("1")
                        .city("Warsaw")
                        .postalCode("00-001")
                        .build())
                .build());
    }

    @Test
    void findByName_returnsWarehouse() {
        persistWarehouse("Central");

        assertThat(warehouseRepository.findByName("Central")).isPresent();
        assertThat(warehouseRepository.findByName("Missing")).isEmpty();
    }

    @Test
    void existsByName() {
        persistWarehouse("Central");

        assertThat(warehouseRepository.existsByName("Central")).isTrue();
        assertThat(warehouseRepository.existsByName("North")).isFalse();
    }

    @Test
    void embeddedAddressIsPersisted() {
        Warehouse saved = persistWarehouse("Central");

        Warehouse found = warehouseRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getAddress().getCity()).isEqualTo("Warsaw");
        assertThat(found.getAddress().getPostalCode()).isEqualTo("00-001");
    }
}
