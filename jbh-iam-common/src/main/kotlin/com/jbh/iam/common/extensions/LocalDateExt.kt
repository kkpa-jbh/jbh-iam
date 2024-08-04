package com.jbh.iam.common.extensions

import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun LocalDate.convertToDateViaInstant(): Date {
    return Date.from(this.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
}
