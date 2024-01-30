package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

class FetchChatsRequest(
    @field:JsonProperty("page") val page: Int,
    @field:JsonProperty("limit") val limit: Int
) : TransactionBase()