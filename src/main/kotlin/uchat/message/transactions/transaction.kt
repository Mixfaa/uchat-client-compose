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
import java.security.MessageDigest
import kotlin.reflect.KClass

typealias SerializedTransaction = ByteArray
typealias EncryptedSymmetric = ByteArray
typealias EncryptedMessage = ByteArray
typealias PublicKey = ByteArray

enum class TransactionType(val transactionClass: KClass<*>) {
    STATUS_RESPONSE(StatusResponse::class),
    HEARTBEAT(Heartbeat::class),
    REQUEST_LOGIN(LoginRequest::class),
    REQUEST_REGISTER(RegisterRequest::class),
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
    REQUEST_ADD_MEMBER_TO_CHAT(ChatAddMemberRequest::class),

    RESPONSE_LOGIN(LoginResponse::class),
    RESPONSE_CHAT(ChatResponse::class),
    RESPONSE_DELETE_CHAT(DeleteChatResponse::class),
    RESPONSE_FETCH_ACCOUNTS(FetchAccountsResponse::class),
    RESPONSE_CHAT_MESSAGE(MessageResponse::class),
    RESPONSE_DELETE_MESSAGE(MessageDeleteResponse::class),
    RESPONSE_EDIT_MESSAGE(MessageEditResponse::class),
    RESPONSE_FETCH_CHATS(FetchChatsResponse::class),
    RESPONSE_FETCH_CHAT_MESSAGES(FetchChatMessagesResponse::class),
    RESPONSE_ADD_MEMBER_TO_CHAT(ChatAddMemberResponse::class);

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

fun createTransactionsMapper(): ObjectMapper =
    ObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_INDEX).registerKotlinModule()

private val transactionsMapper = createTransactionsMapper()

fun serializeTransaction(transaction: TransactionBase): SerializedTransaction {
    return transactionsMapper.writeValueAsBytes(transaction) + '\n'.code.toByte()
}

fun deserializeTransaction(json: String): Result<TransactionBase> = runCatching {
    transactionsMapper.readValue<TransactionBase>(json)
}

@OptIn(ExperimentalStdlibApi::class)
object TransactionUtils {
    val schema = buildString {
        for (type in TransactionType.entries) {
            this.append(type.name)
            this.append(' ')
            this.appendLine(transactionsMapper.generateJsonSchema(type.transactionClass.java).toString())
        }
    }
    val schemaMD5Hash = MessageDigest.getInstance("MD5").digest(schema.toByteArray()).toHexString()
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
    val type: TransactionType by lazy { TransactionType.fromTransactionClass(this) }

    @get:JsonIgnore
    val serialized: SerializedTransaction by lazy { serializeTransaction(this) }
}

data class ChatAddMemberRequest(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("member_id") val memberId: Long,
    @field:JsonProperty("decryption_keys") val decryptionKeys: List<MemberDecryptionKey>
) : TransactionBase()

data class ChatAddMemberResponse(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("member") val member: Account
) : TransactionBase()

data class ChatResponse(
    @field:JsonProperty("name") val name: String,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("owner_id") val ownerId: Long,
    @field:JsonProperty("members_ids") val membersIds: List<Long>,
    @field:JsonProperty("decryption_keys") val decryptionKeys: List<MemberDecryptionKey>,
) : TransactionBase()

data class MemberDecryptionKey(
    @field:JsonProperty("key_seq_id") val keyId: Long,
    @field:JsonProperty("member_id") val memberId: Long,
    @field:JsonProperty("encrypted_key") val encryptedSymmetric: EncryptedSymmetric
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemberDecryptionKey

        if (keyId != other.keyId) return false
        if (memberId != other.memberId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyId.hashCode()
        result = 31 * result + memberId.hashCode()
        return result
    }
}

data class CreateChatRequest(
    @field:JsonProperty("name") val chatName: String,
    @field:JsonProperty("members_ids") val membersIds: Set<Long>,
    @field:JsonProperty("encrypted_chat_symmetric") val encryptedChatSymmetric: EncryptedSymmetric?,
    @field:JsonProperty("members_decryption_keys") val membersDecryptionKeys: Set<MemberDecryptionKey>?
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
    @field:JsonProperty("chats_ids") val chatsIds: List<Long>,
) : TransactionBase()

data class RegisterRequest(
    @field:JsonProperty("username") val username: String,
    @field:JsonProperty("password") val password: String,
    @field:JsonProperty("public_key") val publicKey: PublicKey
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
    @field:JsonProperty("buffer") val buffer: EncryptedMessage,
) : TransactionBase()

data class MessageEditResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("new_buffer") val newBuffer: EncryptedMessage,
) : TransactionBase()

data class MessageRequest(
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("key_id") val keyId: Long,
    @field:JsonProperty("message_type") val messageType: MessageType,
    @field:JsonProperty("message_buffer") val buffer: EncryptedMessage,
) : TransactionBase()

data class MessageResponse(
    @field:JsonProperty("message_id") val messageId: Long,
    @field:JsonProperty("owner_id") val ownerId: Long,
    @field:JsonProperty("chat_id") val chatId: Long,
    @field:JsonProperty("timestamp") val timestamp: Long,
    @field:JsonProperty("message_type") val messageType: MessageType,
    @field:JsonProperty("buffer") val message: EncryptedMessage,
    @field:JsonProperty("key_id") val keyId: Long,
    @field:JsonProperty("is_edited") val edited: Boolean = false,
) : TransactionBase()

data class StatusResponse(
    @field:JsonProperty("message") val message: String,
    @field:JsonProperty("response_for") val responseFor: TransactionType,
) : TransactionBase()