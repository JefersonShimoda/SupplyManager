package com.supplymanager.service;

import com.supplymanager.domain.dto.ProducibleProductDTO;
import com.supplymanager.domain.dto.ProductionSuggestionDTO;
import com.supplymanager.domain.model.Product;
import com.supplymanager.domain.model.ProductRawMaterial;
import com.supplymanager.domain.model.RawMaterial;
import com.supplymanager.repository.ProductRepository;
import com.supplymanager.repository.RawMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ProductionService {

    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;

    public ProductionService(ProductRepository productRepository, RawMaterialRepository rawMaterialRepository) {
        this.productRepository = productRepository;
        this.rawMaterialRepository = rawMaterialRepository;
    }

    public ProductionSuggestionDTO calculateSuggestion() {

        Map<Long, BigDecimal> availableStock = new HashMap<>();
        for (RawMaterial rm : rawMaterialRepository.findAll()) {
            availableStock.put(rm.getId(), rm.getStockQuantity());
        }

        List<Product> products = productRepository.findAllWithRawMaterialsOrderByValueDesc();

        List<ProducibleProductDTO> producible = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Product product : products) {
            List<ProductRawMaterial> requirements = product.getRawMaterials();

            if (requirements.isEmpty()) continue;

            int maxUnits = Integer.MAX_VALUE;
            for (ProductRawMaterial prm : requirements) {
                BigDecimal available = availableStock.getOrDefault(prm.getRawMaterial().getId(), BigDecimal.ZERO);
                BigDecimal required = prm.getRequiredQuantity();
                if (required.compareTo(BigDecimal.ZERO) <= 0) continue;

                int possible = available.divide(required, 0, RoundingMode.FLOOR).intValue();
                maxUnits = Math.min(maxUnits, possible);
            }

            if (maxUnits == Integer.MAX_VALUE) maxUnits = 0;

            if (maxUnits > 0) {
                for (ProductRawMaterial prm : requirements) {
                    BigDecimal consumed = prm.getRequiredQuantity().multiply(BigDecimal.valueOf(maxUnits));
                    availableStock.merge(prm.getRawMaterial().getId(), consumed, BigDecimal::subtract);
                }

                BigDecimal productTotal = product.getValue().multiply(BigDecimal.valueOf(maxUnits));
                totalValue = totalValue.add(productTotal);

                producible.add(new ProducibleProductDTO(
                        product.getId(),
                        product.getCode(),
                        product.getName(),
                        product.getValue(),
                        maxUnits,
                        productTotal));
            }
        }

        return new ProductionSuggestionDTO(producible, totalValue);
    }
}
