package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("owner_id") val ownerId: Long,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("timestamp") val timestamp: Long,
    @field:JsonProperty("message_type") val messageType: MessageType,
    @field:JsonProperty("buffer") var message: String,
    @field:JsonProperty("is_edited") var edited: Boolean = false
) : TransactionBase()
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageResponse

        if (messageId != other.messageId) return false
        if (ownerId != other.ownerId) return false
        if (chatId != other.chatId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messageId.hashCode()
        result = 31 * result + ownerId.hashCode()
        result = 31 * result + chatId.hashCode()
        return result
    }
}
