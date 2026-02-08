package com.supplymanager.service;

import com.supplymanager.domain.dto.ProductDTO;
import com.supplymanager.domain.dto.ProductRawMaterialDTO;
import com.supplymanager.domain.model.Product;
import com.supplymanager.exception.DuplicateResourceException;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(this::toDTOSimple)
                .toList();
    }

    public ProductDTO findById(Long id) {
        Product product = productRepository.findByIdWithRawMaterials(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toDTOFull(product);
    }

    @Transactional
    public ProductDTO create(ProductDTO dto) {
        if (productRepository.existsByCode(dto.code())) {
            throw new DuplicateResourceException("Product with code '" + dto.code() + "' already exists");
        }
        Product product = new Product();
        product.setCode(dto.code());
        product.setName(dto.name());
        product.setValue(dto.value());
        return toDTOSimple(productRepository.save(product));
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        if (!product.getCode().equals(dto.code()) && productRepository.existsByCode(dto.code())) {
            throw new DuplicateResourceException("Product with code '" + dto.code() + "' already exists");
        }
        product.setCode(dto.code());
        product.setName(dto.name());
        product.setValue(dto.value());
        return toDTOSimple(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }

    private ProductDTO toDTOSimple(Product p) {
        return new ProductDTO(p.getId(), p.getCode(), p.getName(), p.getValue(), Collections.emptyList());
    }

    private ProductDTO toDTOFull(Product p) {
        List<ProductRawMaterialDTO> materials = p.getRawMaterials().stream()
                .map(prm -> new ProductRawMaterialDTO(
                        prm.getId(),
                        prm.getRawMaterial().getId(),
                        prm.getRawMaterial().getCode(),
                        prm.getRawMaterial().getName(),
                        prm.getRequiredQuantity()))
                .toList();
        return new ProductDTO(p.getId(), p.getCode(), p.getName(), p.getValue(), materials);
    }
}
