package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class FetchAccountsResponse(
    @field: JsonProperty("accounts") val accounts: List<uchat.message.Account>,
    @field:JsonProperty("query") val query: String?
) : TransactionBase()