package com.supplymanager.domain.dto;

import java.math.BigDecimal;

public record ProducibleProductDTO(
    Long productId,
    String productCode,
    String productName,
    BigDecimal productValue,
    int producibleQuantity,
    BigDecimal totalValue
) {}
