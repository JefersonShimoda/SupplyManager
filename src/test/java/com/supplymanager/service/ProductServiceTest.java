package com.supplymanager.service;

import com.supplymanager.domain.dto.ProductDTO;
import com.supplymanager.domain.model.Product;
import com.supplymanager.domain.model.ProductRawMaterial;
import com.supplymanager.domain.model.RawMaterial;
import com.supplymanager.exception.DuplicateResourceException;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findAll_shouldReturnListOfProducts() {
        Product p1 = new Product(1L, "P1", "Product 1", new BigDecimal("10.00"), new ArrayList<>());
        Product p2 = new Product(2L, "P2", "Product 2", new BigDecimal("20.00"), new ArrayList<>());
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProductDTO> result = productService.findAll();

        assertEquals(2, result.size());
        assertEquals("P1", result.get(0).code());
        assertEquals("P2", result.get(1).code());
    }

    @Test
    void findAll_shouldReturnEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductDTO> result = productService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnProductWithRawMaterials() {
        RawMaterial rm = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("100"), new ArrayList<>());
        Product product = new Product(1L, "P1", "Product 1", new BigDecimal("50.00"), new ArrayList<>());
        ProductRawMaterial prm = new ProductRawMaterial(1L, product, rm, new BigDecimal("2.5"));
        product.setRawMaterials(List.of(prm));

        when(productRepository.findByIdWithRawMaterials(1L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.findById(1L);

        assertEquals("P1", result.code());
        assertEquals(1, result.rawMaterials().size());
        assertEquals("RM1", result.rawMaterials().get(0).rawMaterialCode());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(productRepository.findByIdWithRawMaterials(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(99L));
    }

    @Test
    void create_shouldCreateProduct() {
        ProductDTO dto = new ProductDTO(null, "P1", "Product 1", new BigDecimal("10.00"), null);
        Product saved = new Product(1L, "P1", "Product 1", new BigDecimal("10.00"), new ArrayList<>());

        when(productRepository.existsByCode("P1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO result = productService.create(dto);

        assertEquals(1L, result.id());
        assertEquals("P1", result.code());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_shouldThrowWhenDuplicateCode() {
        ProductDTO dto = new ProductDTO(null, "P1", "Product 1", new BigDecimal("10.00"), null);
        when(productRepository.existsByCode("P1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.create(dto));
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateProduct() {
        Product existing = new Product(1L, "P1", "Product 1", new BigDecimal("10.00"), new ArrayList<>());
        ProductDTO dto = new ProductDTO(null, "P1-UPDATED", "Product Updated", new BigDecimal("20.00"), null);
        Product updated = new Product(1L, "P1-UPDATED", "Product Updated", new BigDecimal("20.00"), new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByCode("P1-UPDATED")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        ProductDTO result = productService.update(1L, dto);

        assertEquals("P1-UPDATED", result.code());
        assertEquals(new BigDecimal("20.00"), result.value());
    }

    @Test
    void update_shouldAllowSameCode() {
        Product existing = new Product(1L, "P1", "Product 1", new BigDecimal("10.00"), new ArrayList<>());
        ProductDTO dto = new ProductDTO(null, "P1", "Product Renamed", new BigDecimal("15.00"), null);
        Product updated = new Product(1L, "P1", "Product Renamed", new BigDecimal("15.00"), new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        ProductDTO result = productService.update(1L, dto);

        assertEquals("Product Renamed", result.name());
        verify(productRepository, never()).existsByCode(anyString());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        ProductDTO dto = new ProductDTO(null, "P1", "Product 1", new BigDecimal("10.00"), null);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.update(99L, dto));
    }

    @Test
    void update_shouldThrowWhenDuplicateCode() {
        Product existing = new Product(1L, "P1", "Product 1", new BigDecimal("10.00"), new ArrayList<>());
        ProductDTO dto = new ProductDTO(null, "P2", "Product 1", new BigDecimal("10.00"), null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByCode("P2")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.update(1L, dto));
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.delete(99L));
        verify(productRepository, never()).deleteById(any());
    }
}
