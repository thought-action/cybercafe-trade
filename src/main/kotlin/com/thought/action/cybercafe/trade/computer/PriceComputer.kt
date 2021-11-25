package com.thought.action.cybercafe.trade.computer

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

interface PriceComputer {

    fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal

    fun compute(
        startDateTime: LocalDateTime, endDateTime: LocalDateTime, defaultTimePriceComputer: PriceComputer,
        discountTimeRangePriceComputers: List<TimeRangePriceComputer>
    ): BigDecimal {
        val amounts = mutableListOf<BigDecimal>(BigDecimal.ZERO)
        var rangeTime = startDateTime
        var defaultStarTime = rangeTime
        while (rangeTime < endDateTime) {
            for (discountPriceComputer in discountTimeRangePriceComputers) {
                val (isContain, priceStartDateTime, priceEndDateTime) = discountPriceComputer.range(
                    rangeTime,
                    endDateTime
                )
                isContain.and(isContain)
                if (isContain) {
                    amounts.add(defaultTimePriceComputer.compute(defaultStarTime, rangeTime))
                    amounts.add(discountPriceComputer.compute(priceStartDateTime, priceEndDateTime))
                    defaultStarTime = priceEndDateTime
                    rangeTime = priceEndDateTime
                }
            }
            rangeTime = rangeTime.plusMinutes(1) //加一方式需优化，直接筛选最近优惠时间
        }
        amounts.add(defaultTimePriceComputer.compute(defaultStarTime, rangeTime.minusMinutes(1)))
        return amounts.reduce { totalAmount: BigDecimal, amount: BigDecimal -> totalAmount.add(amount) }
    }
}