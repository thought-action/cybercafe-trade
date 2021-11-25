package com.thought.action.cybercafe.trade

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

data class DiscountPriceDefine(
    val name: String,
    val days: List<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val unitPrice: BigDecimal
)
