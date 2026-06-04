package com.krystianwoloszun.inv360.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krystianwoloszun.inv360.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByName(String name);

    boolean existsBySku(int sku);

    boolean existsByName(String name);

}
