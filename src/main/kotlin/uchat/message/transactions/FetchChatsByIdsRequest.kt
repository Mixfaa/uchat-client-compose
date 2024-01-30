package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class FetchChatsByIdsRequest(
    @field:JsonProperty("chats_ids") val chatsIds: List<Long>
) : TransactionBase()