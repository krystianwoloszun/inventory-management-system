package com.krystianwoloszun.inv360.inventory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    public Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    public boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);

    public Inventory findByProductId(Long productId);

    public Inventory findByWarehouseId(Long warehouseId);

}
