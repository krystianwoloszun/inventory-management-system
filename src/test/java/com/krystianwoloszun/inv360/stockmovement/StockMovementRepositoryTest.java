package com.krystianwoloszun.inv360.stockmovement;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.krystianwoloszun.inv360.product.Product;
import com.krystianwoloszun.inv360.product.ProductRepository;
import com.krystianwoloszun.inv360.warehouse.Address;
import com.krystianwoloszun.inv360.warehouse.Warehouse;
import com.krystianwoloszun.inv360.warehouse.WarehouseRepository;

@DataJpaTest
class StockMovementRepositoryTest {

    @Autowired
    private StockMovementRepository stockMovementRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;

    private Product product;
    private Warehouse source;
    private Warehouse target;

    private Warehouse warehouse(String name) {
        return warehouseRepository.save(Warehouse.builder()
                .name(name).description("d")
                .address(Address.builder().street("s").buildingNumber("1").city("c").postalCode("00-001").build())
                .build());
    }

    private void setUpData() {
        product = productRepository.save(Product.builder()
                .name("Widget").description("d").sku("SKU-1").price(new BigDecimal("1.00")).build());
        source = warehouse("Source");
        target = warehouse("Target");

        stockMovementRepository.save(StockMovement.builder()
                .product(product).sourceWarehouse(source).targetWarehouse(target)
                .quantity(5).operationType(OperationType.TRANSFER)
                .movementDate(LocalDateTime.now()).build());
    }

    @Test
    void findByProductId() {
        setUpData();

        assertThat(stockMovementRepository.findByProductId(product.getId())).hasSize(1);
        assertThat(stockMovementRepository.findByProductId(999L)).isEmpty();
    }

    @Test
    void findBySourceWarehouseId() {
        setUpData();

        assertThat(stockMovementRepository.findBySourceWarehouseId(source.getId())).hasSize(1);
        assertThat(stockMovementRepository.findBySourceWarehouseId(target.getId())).isEmpty();
    }

    @Test
    void findByTargetWarehouseId() {
        setUpData();

        assertThat(stockMovementRepository.findByTargetWarehouseId(target.getId())).hasSize(1);
    }

    @Test
    void findBySourceWarehouseIdAndTargetWarehouseId() {
        setUpData();

        assertThat(stockMovementRepository
                .findBySourceWarehouseIdAndTargetWarehouseId(source.getId(), target.getId())).hasSize(1);
        assertThat(stockMovementRepository
                .findBySourceWarehouseIdAndTargetWarehouseId(target.getId(), source.getId())).isEmpty();
    }
}
