package com.supplymanager.controller;

import com.supplymanager.domain.dto.ProductionSuggestionDTO;
import com.supplymanager.service.ProductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production")
public class ProductionController {

    @Autowired
    private ProductionService productionService;

    @GetMapping("/suggestion")
    public ResponseEntity<ProductionSuggestionDTO> getSuggestion() {
        return ResponseEntity.ok(productionService.calculateSuggestion());
    }
}
