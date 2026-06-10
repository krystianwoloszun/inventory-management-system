package com.krystianwoloszun.inv360.inventory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getWarehouse().getId(),
                inventory.getProduct().getId(),
                inventory.getQuantity());
    }

    public InventoryResponse createInventory(CreateInventoryRequest request) {

        validateInventoryQuantity(request.quantity());

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + request.productId() + " not found."));

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse with ID " + request.warehouseId() + " not found."));

        if (inventoryRepository.existsByProductIdAndWarehouseId(request.productId(), request.warehouseId())) {
            throw new InventoryAlreadyExistsException(
                    "Inventory for product ID " + request.productId() +
                            " and warehouse ID " + request.warehouseId() +
                            " already exists.");
        }

        Inventory inventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(request.quantity())
                .build();

        Inventory saved = inventoryRepository.save(inventory);

        return toResponse(saved);
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
    public InventoryResponse getInventoryByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        Inventory inventory = getInventoryEntityByProductIdAndWarehouseId(productId, warehouseId);

        return toResponse(inventory);
    }

    public InventoryResponse updateInventory(Long id, UpdateInventoryRequest request) {

        validateInventoryQuantity(request.quantity());

        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory with ID " + id + " not found."));

        existingInventory.setQuantity(request.quantity());

        Inventory saved = inventoryRepository.save(existingInventory);

        return toResponse(saved);
    }

    public void deleteInventory(Long id) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory with ID " + id + " not found."));

        inventoryRepository.delete(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Inventory getInventoryEntityByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory for product ID " + productId +
                                " and warehouse ID " + warehouseId +
                                " not found."));
    }

    private void validateInventoryQuantity(Integer quantity) {
        if (quantity == null) {
            throw new InvalidQuantityException("Quantity cannot be null.");
        }

        if (quantity < 0) {
            throw new InvalidQuantityException("Quantity cannot be negative.");
        }
    }

    private void validateMovementQuantity(Integer quantity) {
        if (quantity == null) {
            throw new InvalidQuantityException("Quantity cannot be null.");
        }

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity cannot be zero or negative.");
        }
    }

    public InventoryResponse addStock(AddStockRequest request) {

        validateMovementQuantity(request.quantity());

        Inventory inventory = getInventoryEntityByProductIdAndWarehouseId(request.productId(), request.warehouseId());
        inventory.setQuantity(inventory.getQuantity() + request.quantity());

        stockMovementRepository.save(StockMovement.builder()
                .product(inventory.getProduct())
                .sourceWarehouse(null)
                .targetWarehouse(inventory.getWarehouse())
                .quantity(request.quantity())
                .operationType(OperationType.IN)
                .movementDate(LocalDateTime.now())
                .build());

        return toResponse(inventory);
    }

    public InventoryResponse removeStock(RemoveStockRequest request) {

        validateMovementQuantity(request.quantity());

        Inventory inventory = getInventoryEntityByProductIdAndWarehouseId(request.productId(), request.warehouseId());
        if (inventory.getQuantity() < request.quantity()) {
            throw new InsufficientStockException(
                    "Cannot remove more stock than available. Current quantity: " + inventory.getQuantity());
        }
        inventory.setQuantity(inventory.getQuantity() - request.quantity());

        stockMovementRepository.save(StockMovement.builder()
                .product(inventory.getProduct())
                .sourceWarehouse(inventory.getWarehouse())
                .targetWarehouse(null)
                .quantity(request.quantity())
                .operationType(OperationType.OUT)
                .movementDate(LocalDateTime.now())
                .build());

        return toResponse(inventory);
    }

    public List<InventoryResponse> transferStock(TransferStockRequest request) {

        validateMovementQuantity(request.quantity());
        if (request.sourceWarehouseId().equals(request.targetWarehouseId())) {
            throw new InvalidWarehouseTransferException("Cannot transfer stock within the same warehouse.");
        }

        Inventory sourceInventory = getInventoryEntityByProductIdAndWarehouseId(request.productId(),
                request.sourceWarehouseId());
        if (sourceInventory.getQuantity() < request.quantity()) {
            throw new InsufficientStockException(
                    "Cannot transfer more stock than available in source warehouse. Current quantity: "
                            + sourceInventory.getQuantity());
        }

        Inventory targetInventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.productId(), request.targetWarehouseId())
                .orElseGet(() -> createEmptyInventory(request.productId(), request.targetWarehouseId()));

        sourceInventory.setQuantity(sourceInventory.getQuantity() - request.quantity());
        targetInventory.setQuantity(targetInventory.getQuantity() + request.quantity());

        stockMovementRepository.save(StockMovement.builder()
                .product(sourceInventory.getProduct())
                .sourceWarehouse(sourceInventory.getWarehouse())
                .targetWarehouse(targetInventory.getWarehouse())
                .quantity(request.quantity())
                .operationType(OperationType.TRANSFER)
                .movementDate(LocalDateTime.now())
                .build());

        return List.of(toResponse(sourceInventory), toResponse(targetInventory));
    }
}
