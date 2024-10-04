package com.mongmong.namo.presentation.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.*
import org.joda.time.LocalDateTime
import java.lang.reflect.Type

// LocalDateTime에 대한 커스텀 TypeAdapter 정의
class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString()) // LocalDateTime을 String으로 변환 (ISO8601 형식)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString) // 문자열을 LocalDateTime으로 변환
    }
}