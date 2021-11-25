package com.thought.action.cybercafe.trade

import java.math.BigDecimal
import java.time.LocalDateTime

interface PriceComputer {

    fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal

}