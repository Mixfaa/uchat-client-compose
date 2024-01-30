package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(
    @field:JsonProperty("user") val user: uchat.message.Account,
    @field:JsonProperty("chats_ids") val chatsIds: List<Long>
) : TransactionBase()