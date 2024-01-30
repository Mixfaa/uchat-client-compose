package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageDeleteRequest(
    @field:JsonProperty("message_id") val messageId: Long,
) : TransactionBase()