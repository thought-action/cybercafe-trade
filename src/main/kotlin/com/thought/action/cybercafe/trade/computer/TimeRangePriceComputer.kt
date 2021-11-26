package com.thought.action.cybercafe.trade.computer

import java.time.LocalDateTime
import java.time.LocalTime

interface TimeRangePriceComputer : PriceComputer, Comparable<LocalTime> {

    fun startTime(): LocalTime

    /**
     * 验证时间是否在时间范围内，并返回范围内时间区间
     */
    fun range(
        startDatetime: LocalDateTime,
        endDateTime: LocalDateTime,
        overrideNight: Boolean
    ): Triple<Boolean, LocalDateTime, LocalDateTime>

}