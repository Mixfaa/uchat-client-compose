package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatResponse(
    @field:JsonProperty("name") val name: String,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("owner_id") val ownerId: Long,
    @field:JsonProperty("participants_ids") val participants: Iterable<Long>,
) : TransactionBase()
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatResponse

        return chatId == other.chatId
    }

    override fun hashCode(): Int {
        return chatId.hashCode()
    }
}

