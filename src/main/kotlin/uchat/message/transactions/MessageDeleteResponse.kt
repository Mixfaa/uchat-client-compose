package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageDeleteResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("chat_id") val chatId: Long
) : TransactionBase()