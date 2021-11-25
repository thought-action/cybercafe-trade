package com.thought.action.cybercafe.trade.computer

import com.thought.action.cybercafe.trade.DefaultPriceDefine
import com.thought.action.cybercafe.trade.DiscountPriceDefine
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

class DefaultPriceComputer(
    private val defaultPriceDefine: DefaultPriceDefine,
    private val discountPrices: List<DiscountPriceDefine>
) : PriceComputer {

    private val logger = LoggerFactory.getLogger(this.javaClass);

    override fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal {
        val minutes = Duration.between(startDateTime, endDateTime).toMinutes()
        val realEndDateTime = endDateTime.plusMinutes(59 - minutes % 60) //按小时计费，不满一小时按一小时计算，实际结束时间
        if (minutes % 60 > 0) {
            logger.info(
                "StarDateTime={} to EndDateTime={} contain insufficient part, real duration is {} hour",
                startDateTime,
                endDateTime,
                Duration.between(startDateTime, realEndDateTime).toHours()
            )
        }
        val defaultTimeRangeComputer = DefaultTimeRangeComputer(defaultPriceDefine)
        val discountTimeRangePriceComputers = discountPrices.map { DiscountTimeRangePriceComputer(it) }.toList()
        val nightDiscountTimeRangeComputer =
            NightDiscountTimeRangeComputer(
                defaultPriceDefine,
                defaultTimeRangeComputer,
                discountTimeRangePriceComputers
            )
        val discountPriceComputersAndNightPriceComputer =
            mutableListOf<TimeRangePriceComputer>(nightDiscountTimeRangeComputer)
        discountPriceComputersAndNightPriceComputer.addAll(discountTimeRangePriceComputers)

        val amounts = mutableListOf<BigDecimal>(defaultPriceDefine.powerOnPrice)
        if (defaultPriceDefine.powerOnPrice > BigDecimal.ZERO) {
            logger.info("power on price = {}", defaultPriceDefine.powerOnPrice)
        }

        val timeTotalAmount =
            compute(startDateTime, endDateTime, defaultTimeRangeComputer, discountPriceComputersAndNightPriceComputer)
        amounts.add(timeTotalAmount)
        logger.info("total use minutes = {}, amount = {}", minutes, timeTotalAmount)

        val totalAmount = amounts.reduce { totalAmount: BigDecimal, amount: BigDecimal -> totalAmount.add(amount) }
            .setScale(0, RoundingMode.UP)

        if (defaultPriceDefine.useMinimumConsumption) {
            if (defaultPriceDefine.minimumConsumption > totalAmount) {
                logger.info(
                    "Open use minimum consumption option, current total amount {} less minimum price {}, total amount change to {}",
                    totalAmount, defaultPriceDefine.minimumConsumption, defaultPriceDefine.minimumConsumption
                )
                return defaultPriceDefine.minimumConsumption
            }
        }


        return totalAmount
    }
}