package com.hashmimotors.app.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Room TypeConverters for complex types (lists, enums).
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // String list
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) "" else json.encodeToString(ListSerializer(String.serializer()), value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(String.serializer()), value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // InvoiceLineEmbedded list
    @TypeConverter
    fun fromInvoiceLineList(value: List<InvoiceLineEmbedded>?): String {
        return if (value == null) "" else json.encodeToString(ListSerializer(InvoiceLineEmbedded.serializer()), value)
    }

    @TypeConverter
    fun toInvoiceLineList(value: String?): List<InvoiceLineEmbedded> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(InvoiceLineEmbedded.serializer()), value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Generic JSON string
    @TypeConverter
    fun fromJsonString(value: String?): String = value ?: ""

    @TypeConverter
    fun toJsonString(value: String?): String = value ?: ""
}
