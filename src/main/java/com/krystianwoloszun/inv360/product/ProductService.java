package com.krystianwoloszun.inv360.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.ProductNotFoundException;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists.");
        }
        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("Product with name " + product.getName() + " already exists.");
        }
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found."));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product with SKU " + sku + " not found."));
    }

    @Transactional(readOnly = true)
    public Product getProductByName(String name) {
        return productRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Product with name " + name + " not found."));
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);
        if (!existingProduct.getSku().equals(updatedProduct.getSku())
                && productRepository.existsBySku(updatedProduct.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + updatedProduct.getSku() + " already exists.");
        }
        if (!existingProduct.getName().equals(updatedProduct.getName())
                && productRepository.existsByName(updatedProduct.getName())) {
            throw new IllegalArgumentException("Product with name " + updatedProduct.getName() + " already exists.");
        }
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setPrice(updatedProduct.getPrice());
        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product with ID " + id + " not found.");
        }
        productRepository.deleteById(id);
    }
}
