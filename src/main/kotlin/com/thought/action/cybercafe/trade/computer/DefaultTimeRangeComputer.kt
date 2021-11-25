package com.thought.action.cybercafe.trade.computer

import com.thought.action.cybercafe.trade.DefaultPriceDefine
import com.thought.action.cybercafe.trade.PriceComputer
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime

class DefaultTimeRangeComputer(private val defaultPriceDefine: DefaultPriceDefine) : PriceComputer {

    private val logger = LoggerFactory.getLogger(this::class.java);

    override fun compute(startDateTime: LocalDateTime, endDateTime: LocalDateTime): BigDecimal {
        if (startDateTime >= endDateTime) {
            return BigDecimal.ZERO
        }
        val minutes = Duration.between(startDateTime, endDateTime).toMinutes()
        val amount = defaultPriceDefine.unitPrice.multiply(BigDecimal.valueOf(minutes))
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
        logger.info(
            "StartDateTime={} to EndDateTime={} use default time range price, minutes={}, amount = {}",
            startDateTime,
            endDateTime,
            minutes,
            amount
        )
        return amount
    }
}