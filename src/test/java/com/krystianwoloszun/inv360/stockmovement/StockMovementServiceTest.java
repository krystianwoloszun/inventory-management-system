package com.krystianwoloszun.inv360.stockmovement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krystianwoloszun.inv360.common.exception.StockMovementNotFoundException;
import com.krystianwoloszun.inv360.product.Product;
import com.krystianwoloszun.inv360.stockmovement.dto.StockMovementResponse;
import com.krystianwoloszun.inv360.warehouse.Warehouse;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    private Product product() {
        return Product.builder().id(10L).name("Widget").sku("SKU-001").build();
    }

    private Warehouse warehouse(Long id) {
        return Warehouse.builder().id(id).name("WH-" + id).build();
    }

    private StockMovement transferMovement() {
        return StockMovement.builder()
                .id(1L)
                .product(product())
                .sourceWarehouse(warehouse(1L))
                .targetWarehouse(warehouse(2L))
                .quantity(5)
                .operationType(OperationType.TRANSFER)
                .movementDate(LocalDateTime.now())
                .build();
    }

    private StockMovement inMovement() {
        return StockMovement.builder()
                .id(2L)
                .product(product())
                .sourceWarehouse(null)
                .targetWarehouse(warehouse(2L))
                .quantity(3)
                .operationType(OperationType.IN)
                .movementDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllMovements_mapsAll() {
        when(stockMovementRepository.findAll()).thenReturn(List.of(transferMovement(), inMovement()));

        List<StockMovementResponse> all = stockMovementService.getAllMovements();

        assertThat(all).hasSize(2);
    }

    @Test
    void getStockMovementById_returnsResponseWithWarehouseNames() {
        when(stockMovementRepository.findById(1L)).thenReturn(Optional.of(transferMovement()));

        StockMovementResponse response = stockMovementService.getStockMovementById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.productName()).isEqualTo("Widget");
        assertThat(response.sourceWarehouseId()).isEqualTo(1L);
        assertThat(response.sourceWarehouseName()).isEqualTo("WH-1");
        assertThat(response.targetWarehouseId()).isEqualTo(2L);
        assertThat(response.targetWarehouseName()).isEqualTo("WH-2");
        assertThat(response.operationType()).isEqualTo(OperationType.TRANSFER);
    }

    @Test
    void getStockMovementById_handlesNullSourceWarehouse() {
        when(stockMovementRepository.findById(2L)).thenReturn(Optional.of(inMovement()));

        StockMovementResponse response = stockMovementService.getStockMovementById(2L);

        assertThat(response.sourceWarehouseId()).isNull();
        assertThat(response.sourceWarehouseName()).isNull();
        assertThat(response.targetWarehouseName()).isEqualTo("WH-2");
    }

    @Test
    void getStockMovementById_throwsWhenMissing() {
        when(stockMovementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMovementService.getStockMovementById(99L))
                .isInstanceOf(StockMovementNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getMovementsByProduct_mapsResults() {
        when(stockMovementRepository.findByProductId(10L)).thenReturn(List.of(transferMovement()));

        assertThat(stockMovementService.getMovementsByProduct(10L)).hasSize(1);
    }

    @Test
    void getMovementsBySourceWarehouse_mapsResults() {
        when(stockMovementRepository.findBySourceWarehouseId(1L)).thenReturn(List.of(transferMovement()));

        assertThat(stockMovementService.getMovementsBySourceWarehouse(1L)).hasSize(1);
    }

    @Test
    void getMovementsByTargetWarehouse_mapsResults() {
        when(stockMovementRepository.findByTargetWarehouseId(2L))
                .thenReturn(List.of(transferMovement(), inMovement()));

        assertThat(stockMovementService.getMovementsByTargetWarehouse(2L)).hasSize(2);
    }

    @Test
    void getMovementsBetweenWarehouses_mapsResults() {
        when(stockMovementRepository.findBySourceWarehouseIdAndTargetWarehouseId(1L, 2L))
                .thenReturn(List.of(transferMovement()));

        assertThat(stockMovementService.getMovementsBetweenWarehouses(1L, 2L)).hasSize(1);
    }
}
