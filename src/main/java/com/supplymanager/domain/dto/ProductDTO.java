package com.supplymanager.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductDTO(
    Long id,
    @NotBlank String code,
    @NotBlank String name,
    @NotNull @DecimalMin("0.01") BigDecimal value,
    List<ProductRawMaterialDTO> rawMaterials
) {}
