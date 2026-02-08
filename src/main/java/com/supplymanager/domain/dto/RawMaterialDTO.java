package com.supplymanager.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RawMaterialDTO(
    Long id,
    @NotBlank String code,
    @NotBlank String name,
    @NotNull @DecimalMin("0.0") BigDecimal stockQuantity
) {}
