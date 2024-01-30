package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

class FetchChatMessagesResponse(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("messages") val messages: List<MessageResponse>
) : TransactionBase()