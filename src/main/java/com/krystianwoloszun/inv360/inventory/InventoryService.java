package com.krystianwoloszun.inv360.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.InvalidQuantityException;
import com.krystianwoloszun.inv360.common.exception.InventoryAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.InventoryNotFoundException;
import com.krystianwoloszun.inv360.common.exception.ProductNotFoundException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;
import com.krystianwoloszun.inv360.product.Product;
import com.krystianwoloszun.inv360.product.ProductRepository;
import com.krystianwoloszun.inv360.warehouse.Warehouse;
import com.krystianwoloszun.inv360.warehouse.WarehouseRepository;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository) {

        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
    }

    public Inventory createInventory(Long productId, Long warehouseId, int quantity) {

        validateQuantity(quantity);

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

    @Transactional(readOnly = true)
    public Inventory getInventoryByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory for product ID " + productId +
                                " and warehouse ID " + warehouseId +
                                " not found."));
    }

    public Inventory updateInventory(Long id, Inventory updatedInventory) {

        validateQuantity(updatedInventory.getQuantity());

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

    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new InvalidQuantityException("Quantity cannot be negative.");
        }
    }
}