package com.krystianwoloszun.inv360.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.krystianwoloszun.inv360.product.Product;
import com.krystianwoloszun.inv360.product.ProductRepository;
import com.krystianwoloszun.inv360.warehouse.Address;
import com.krystianwoloszun.inv360.warehouse.Warehouse;
import com.krystianwoloszun.inv360.warehouse.WarehouseRepository;

@DataJpaTest
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;

    private Product product;
    private Warehouse warehouse;

    private void setUpData() {
        product = productRepository.save(Product.builder()
                .name("Widget").description("d").sku("SKU-1").price(new BigDecimal("1.00")).build());
        warehouse = warehouseRepository.save(Warehouse.builder()
                .name("Central").description("d")
                .address(Address.builder().street("s").buildingNumber("1").city("c").postalCode("00-001").build())
                .build());
        inventoryRepository.save(Inventory.builder()
                .product(product).warehouse(warehouse).quantity(7).build());
    }

    @Test
    void findByProductIdAndWarehouseId_returnsInventory() {
        setUpData();

        assertThat(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .isPresent()
                .get()
                .extracting(Inventory::getQuantity)
                .isEqualTo(7);
    }

    @Test
    void findByProductIdAndWarehouseId_emptyWhenMissing() {
        setUpData();

        assertThat(inventoryRepository.findByProductIdAndWarehouseId(product.getId(), 999L)).isEmpty();
    }

    @Test
    void existsByProductIdAndWarehouseId() {
        setUpData();

        assertThat(inventoryRepository.existsByProductIdAndWarehouseId(product.getId(), warehouse.getId())).isTrue();
        assertThat(inventoryRepository.existsByProductIdAndWarehouseId(product.getId(), 999L)).isFalse();
    }
}
