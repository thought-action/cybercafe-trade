package com.thought.action.cybercafe.trade.computer

import com.thought.action.cybercafe.trade.DefaultPriceDefine
import com.thought.action.cybercafe.trade.DiscountPriceDefine
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

internal class DefaultPriceComputerTest {

    @Test
    fun compute() {
        val defaultPrice = DefaultPriceDefine(
            BigDecimal.valueOf(15),
            BigDecimal.valueOf(25),
            LocalTime.of(22, 0),
            LocalTime.of(6, 0),
            BigDecimal.valueOf(50), true,
            BigDecimal.valueOf
                (5)
        )
        val firstDiscountPriceDefine =
            DiscountPriceDefine("早晨优惠", DayOfWeek.values().toList(), LocalTime.of(7, 0), LocalTime.of(12, 0), BigDecimal.valueOf(6))

        val secondDiscountPriceDefine =
            DiscountPriceDefine("下午优惠", DayOfWeek.values().toList(), LocalTime.of(12, 0), LocalTime.of(18, 0), BigDecimal.valueOf(8))

        val thirdDiscountPriceDefine =
            DiscountPriceDefine("晚间优惠", DayOfWeek.values().toList(), LocalTime.of(20, 0), LocalTime.of(23, 0), BigDecimal.valueOf(8))

        val defaultPriceComputer = DefaultPriceComputer(defaultPrice, listOf(firstDiscountPriceDefine, secondDiscountPriceDefine, thirdDiscountPriceDefine))

        val startDateTime = LocalDateTime.of(2021, 12, 12, 10, 1)
        val endDateTime = LocalDateTime.of(2021, 12, 13, 11, 1)

        Assertions.assertEquals(BigDecimal.ZERO, defaultPriceComputer.compute(startDateTime, endDateTime))
    }
}