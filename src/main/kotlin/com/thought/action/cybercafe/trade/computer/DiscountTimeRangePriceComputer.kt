package com.thought.action.cybercafe.trade.computer

import com.thought.action.cybercafe.trade.DefaultPriceDefine
import com.thought.action.cybercafe.trade.DiscountPriceDefine
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class DiscountTimeRangePriceComputer(
    private val defaultPriceDefine: DefaultPriceDefine,
    private val discountPriceDefine: DiscountPriceDefine
) : TimeRangePriceComputer {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun startTime(): LocalTime {
        return discountPriceDefine.startTime
    }

    override fun range(
        startDatetime: LocalDateTime,
        endDateTime: LocalDateTime,
        overrideNight: Boolean
    ): Triple<Boolean, LocalDateTime, LocalDateTime> {

        if (endDateTime < startDatetime) {
            throw RuntimeException("结束时间不能再开始时间之前")
        }

        val dayOfWeek = startDatetime.dayOfWeek
        val startTime = startDatetime.toLocalTime()

        // 优惠结束时间如果再夜间计费之后，则此轮优惠时间结束时间取夜间计费开始时间, 夜间计费再重新计算总消费额度
        val discountPriceEndTime =
            if (discountPriceDefine.endTime > defaultPriceDefine.nightStartTime && !overrideNight) {
                defaultPriceDefine.nightStartTime
            } else {
                discountPriceDefine.endTime
            }
        val contain = discountPriceDefine.days.any {
            it == dayOfWeek
        } && startTime >= discountPriceDefine.startTime && startTime <= discountPriceEndTime
        if (contain) {
            if (endDateTime.dayOfYear > startDatetime.dayOfYear) {
                return Triple(
                    true,
                    startDatetime,
                    LocalDateTime.of(startDatetime.toLocalDate(), discountPriceEndTime)
                )
            }
            val endTime = endDateTime.toLocalTime()
            if (endTime > discountPriceEndTime) {
                return Triple(
                    true,
                    startDatetime,
                    LocalDateTime.of(startDatetime.toLocalDate(), discountPriceEndTime)
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
            "计费模式[{}] {} - {}, 时常 : {}, 费用 : {}",
            discountPriceDefine.name,
            startDateTime,
            endDateTime,
            minutes,
            amount
        )
        return amount
    }

    override fun compareTo(other: LocalTime): Int {
        return discountPriceDefine.startTime.compareTo(other)
    }


}