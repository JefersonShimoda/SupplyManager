package com.supplymanager.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRawMaterialDTO(
    Long id,
    @NotNull Long rawMaterialId,
    String rawMaterialCode,
    String rawMaterialName,
    @NotNull @DecimalMin("0.0001") BigDecimal requiredQuantity
) {}
