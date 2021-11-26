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

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal {
        val minutes = Duration.between(startDateTime, endDateTime).toMinutes()
        val realEndDateTime = if (minutes % 60 > 0) {
            val realEndDateTime = endDateTime.plusMinutes(60 - minutes % 60)
            logger.info(
                "上机时间 {} - {} 包含不足小时部分，按 {} 分钟计算，",
                startDateTime,
                endDateTime,
                Duration.between(startDateTime, realEndDateTime).toHours()
            )
            realEndDateTime
        } else {
            logger.info(
                "上机时间 {} - {} ， 总上机时常 : {} 分钟",
                startDateTime,
                endDateTime,
                Duration.between(startDateTime, endDateTime).toMinutes()
            )
            endDateTime
        }
        val defaultTimeRangeComputer = DefaultTimeRangeComputer(defaultPriceDefine)
        val discountTimeRangePriceComputers =
            discountPrices.map { DiscountTimeRangePriceComputer(defaultPriceDefine, it) }.toSet()
        val nightDiscountTimeRangeComputer =
            NightDiscountTimeRangeComputer(
                defaultPriceDefine,
                defaultTimeRangeComputer,
                discountTimeRangePriceComputers
            )
        val discountPriceComputersAndNightPriceComputer =
            mutableSetOf<TimeRangePriceComputer>(nightDiscountTimeRangeComputer)
        discountPriceComputersAndNightPriceComputer.addAll(discountTimeRangePriceComputers)

        val amounts = mutableListOf(defaultPriceDefine.powerOnPrice)
        if (defaultPriceDefine.powerOnPrice > BigDecimal.ZERO) {
            logger.info("开机费用 : {}", defaultPriceDefine.powerOnPrice)
        }

        val timeTotalAmount =
            compute(
                startDateTime,
                realEndDateTime,
                defaultTimeRangeComputer,
                discountPriceComputersAndNightPriceComputer,
                false
            )
        amounts.add(timeTotalAmount)
        logger.info("总上机时长费用 : {}", timeTotalAmount)

        val totalAmount = amounts.reduce { totalAmount: BigDecimal, amount: BigDecimal -> totalAmount.add(amount) }
            .setScale(0, RoundingMode.UP)

        if (defaultPriceDefine.useMinimumConsumption) {
            if (defaultPriceDefine.minimumConsumption > totalAmount) {
                logger.info(
                    "开启最低消费模式，当前消费: {},  不足最低消费: {}, 按最低消费计算总价",
                    totalAmount, defaultPriceDefine.minimumConsumption
                )
                return defaultPriceDefine.minimumConsumption
            }
        }

        logger.info("总消费 = {}", timeTotalAmount)

        return totalAmount
    }
}