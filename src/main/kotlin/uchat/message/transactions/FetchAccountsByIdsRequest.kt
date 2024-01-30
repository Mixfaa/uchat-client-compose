package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class FetchAccountsByIdsRequest(
    @field:JsonProperty("accounts_ids") val ids: Iterable<Long>
) : TransactionBase()