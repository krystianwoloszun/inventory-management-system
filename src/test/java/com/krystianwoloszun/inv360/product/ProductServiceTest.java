package com.krystianwoloszun.inv360.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krystianwoloszun.inv360.common.exception.ProductAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.ProductNotFoundException;
import com.krystianwoloszun.inv360.product.dto.CreateProductRequest;
import com.krystianwoloszun.inv360.product.dto.ProductResponse;
import com.krystianwoloszun.inv360.product.dto.UpdateProductRequest;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct() {
        return Product.builder()
                .id(1L)
                .name("Widget")
                .description("A useful widget")
                .sku("SKU-001")
                .price(new BigDecimal("9.99"))
                .build();
    }

    @Test
    void createProduct_savesAndReturnsResponse() {
        CreateProductRequest request = new CreateProductRequest(
                "Widget", "A useful widget", "SKU-001", new BigDecimal("9.99"));

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.existsByName("Widget")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct());

        ProductResponse response = productService.createProduct(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Widget");
        assertThat(response.sku()).isEqualTo("SKU-001");
        assertThat(response.price()).isEqualByComparingTo("9.99");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_throwsWhenSkuExists() {
        CreateProductRequest request = new CreateProductRequest(
                "Widget", "A useful widget", "SKU-001", new BigDecimal("9.99"));

        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining("SKU-001");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsWhenNameExists() {
        CreateProductRequest request = new CreateProductRequest(
                "Widget", "A useful widget", "SKU-001", new BigDecimal("9.99"));

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.existsByName("Widget")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining("Widget");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct()));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.name()).isEqualTo("Widget");
    }

    @Test
    void getProductById_throwsWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getProductBySku_returnsProduct() {
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(sampleProduct()));

        assertThat(productService.getProductBySku("SKU-001").sku()).isEqualTo("SKU-001");
    }

    @Test
    void getProductBySku_throwsWhenMissing() {
        when(productRepository.findBySku("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductBySku("NOPE"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getProductByName_throwsWhenMissing() {
        when(productRepository.findByName("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductByName("Ghost"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void updateProduct_updatesFields() {
        Product existing = sampleProduct();
        UpdateProductRequest request = new UpdateProductRequest(
                "Widget Pro", "Better widget", "SKU-002", new BigDecimal("19.99"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("SKU-002")).thenReturn(false);
        when(productRepository.existsByName("Widget Pro")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response.name()).isEqualTo("Widget Pro");
        assertThat(response.sku()).isEqualTo("SKU-002");
        assertThat(response.price()).isEqualByComparingTo("19.99");
    }

    @Test
    void updateProduct_keepingSameSkuAndNameDoesNotCheckUniqueness() {
        Product existing = sampleProduct();
        UpdateProductRequest request = new UpdateProductRequest(
                "Widget", "Updated description", "SKU-001", new BigDecimal("12.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response.description()).isEqualTo("Updated description");
        verify(productRepository, never()).existsBySku(any());
        verify(productRepository, never()).existsByName(any());
    }

    @Test
    void updateProduct_throwsWhenNewSkuTaken() {
        Product existing = sampleProduct();
        UpdateProductRequest request = new UpdateProductRequest(
                "Widget", "desc", "SKU-TAKEN", new BigDecimal("9.99"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("SKU-TAKEN")).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
                .isInstanceOf(ProductAlreadyExistsException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_throwsWhenMissing() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(42L,
                new UpdateProductRequest("x", "y", "z", BigDecimal.ONE)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getAllProducts_mapsAll() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct()));

        List<ProductResponse> all = productService.getAllProducts();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).name()).isEqualTo("Widget");
    }

    @Test
    void deleteProduct_deletesExisting() {
        Product existing = sampleProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        productService.deleteProduct(1L);

        verify(productRepository).delete(existing);
    }

    @Test
    void deleteProduct_throwsWhenMissing() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }
}
