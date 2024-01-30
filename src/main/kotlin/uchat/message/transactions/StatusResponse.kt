package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class StatusResponse(
    @field:JsonProperty("message") var message: String,
    @field:JsonProperty("response_for") var responseFor: TransactionType,
    @field:JsonProperty("is_failed") var fail: Boolean = true,
) : TransactionBase()