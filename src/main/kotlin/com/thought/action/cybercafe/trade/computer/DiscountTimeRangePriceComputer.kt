package com.thought.action.cybercafe.trade.computer

import com.thought.action.cybercafe.trade.DiscountPriceDefine
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class DiscountTimeRangePriceComputer(
    private val discountPriceDefine: DiscountPriceDefine
) : TimeRangePriceComputer {

    private val logger = LoggerFactory.getLogger(this::class.java);

    override fun startTime(): LocalTime {
        return discountPriceDefine.startTime
    }

    override fun range(
        startDatetime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Triple<Boolean, LocalDateTime, LocalDateTime> {

        if (endDateTime < startDatetime) {
            throw RuntimeException("结束时间不能再开始时间之前")
        }

        val dayOfWeek = startDatetime.dayOfWeek
        val startTime = startDatetime.toLocalTime()

        val contain = discountPriceDefine.days.any {
            it == dayOfWeek
        } && startTime >= discountPriceDefine.startTime && startTime <= discountPriceDefine.endTime
        if (contain) {
            if (endDateTime.dayOfYear > startDatetime.dayOfYear) {
                return Triple(
                    true,
                    startDatetime,
                    LocalDateTime.of(startDatetime.toLocalDate(), discountPriceDefine.endTime)
                )
            }
            val endTime = endDateTime.toLocalTime()
            if (endTime > discountPriceDefine.endTime) {
                return Triple(
                    true,
                    startDatetime,
                    LocalDateTime.of(startDatetime.toLocalDate(), discountPriceDefine.endTime)
                )
            }
            return Triple(true, startDatetime, endDateTime)
        }
        return Triple(false, startDatetime, endDateTime)
    }


    override fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal {

        if (startDateTime >= endDateTime) {
            return BigDecimal.ZERO
        }

        val minutes = Duration.between(startDateTime, endDateTime).toMinutes()
        val amount = discountPriceDefine.unitPrice.multiply(BigDecimal.valueOf(minutes))
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
        logger.info(
            "StartDateTime={} to EndDateTime={} discount time range price, minutes={}, amount = {}",
            startDateTime,
            endDateTime,
            minutes,
            amount
        )
        return amount
    }



}