package com.thought.action.cybercafe.trade.computer

import com.thought.action.cybercafe.trade.DefaultPriceDefine
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class NightDiscountTimeRangeComputer(
    private val defaultPriceDefine: DefaultPriceDefine,
    private val defaultTimeRangeComputer: DefaultTimeRangeComputer,
    private val discountTimePriceComputers: List<DiscountTimeRangePriceComputer>
) : TimeRangePriceComputer {

    private val logger = LoggerFactory.getLogger(this::class.java);
    override fun startTime(): LocalTime {
        return defaultPriceDefine.nightStartTime
    }

    override fun range(
        startDatetime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Triple<Boolean, LocalDateTime, LocalDateTime> {

        if (endDateTime < startDatetime) {
            throw RuntimeException("结束时间不能再开始时间之前")
        }

        val startTime = startDatetime.toLocalTime()
        val nightStartTime = defaultPriceDefine.nightStartTime
        val nightEndTime = defaultPriceDefine.nightEndTime
        val isAcrossTheDay = nightEndTime < nightStartTime //夜间价格是否跨天
        if (startTime >= nightStartTime) {
            if (endDateTime.dayOfYear > startDatetime.dayOfYear) {
                if (isAcrossTheDay) {
                    val nightEndDateTime = LocalDateTime.of(startDatetime.toLocalDate().plusDays(1), nightEndTime)
                    if (nightEndDateTime > endDateTime) {
                        return Triple(true, startDatetime, endDateTime)
                    }
                    return Triple(true, startDatetime, nightEndDateTime)
                } else {
                    return Triple(true, startDatetime, LocalDateTime.of(startDatetime.toLocalDate(), nightEndTime))
                }
            } else {
                if (isAcrossTheDay) {
                    return Triple(true, startDatetime, endDateTime)
                } else {
                    val endTime = endDateTime.toLocalTime()
                    if (endTime > nightEndTime) {
                        return Triple(true, startDatetime, LocalDateTime.of(endDateTime.toLocalDate(), nightEndTime))
                    }
                    return Triple(true, startDatetime, endDateTime)
                }
            }
        }
        return Triple(false, endDateTime, endDateTime)
    }

    override fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal {
        if (startDateTime >= endDateTime) {
            return BigDecimal.ZERO
        }

        val totalAmount = compute(startDateTime, endDateTime, defaultTimeRangeComputer, discountTimePriceComputers)

        val minutes = Duration.between(startDateTime, endDateTime).toMinutes()
        if (totalAmount > defaultPriceDefine.nightPrice) {
            logger.info(
                "StartDateTime={} to EndDateTime={} use night discount time range price, minutes={}, total amount={}，night amount = {} ",
                startDateTime,
                endDateTime,
                minutes,
                totalAmount,
                defaultPriceDefine.nightPrice
            )
            return defaultPriceDefine.nightPrice
        }
        logger.info(
            "StartDateTime={} to EndDateTime={} use night discount time range price, minutes={}, total amount={}",
            startDateTime,
            endDateTime,
            minutes,
            totalAmount
        )
        return totalAmount
    }

}