package com.supplymanager.service;

import com.supplymanager.domain.dto.ProductRawMaterialDTO;
import com.supplymanager.domain.model.Product;
import com.supplymanager.domain.model.ProductRawMaterial;
import com.supplymanager.domain.model.RawMaterial;
import com.supplymanager.exception.DuplicateResourceException;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.repository.ProductRawMaterialRepository;
import com.supplymanager.repository.ProductRepository;
import com.supplymanager.repository.RawMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductRawMaterialService {

    private final ProductRawMaterialRepository prmRepository;
    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;

    public ProductRawMaterialService(ProductRawMaterialRepository prmRepository,
                                     ProductRepository productRepository,
                                     RawMaterialRepository rawMaterialRepository) {
        this.prmRepository = prmRepository;
        this.productRepository = productRepository;
        this.rawMaterialRepository = rawMaterialRepository;
    }

    public List<ProductRawMaterialDTO> findByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        Product product = productRepository.findByIdWithRawMaterials(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return product.getRawMaterials().stream().map(this::toDTO).toList();
    }

    @Transactional
    public ProductRawMaterialDTO add(Long productId, ProductRawMaterialDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        RawMaterial rawMaterial = rawMaterialRepository.findById(dto.rawMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("RawMaterial", dto.rawMaterialId()));

        prmRepository.findByProductIdAndRawMaterialId(productId, dto.rawMaterialId())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("This raw material is already linked to this product");
                });

        ProductRawMaterial prm = new ProductRawMaterial();
        prm.setProduct(product);
        prm.setRawMaterial(rawMaterial);
        prm.setRequiredQuantity(dto.requiredQuantity());
        return toDTO(prmRepository.save(prm));
    }

    @Transactional
    public ProductRawMaterialDTO update(Long productId, Long rawMaterialId, ProductRawMaterialDTO dto) {
        ProductRawMaterial prm = prmRepository.findByProductIdAndRawMaterialId(productId, rawMaterialId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRawMaterial", productId));
        prm.setRequiredQuantity(dto.requiredQuantity());
        return toDTO(prmRepository.save(prm));
    }

    @Transactional
    public void remove(Long productId, Long rawMaterialId) {
        ProductRawMaterial prm = prmRepository.findByProductIdAndRawMaterialId(productId, rawMaterialId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRawMaterial", productId));
        prmRepository.delete(prm);
    }

    private ProductRawMaterialDTO toDTO(ProductRawMaterial prm) {
        return new ProductRawMaterialDTO(
                prm.getId(),
                prm.getRawMaterial().getId(),
                prm.getRawMaterial().getCode(),
                prm.getRawMaterial().getName(),
                prm.getRequiredQuantity());
    }
}
