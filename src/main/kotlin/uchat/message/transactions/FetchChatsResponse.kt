package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

class FetchChatsResponse(
    @field:JsonProperty("chats") val chats: List<ChatResponse>
) : TransactionBase()