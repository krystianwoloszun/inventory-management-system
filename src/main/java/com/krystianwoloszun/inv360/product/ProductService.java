package com.krystianwoloszun.inv360.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.ProductAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.ProductNotFoundException;
import com.krystianwoloszun.inv360.product.dto.CreateProductRequest;
import com.krystianwoloszun.inv360.product.dto.ProductResponse;
import com.krystianwoloszun.inv360.product.dto.UpdateProductRequest;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice());
    }

    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsBySku(request.sku())) {
            throw new ProductAlreadyExistsException("Product with SKU " + request.sku() + " already exists.");
        }

        if (productRepository.existsByName(request.name())) {
            throw new ProductAlreadyExistsException("Product with name " + request.name() + " already exists.");
        }

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .sku(request.sku())
                .price(request.price())
                .build();

        Product saved = productRepository.save(product);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + id + " not found."));

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with SKU " + sku + " not found."));

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByName(String name) {
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with name " + name + " not found."));

        return toResponse(product);
    }

    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + id + " not found."));

        if (!existing.getSku().equals(request.sku())
                && productRepository.existsBySku(request.sku())) {
            throw new ProductAlreadyExistsException(
                    "Product with SKU " + request.sku() + " already exists.");
        }

        if (!existing.getName().equals(request.name())
                && productRepository.existsByName(request.name())) {
            throw new ProductAlreadyExistsException(
                    "Product with name " + request.name() + " already exists.");
        }

        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setSku(request.sku());
        existing.setPrice(request.price());

        Product saved = productRepository.save(existing);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with ID " + id + " not found."));

        productRepository.delete(product);
    }
}
