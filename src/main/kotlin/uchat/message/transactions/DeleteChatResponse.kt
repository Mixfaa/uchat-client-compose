package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class DeleteChatResponse(
    @field:JsonProperty("chat_id") val chatId: Long
) : TransactionBase()