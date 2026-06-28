package com.krystianwoloszun.inv360.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krystianwoloszun.inv360.common.exception.InsufficientStockException;
import com.krystianwoloszun.inv360.common.exception.InvalidQuantityException;
import com.krystianwoloszun.inv360.common.exception.InvalidWarehouseTransferException;
import com.krystianwoloszun.inv360.common.exception.InventoryAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.InventoryNotFoundException;
import com.krystianwoloszun.inv360.common.exception.ProductNotFoundException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;
import com.krystianwoloszun.inv360.inventory.dto.AddStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.CreateInventoryRequest;
import com.krystianwoloszun.inv360.inventory.dto.InventoryResponse;
import com.krystianwoloszun.inv360.inventory.dto.RemoveStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.TransferStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.UpdateInventoryRequest;
import com.krystianwoloszun.inv360.product.Product;
import com.krystianwoloszun.inv360.product.ProductRepository;
import com.krystianwoloszun.inv360.stockmovement.OperationType;
import com.krystianwoloszun.inv360.stockmovement.StockMovement;
import com.krystianwoloszun.inv360.stockmovement.StockMovementRepository;
import com.krystianwoloszun.inv360.warehouse.Warehouse;
import com.krystianwoloszun.inv360.warehouse.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private static final Long PRODUCT_ID = 10L;
    private static final Long SOURCE_WH = 1L;
    private static final Long TARGET_WH = 2L;

    private Product product() {
        return Product.builder().id(PRODUCT_ID).name("Widget").sku("SKU-001").build();
    }

    private Warehouse warehouse(Long id) {
        return Warehouse.builder().id(id).name("WH-" + id).build();
    }

    private Inventory inventory(Long id, Long warehouseId, int quantity) {
        return Inventory.builder()
                .id(id)
                .product(product())
                .warehouse(warehouse(warehouseId))
                .quantity(quantity)
                .build();
    }

    // ---------- createInventory ----------

    @Test
    void createInventory_savesAndReturnsResponse() {
        CreateInventoryRequest request = new CreateInventoryRequest(PRODUCT_ID, SOURCE_WH, 5);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(warehouseRepository.findById(SOURCE_WH)).thenReturn(Optional.of(warehouse(SOURCE_WH)));
        when(inventoryRepository.existsByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory(100L, SOURCE_WH, 5));

        InventoryResponse response = inventoryService.createInventory(request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.productId()).isEqualTo(PRODUCT_ID);
        assertThat(response.warehouseId()).isEqualTo(SOURCE_WH);
        assertThat(response.quantity()).isEqualTo(5);
    }

    @Test
    void createInventory_throwsWhenQuantityNull() {
        CreateInventoryRequest request = new CreateInventoryRequest(PRODUCT_ID, SOURCE_WH, null);

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessageContaining("null");
    }

    @Test
    void createInventory_throwsWhenQuantityNegative() {
        CreateInventoryRequest request = new CreateInventoryRequest(PRODUCT_ID, SOURCE_WH, -1);

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void createInventory_throwsWhenProductMissing() {
        CreateInventoryRequest request = new CreateInventoryRequest(PRODUCT_ID, SOURCE_WH, 5);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createInventory_throwsWhenWarehouseMissing() {
        CreateInventoryRequest request = new CreateInventoryRequest(PRODUCT_ID, SOURCE_WH, 5);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(warehouseRepository.findById(SOURCE_WH)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    void createInventory_throwsWhenAlreadyExists() {
        CreateInventoryRequest request = new CreateInventoryRequest(PRODUCT_ID, SOURCE_WH, 5);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(warehouseRepository.findById(SOURCE_WH)).thenReturn(Optional.of(warehouse(SOURCE_WH)));
        when(inventoryRepository.existsByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH)).thenReturn(true);

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(InventoryAlreadyExistsException.class);

        verify(inventoryRepository, never()).save(any());
    }

    // ---------- updateInventory ----------

    @Test
    void updateInventory_updatesQuantity() {
        Inventory existing = inventory(100L, SOURCE_WH, 5);
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryResponse response = inventoryService.updateInventory(100L, new UpdateInventoryRequest(42));

        assertThat(response.quantity()).isEqualTo(42);
    }

    @Test
    void updateInventory_throwsWhenNegativeQuantity() {
        assertThatThrownBy(() -> inventoryService.updateInventory(100L, new UpdateInventoryRequest(-3)))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    void updateInventory_throwsWhenMissing() {
        when(inventoryRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.updateInventory(100L, new UpdateInventoryRequest(5)))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    // ---------- delete / get ----------

    @Test
    void deleteInventory_deletesExisting() {
        Inventory existing = inventory(100L, SOURCE_WH, 5);
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(existing));

        inventoryService.deleteInventory(100L);

        verify(inventoryRepository).delete(existing);
    }

    @Test
    void deleteInventory_throwsWhenMissing() {
        when(inventoryRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.deleteInventory(100L))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void getInventoryByProductIdAndWarehouseId_returns() {
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.of(inventory(100L, SOURCE_WH, 7)));

        InventoryResponse response = inventoryService.getInventoryByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH);

        assertThat(response.quantity()).isEqualTo(7);
    }

    @Test
    void getInventoryByProductIdAndWarehouseId_throwsWhenMissing() {
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void getAllInventories_mapsAll() {
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory(100L, SOURCE_WH, 3)));

        assertThat(inventoryService.getAllInventories()).hasSize(1);
    }

    // ---------- addStock ----------

    @Test
    void addStock_increasesQuantityAndRecordsMovement() {
        Inventory existing = inventory(100L, SOURCE_WH, 5);
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.of(existing));

        InventoryResponse response = inventoryService.addStock(new AddStockRequest(SOURCE_WH, PRODUCT_ID, 3));

        assertThat(response.quantity()).isEqualTo(8);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();
        assertThat(movement.getOperationType()).isEqualTo(OperationType.IN);
        assertThat(movement.getQuantity()).isEqualTo(3);
        assertThat(movement.getSourceWarehouse()).isNull();
        assertThat(movement.getTargetWarehouse().getId()).isEqualTo(SOURCE_WH);
        assertThat(movement.getMovementDate()).isNotNull();
    }

    @Test
    void addStock_throwsWhenQuantityZero() {
        assertThatThrownBy(() -> inventoryService.addStock(new AddStockRequest(SOURCE_WH, PRODUCT_ID, 0)))
                .isInstanceOf(InvalidQuantityException.class);

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void addStock_throwsWhenInventoryMissing() {
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.addStock(new AddStockRequest(SOURCE_WH, PRODUCT_ID, 3)))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    // ---------- removeStock ----------

    @Test
    void removeStock_decreasesQuantityAndRecordsMovement() {
        Inventory existing = inventory(100L, SOURCE_WH, 5);
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.of(existing));

        InventoryResponse response = inventoryService.removeStock(new RemoveStockRequest(SOURCE_WH, PRODUCT_ID, 2));

        assertThat(response.quantity()).isEqualTo(3);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();
        assertThat(movement.getOperationType()).isEqualTo(OperationType.OUT);
        assertThat(movement.getSourceWarehouse().getId()).isEqualTo(SOURCE_WH);
        assertThat(movement.getTargetWarehouse()).isNull();
    }

    @Test
    void removeStock_throwsWhenInsufficient() {
        Inventory existing = inventory(100L, SOURCE_WH, 1);
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> inventoryService.removeStock(new RemoveStockRequest(SOURCE_WH, PRODUCT_ID, 5)))
                .isInstanceOf(InsufficientStockException.class);

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void removeStock_throwsWhenQuantityNegative() {
        assertThatThrownBy(() -> inventoryService.removeStock(new RemoveStockRequest(SOURCE_WH, PRODUCT_ID, -1)))
                .isInstanceOf(InvalidQuantityException.class);
    }

    // ---------- transferStock ----------

    @Test
    void transferStock_movesBetweenExistingInventories() {
        Inventory source = inventory(100L, SOURCE_WH, 10);
        Inventory target = inventory(200L, TARGET_WH, 4);

        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.of(source));
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, TARGET_WH))
                .thenReturn(Optional.of(target));

        List<InventoryResponse> result = inventoryService.transferStock(
                new TransferStockRequest(SOURCE_WH, TARGET_WH, PRODUCT_ID, 6));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).quantity()).isEqualTo(4);   // source: 10 - 6
        assertThat(result.get(1).quantity()).isEqualTo(10);  // target: 4 + 6

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getOperationType()).isEqualTo(OperationType.TRANSFER);
    }

    @Test
    void transferStock_createsEmptyTargetWhenMissing() {
        Inventory source = inventory(100L, SOURCE_WH, 10);

        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.of(source));
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, TARGET_WH))
                .thenReturn(Optional.empty());
        // createEmptyInventory(...)
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(warehouseRepository.findById(TARGET_WH)).thenReturn(Optional.of(warehouse(TARGET_WH)));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        List<InventoryResponse> result = inventoryService.transferStock(
                new TransferStockRequest(SOURCE_WH, TARGET_WH, PRODUCT_ID, 6));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).quantity()).isEqualTo(4);  // source: 10 - 6
        assertThat(result.get(1).quantity()).isEqualTo(6);  // new target: 0 + 6
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void transferStock_throwsWhenSameWarehouse() {
        assertThatThrownBy(() -> inventoryService.transferStock(
                new TransferStockRequest(SOURCE_WH, SOURCE_WH, PRODUCT_ID, 5)))
                .isInstanceOf(InvalidWarehouseTransferException.class);
    }

    @Test
    void transferStock_throwsWhenSourceInsufficient() {
        Inventory source = inventory(100L, SOURCE_WH, 2);
        when(inventoryRepository.findByProductIdAndWarehouseId(PRODUCT_ID, SOURCE_WH))
                .thenReturn(Optional.of(source));

        assertThatThrownBy(() -> inventoryService.transferStock(
                new TransferStockRequest(SOURCE_WH, TARGET_WH, PRODUCT_ID, 5)))
                .isInstanceOf(InsufficientStockException.class);

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void transferStock_throwsWhenQuantityZero() {
        assertThatThrownBy(() -> inventoryService.transferStock(
                new TransferStockRequest(SOURCE_WH, TARGET_WH, PRODUCT_ID, 0)))
                .isInstanceOf(InvalidQuantityException.class);
    }
}
