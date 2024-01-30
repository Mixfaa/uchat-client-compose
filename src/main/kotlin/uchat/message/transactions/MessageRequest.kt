package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageRequest(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("message_type") val messageType: MessageType,
    @field:JsonProperty("message_buffer") val buffer: String
) : TransactionBase()

