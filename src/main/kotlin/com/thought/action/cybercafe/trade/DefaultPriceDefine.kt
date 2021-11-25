package com.thought.action.cybercafe.trade

import java.math.BigDecimal
import java.time.LocalTime

data class DefaultPriceDefine(
    val unitPrice: BigDecimal= BigDecimal.TEN,
    val nightPrice: BigDecimal,
    val nightStartTime: LocalTime,
    val nightEndTime: LocalTime,
    val minimumConsumption: BigDecimal = BigDecimal.ZERO,
    val useMinimumConsumption: Boolean = false,
    val powerOnPrice: BigDecimal = BigDecimal.ZERO
)
