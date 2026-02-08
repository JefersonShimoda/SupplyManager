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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRawMaterialServiceTest {

    @Mock
    private ProductRawMaterialRepository prmRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @InjectMocks
    private ProductRawMaterialService prmService;

    private Product product;
    private RawMaterial rawMaterial;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "P1", "Product 1", new BigDecimal("100"), new ArrayList<>());
        rawMaterial = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("50"), new ArrayList<>());
    }

    @Test
    void findByProductId_shouldReturnRawMaterials() {
        ProductRawMaterial prm = new ProductRawMaterial(1L, product, rawMaterial, new BigDecimal("2.5"));
        product.setRawMaterials(List.of(prm));

        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findByIdWithRawMaterials(1L)).thenReturn(Optional.of(product));

        List<ProductRawMaterialDTO> result = prmService.findByProductId(1L);

        assertEquals(1, result.size());
        assertEquals("RM1", result.get(0).rawMaterialCode());
        assertEquals(new BigDecimal("2.5"), result.get(0).requiredQuantity());
    }

    @Test
    void findByProductId_shouldThrowWhenProductNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> prmService.findByProductId(99L));
    }

    @Test
    void add_shouldAddRawMaterialToProduct() {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, 1L, null, null, new BigDecimal("3.0"));
        ProductRawMaterial saved = new ProductRawMaterial(1L, product, rawMaterial, new BigDecimal("3.0"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(prmRepository.findByProductIdAndRawMaterialId(1L, 1L)).thenReturn(Optional.empty());
        when(prmRepository.save(any(ProductRawMaterial.class))).thenReturn(saved);

        ProductRawMaterialDTO result = prmService.add(1L, dto);

        assertEquals(1L, result.id());
        assertEquals("RM1", result.rawMaterialCode());
        assertEquals(new BigDecimal("3.0"), result.requiredQuantity());
    }

    @Test
    void add_shouldThrowWhenProductNotFound() {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, 1L, null, null, new BigDecimal("3.0"));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> prmService.add(99L, dto));
        verify(prmRepository, never()).save(any());
    }

    @Test
    void add_shouldThrowWhenRawMaterialNotFound() {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, 99L, null, null, new BigDecimal("3.0"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(rawMaterialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> prmService.add(1L, dto));
        verify(prmRepository, never()).save(any());
    }

    @Test
    void add_shouldThrowWhenDuplicate() {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, 1L, null, null, new BigDecimal("3.0"));
        ProductRawMaterial existing = new ProductRawMaterial(1L, product, rawMaterial, new BigDecimal("2.0"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(prmRepository.findByProductIdAndRawMaterialId(1L, 1L)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> prmService.add(1L, dto));
        verify(prmRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateQuantity() {
        ProductRawMaterial existing = new ProductRawMaterial(1L, product, rawMaterial, new BigDecimal("2.0"));
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, null, null, null, new BigDecimal("5.0"));
        ProductRawMaterial updated = new ProductRawMaterial(1L, product, rawMaterial, new BigDecimal("5.0"));

        when(prmRepository.findByProductIdAndRawMaterialId(1L, 1L)).thenReturn(Optional.of(existing));
        when(prmRepository.save(any(ProductRawMaterial.class))).thenReturn(updated);

        ProductRawMaterialDTO result = prmService.update(1L, 1L, dto);

        assertEquals(new BigDecimal("5.0"), result.requiredQuantity());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, null, null, null, new BigDecimal("5.0"));
        when(prmRepository.findByProductIdAndRawMaterialId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> prmService.update(1L, 99L, dto));
    }

    @Test
    void remove_shouldDeleteAssociation() {
        ProductRawMaterial existing = new ProductRawMaterial(1L, product, rawMaterial, new BigDecimal("2.0"));
        when(prmRepository.findByProductIdAndRawMaterialId(1L, 1L)).thenReturn(Optional.of(existing));

        prmService.remove(1L, 1L);

        verify(prmRepository).delete(existing);
    }

    @Test
    void remove_shouldThrowWhenNotFound() {
        when(prmRepository.findByProductIdAndRawMaterialId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> prmService.remove(1L, 99L));
        verify(prmRepository, never()).delete(any());
    }
}
