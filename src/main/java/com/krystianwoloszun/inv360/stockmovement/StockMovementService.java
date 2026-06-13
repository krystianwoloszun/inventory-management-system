package com.krystianwoloszun.inv360.stockmovement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.StockMovementNotFoundException;
import com.krystianwoloszun.inv360.stockmovement.dto.StockMovementResponse;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),

                movement.getProduct().getId(),
                movement.getProduct().getName(),

                movement.getSourceWarehouse() != null
                        ? movement.getSourceWarehouse().getId()
                        : null,

                movement.getSourceWarehouse() != null
                        ? movement.getSourceWarehouse().getName()
                        : null,

                movement.getTargetWarehouse() != null
                        ? movement.getTargetWarehouse().getId()
                        : null,

                movement.getTargetWarehouse() != null
                        ? movement.getTargetWarehouse().getName()
                        : null,

                movement.getQuantity(),
                movement.getOperationType(),
                movement.getMovementDate());
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getAllMovements() {
        return stockMovementRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockMovementResponse getStockMovementById(Long id) {

        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new StockMovementNotFoundException(
                        "Stock movement with ID " + id + " not found."));

        return toResponse(movement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByProduct(Long productId) {
        return stockMovementRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsBySourceWarehouse(Long warehouseId) {
        return stockMovementRepository.findBySourceWarehouseId(warehouseId)
                .stream()
                .map(this::toResponse)
                .toList();

    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByTargetWarehouse(Long warehouseId) {
        return stockMovementRepository.findByTargetWarehouseId(warehouseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsBetweenWarehouses(
            Long sourceWarehouseId,
            Long targetWarehouseId) {

        return stockMovementRepository.findBySourceWarehouseIdAndTargetWarehouseId(
                sourceWarehouseId,
                targetWarehouseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}