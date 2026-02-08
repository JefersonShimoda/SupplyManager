package com.supplymanager.controller;

import com.supplymanager.domain.dto.ProductRawMaterialDTO;
import com.supplymanager.service.ProductRawMaterialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/raw-materials")
public class ProductRawMaterialController {

    @Autowired
    private ProductRawMaterialService prmService;

    @GetMapping
    public ResponseEntity<List<ProductRawMaterialDTO>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(prmService.findByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<ProductRawMaterialDTO> add(
            @PathVariable Long productId, @Valid @RequestBody ProductRawMaterialDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prmService.add(productId, dto));
    }

    @PutMapping("/{rawMaterialId}")
    public ResponseEntity<ProductRawMaterialDTO> update(
            @PathVariable Long productId, @PathVariable Long rawMaterialId,
            @Valid @RequestBody ProductRawMaterialDTO dto) {
        return ResponseEntity.ok(prmService.update(productId, rawMaterialId, dto));
    }

    @DeleteMapping("/{rawMaterialId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId, @PathVariable Long rawMaterialId) {
        prmService.remove(productId, rawMaterialId);
        return ResponseEntity.noContent().build();
    }
}
