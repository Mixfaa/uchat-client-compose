package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class DeleteChatRequest(
    @field:JsonProperty("chat_id") val chatId: Long
) : TransactionBase()