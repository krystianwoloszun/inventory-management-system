package com.krystianwoloszun.inv360.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product persistProduct(String name, String sku) {
        return productRepository.save(Product.builder()
                .name(name)
                .description("desc")
                .sku(sku)
                .price(new BigDecimal("5.00"))
                .build());
    }

    @Test
    void findBySku_returnsProduct() {
        persistProduct("Widget", "SKU-1");

        assertThat(productRepository.findBySku("SKU-1")).isPresent();
        assertThat(productRepository.findBySku("missing")).isEmpty();
    }

    @Test
    void findByName_returnsProduct() {
        persistProduct("Widget", "SKU-1");

        assertThat(productRepository.findByName("Widget")).isPresent();
        assertThat(productRepository.findByName("Other")).isEmpty();
    }

    @Test
    void existsBySkuAndExistsByName() {
        persistProduct("Widget", "SKU-1");

        assertThat(productRepository.existsBySku("SKU-1")).isTrue();
        assertThat(productRepository.existsBySku("SKU-2")).isFalse();
        assertThat(productRepository.existsByName("Widget")).isTrue();
        assertThat(productRepository.existsByName("Nope")).isFalse();
    }
}
