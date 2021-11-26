package com.thought.action.cybercafe.trade.computer

import java.math.BigDecimal
import java.time.LocalDateTime

interface PriceComputer {

    fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal

    fun compute(
        startDateTime: LocalDateTime, endDateTime: LocalDateTime, defaultTimePriceComputer: PriceComputer,
        discountTimeRangePriceComputers: Collection<TimeRangePriceComputer>,
        overrideNight: Boolean,
    ): BigDecimal {
        if (endDateTime <= startDateTime) {
            return BigDecimal.ZERO
        }
        if (discountTimeRangePriceComputers.isEmpty()) {
            return defaultTimePriceComputer.compute(startDateTime, endDateTime)
        }
        val amounts = mutableListOf<BigDecimal>(BigDecimal.ZERO)
        var defaultTimePriceRangeStartTime = startDateTime
        var priceStartTime = startDateTime
        while (priceStartTime < endDateTime) {
             discountTimeRangePriceComputers.any {
                val (isContain, priceStartDateTime, priceEndDateTime) = it.range(
                    priceStartTime,
                    endDateTime,
                    overrideNight
                )
                if (isContain) {
                    if (priceStartTime > defaultTimePriceRangeStartTime.plusMinutes(1)) {
                        amounts.add(defaultTimePriceComputer.compute(defaultTimePriceRangeStartTime, priceStartDateTime))
                    }
                    amounts.add(it.compute(priceStartDateTime, priceEndDateTime))
                    priceStartTime = priceEndDateTime
                    defaultTimePriceRangeStartTime = priceEndDateTime
                }
                 isContain
            }
            priceStartTime = priceStartTime.plusMinutes(1) //加一方式需优化，直接筛选最近优惠时间
        }
        amounts.add(defaultTimePriceComputer.compute(defaultTimePriceRangeStartTime, priceStartTime.minusMinutes(1)))
        return amounts.reduce { totalAmount: BigDecimal, amount: BigDecimal -> totalAmount.add(amount) }
    }
}