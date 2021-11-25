package com.thought.action.cybercafe.trade.computer

import java.time.LocalDateTime

interface TimeRangePriceComputer : PriceComputer {

    /**
     * 验证时间是否在时间范围内，并返回范围内时间区间
     */
    fun range(startDatetime: LocalDateTime, endDateTime: LocalDateTime): Triple<Boolean, LocalDateTime, LocalDateTime>

}