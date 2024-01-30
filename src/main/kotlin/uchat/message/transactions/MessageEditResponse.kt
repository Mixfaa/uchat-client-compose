package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageEditResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("new_buffer") val newBuffer: String
) : TransactionBase()
