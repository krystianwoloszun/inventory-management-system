package com.krystianwoloszun.inv360.inventory;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.InsufficientStockException;
import com.krystianwoloszun.inv360.common.exception.InvalidQuantityException;
import com.krystianwoloszun.inv360.common.exception.InvalidWarehouseTransferException;
import com.krystianwoloszun.inv360.common.exception.InventoryAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.InventoryNotFoundException;
import com.krystianwoloszun.inv360.common.exception.ProductNotFoundException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;
import com.krystianwoloszun.inv360.product.Product;
import com.krystianwoloszun.inv360.product.ProductRepository;
import com.krystianwoloszun.inv360.stockmovement.OperationType;
import com.krystianwoloszun.inv360.stockmovement.StockMovement;
import com.krystianwoloszun.inv360.stockmovement.StockMovementRepository;
import com.krystianwoloszun.inv360.warehouse.Warehouse;
import com.krystianwoloszun.inv360.warehouse.WarehouseRepository;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository) {

        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Inventory createInventory(Long productId, Long warehouseId, int quantity) {

        validateInventoryQuantity(quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + productId + " not found."));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse with ID " + warehouseId + " not found."));

        if (inventoryRepository.existsByProductIdAndWarehouseId(productId, warehouseId)) {
            throw new InventoryAlreadyExistsException(
                    "Inventory for product ID " + productId +
                            " and warehouse ID " + warehouseId +
                            " already exists.");
        }

        Inventory inventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(quantity)
                .build();

        return inventoryRepository.save(inventory);
    }
    
    private Inventory createEmptyInventory(Long productId, Long warehouseId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + productId + " not found."));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse with ID " + warehouseId + " not found."));
        Inventory inventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(0)
                .build();
        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory for product ID " + productId +
                                " and warehouse ID " + warehouseId +
                                " not found."));
    }

    public Inventory updateInventory(Long id, Inventory updatedInventory) {

        validateInventoryQuantity(updatedInventory.getQuantity());

        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory with ID " + id + " not found."));

        existingInventory.setQuantity(updatedInventory.getQuantity());

        return inventoryRepository.save(existingInventory);
    }

    public void deleteInventory(Long id) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory with ID " + id + " not found."));

        inventoryRepository.delete(inventory);
    }

    private void validateInventoryQuantity(int quantity) {
        if (quantity < 0) {
            throw new InvalidQuantityException("Quantity cannot be negative.");
        }
    }

    private void validateMovementQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity cannot be zero or negative.");
        }
    }

    public void addStock(Long productId, Long warehouseId, int quantityToAdd) {

        validateMovementQuantity(quantityToAdd);

        Inventory inventory = getInventoryByProductIdAndWarehouseId(productId, warehouseId);
        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);

        stockMovementRepository.save(StockMovement.builder()
                .product(inventory.getProduct())
                .sourceWarehouse(null)
                .targetWarehouse(inventory.getWarehouse())
                .quantity(quantityToAdd)
                .operationType(OperationType.IN)
                .movementDate(LocalDateTime.now())
                .build());
    }

    public void removeStock(Long productId, Long warehouseId, int quantityToRemove) {

        validateMovementQuantity(quantityToRemove);

        Inventory inventory = getInventoryByProductIdAndWarehouseId(productId, warehouseId);
        if (inventory.getQuantity() < quantityToRemove) {
            throw new InsufficientStockException(
                    "Cannot remove more stock than available. Current quantity: " + inventory.getQuantity());
        }
        inventory.setQuantity(inventory.getQuantity() - quantityToRemove);

        stockMovementRepository.save(StockMovement.builder()
                .product(inventory.getProduct())
                .sourceWarehouse(inventory.getWarehouse())
                .targetWarehouse(null)
                .quantity(quantityToRemove)
                .operationType(OperationType.OUT)
                .movementDate(LocalDateTime.now())
                .build());
    }

    public void transferStock(Long productId, Long sourceWarehouseId, Long targetWarehouseId, int quantityToTransfer) {

        validateMovementQuantity(quantityToTransfer);
        if(sourceWarehouseId.equals(targetWarehouseId)) {
            throw new InvalidWarehouseTransferException("Cannot transfer stock within the same warehouse.");
        }

        Inventory sourceInventory = getInventoryByProductIdAndWarehouseId(productId, sourceWarehouseId);
        if (sourceInventory.getQuantity() < quantityToTransfer) {
            throw new InsufficientStockException(
                    "Cannot transfer more stock than available in source warehouse. Current quantity: " + sourceInventory.getQuantity());
        }

        Inventory targetInventory = inventoryRepository.findByProductIdAndWarehouseId(productId, targetWarehouseId)
                .orElseGet(() -> createEmptyInventory(productId, targetWarehouseId));

        sourceInventory.setQuantity(sourceInventory.getQuantity() - quantityToTransfer);
        targetInventory.setQuantity(targetInventory.getQuantity() + quantityToTransfer);

        stockMovementRepository.save(StockMovement.builder()
                .product(sourceInventory.getProduct())
                .sourceWarehouse(sourceInventory.getWarehouse())
                .targetWarehouse(targetInventory.getWarehouse())
                .quantity(quantityToTransfer)
                .operationType(OperationType.TRANSFER)
                .movementDate(LocalDateTime.now())
                .build());
    }
}