package uchat.message.transactions

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import uchat.message.Account
import uchat.message.TransactionTypeIdResolver
import kotlin.reflect.KClass

typealias SerializedTransaction = ByteArray

enum class TransactionType(val transactionClass: KClass<*>) {
    STATUS_RESPONSE(StatusResponse::class),
    HEARTBEAT(Heartbeat::class),
    REQUEST_LOGIN(LoginRequest::class),
    REQUEST_MESSAGE(MessageRequest::class),
    REQUEST_CREATE_CHAT(CreateChatRequest::class),
    REQUEST_FETCH_ACCOUNTS(FetchAccountsRequest::class),
    REQUEST_DELETE_MESSAGE(MessageDeleteRequest::class),
    REQUEST_EDIT_MESSAGE(MessageEditRequest::class),
    REQUEST_FETCH_CHATS(FetchChatsRequest::class),
    REQUEST_FETCH_CHAT_MESSAGES(FetchChatMessagesRequest::class),
    REQUEST_DELETE_CHAT(DeleteChatRequest::class),
    REQUEST_FETCH_ACCOUNTS_BY_IDS(FetchAccountsByIdsRequest::class),
    REQUEST_FETCH_CHATS_BY_IDS(FetchChatsByIdsRequest::class),

    RESPONSE_LOGIN(LoginResponse::class),
    RESPONSE_CHAT(ChatResponse::class),
    RESPONSE_DELETE_CHAT(DeleteChatResponse::class),
    RESPONSE_FETCH_ACCOUNTS(FetchAccountsResponse::class),
    RESPONSE_CHAT_MESSAGE(MessageResponse::class),
    RESPONSE_DELETE_MESSAGE(MessageDeleteResponse::class),
    RESPONSE_EDIT_MESSAGE(MessageEditResponse::class),
    RESPONSE_FETCH_CHATS(FetchChatsResponse::class),
    RESPONSE_FETCH_CHAT_MESSAGES(FetchChatMessagesResponse::class);

    companion object {
        fun fromTransactionClass(type: TransactionBase): TransactionType {
            return entries.first { it.transactionClass == type::class }
        }

        fun forValue(value: String): TransactionType {
            val ordinal = value.toIntOrNull()
            return if (ordinal != null)
                entries[ordinal]
            else
                valueOf(value.uppercase())
        }
    }
}

private val jsonMapper = ObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_INDEX).registerKotlinModule()

fun serializeTransaction(transaction: TransactionBase): SerializedTransaction {
    return jsonMapper.writeValueAsBytes(transaction) + '\n'.code.toByte()
}

fun deserializeTransaction(json: String): Result<TransactionBase> = runCatching {
    jsonMapper.readValue<TransactionBase>(json)
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CUSTOM,
    include = JsonTypeInfo.As.PROPERTY,
    visible = false, // important
    property = "transaction_type"
)
@JsonTypeIdResolver(TransactionTypeIdResolver::class)
sealed class TransactionBase {
    @get:JsonIgnore
    val type: TransactionType by lazy { TransactionType.fromTransactionClass(this@TransactionBase) }

    @get:JsonIgnore
    val serialized: ByteArray by lazy { serializeTransaction(this@TransactionBase) }
}


data class ChatResponse(
    @field:JsonProperty("name") val name: String,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("owner_id") val ownerId: Long,
    @field:JsonProperty("participants_ids") val participants: List<Long>,
) : TransactionBase()
data class CreateChatRequest(
    @field:JsonProperty("name") val chatName: String,
    @field:JsonProperty("participants_ids") val participantsIds: MutableList<Long>?
) : TransactionBase()

data class DeleteChatRequest(
    @field:JsonProperty("chat_id") val chatId: Long
) : TransactionBase()

data class DeleteChatResponse(
    @field:JsonProperty("chat_id") val chatId: Long
) : TransactionBase()

data class FetchAccountsByIdsRequest(
    @field:JsonProperty("accounts_ids") val ids: List<Long>
) : TransactionBase()

data class FetchAccountsRequest(
    @field:JsonProperty("query") val query: String?,
    @field:JsonProperty("page") val page: Int,
    @field:JsonProperty("limit") val limit: Int
) : TransactionBase()

data class FetchAccountsResponse(
    @field: JsonProperty("accounts") val accounts: List<Account>,
    @field:JsonProperty("query") val query: String?
) : TransactionBase()

data class FetchChatMessagesRequest(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("page") val page: Int,
    @field:JsonProperty("limit") val limit: Int
) : TransactionBase()

data class FetchChatMessagesResponse(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("messages") val messages: List<MessageResponse>
) : TransactionBase()

data class FetchChatsByIdsRequest(
    @field:JsonProperty("chats_ids") val chatsIds: List<Long>
) : TransactionBase()

data class FetchChatsRequest(
    @field:JsonProperty("page") val page: Int,
    @field:JsonProperty("limit") val limit: Int
) : TransactionBase()


data class FetchChatsResponse(
    @field:JsonProperty("chats") val chats: List<ChatResponse>
) : TransactionBase()

data object Heartbeat : TransactionBase()

data class LoginRequest(
    @field:JsonProperty("username") val username: String,
    @field:JsonProperty("password") val password: String
) : TransactionBase()


data class LoginResponse(
    @field:JsonProperty("user") val user: Account,
    @field:JsonProperty("chats_ids") val chatsIds: List<Long>
) : TransactionBase()


data class MessageDeleteRequest(
    @field:JsonProperty("message_id") val messageId: Long,
) : TransactionBase()


data class MessageDeleteResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("chat_id") val chatId: Long
) : TransactionBase()

data class MessageEditRequest(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("buffer") val buffer: String
) : TransactionBase()

data class MessageEditResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("new_buffer") val newBuffer: String
) : TransactionBase()
data class MessageRequest(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("message_type") val messageType: MessageType,
    @field:JsonProperty("message_buffer") val buffer: String
) : TransactionBase()

data class MessageResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("owner_id") val ownerId: Long,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("timestamp") val timestamp: Long,
    @field:JsonProperty("message_type") val messageType: MessageType,
    @field:JsonProperty("buffer") var message: String,
    @field:JsonProperty("is_edited") var edited: Boolean = false
) : TransactionBase()

data class StatusResponse(
    @field:JsonProperty("message") var message: String,
    @field:JsonProperty("response_for") var responseFor: TransactionType,
    @field:JsonProperty("is_failed") var fail: Boolean = true,
) : TransactionBase()