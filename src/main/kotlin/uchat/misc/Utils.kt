package uchat.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import uchat.message.transactions.SerializedTransaction
import uchat.message.transactions.TransactionBase

object Utils {
    fun splitJsons(string: String): List<String> {
        return string.split("\n").filter { it.isNotBlank() || it.isNotEmpty() }.toList()
    }

    val jsonMapper = ObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_INDEX).registerKotlinModule()

    fun serializeTransaction(transaction: TransactionBase): SerializedTransaction {
        return jsonMapper.writeValueAsBytes(transaction) + '\n'.code.toByte()
    }
}