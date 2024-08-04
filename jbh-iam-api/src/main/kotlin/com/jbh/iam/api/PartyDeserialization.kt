package com.jbh.iam.api

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@RestController
@RequestMapping("/party-deserialization")
class PartyDeserialization {

    val clock = Clock.fixed(Instant.parse("2019-02-05T16:45:42.01Z"), ZoneId.of("Australia/Sydney"))

    open class DateTimeDto(clock: Clock) {

        open val localDate: LocalDate
        open val localDateTime: LocalDateTime
        open val localTime: LocalTime

        init {
            localDate = LocalDate.now(clock)
            localDateTime = LocalDateTime.now(clock)
            localTime = LocalTime.now(clock)
        }
    }

    inner class FormattedDateTimeDto(clock: Clock) : DateTimeDto(clock) {

        override val localDate: LocalDate
            @JsonFormat(pattern = "dd-MM-yyyy")
            get() = super.localDate

        override val localDateTime: LocalDateTime
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
            get() = super.localDateTime

        val localDateTimeFormatted: LocalDateTime
            @JsonFormat(pattern = "yyyy-MM-dd")
            get() = super.localDateTime

        override val localTime: LocalTime
            @JsonFormat(pattern = "HH:mm")
            get() = super.localTime
    }

    // https://www.jworks.io/formatting-java-8-localdatetime-in-json-with-spring-boot/
    @GetMapping("/dates")
    fun getDatesFormat(): DateTimeDto {
        return DateTimeDto(clock)
    }

    @GetMapping("/dates-formatted")
    fun formattedTimeMapping(): DateTimeDto {
        return FormattedDateTimeDto(clock)
    }
}