package uchat.client

import uchat.message.Account
import uchat.message.transactions.*

class SocketChatImpl(
    address: String,
    port: Int,
    var errorCallback: (String) -> Unit,
    var transactionCallback: (TransactionBase) -> Unit,
) : SocketChatClient(address, port) {
    private var isLoggedIn = false

    val chats = mutableSetOf<ChatResponse>()//CopyOnWriteArraySet<ChatResponse>()
    val users = mutableSetOf<Account>()//CopyOnWriteArraySet<Account>()
    val messages = mutableSetOf<MessageResponse>()//CopyOnWriteArraySet<MessageResponse>()
    var currentUser: Account? = null
        private set

    override fun handleTransaction(transaction: TransactionBase) {
        if (!isLoggedIn) // we wait for LoginResponse
        {
            if (transaction is LoginResponse) {
                isLoggedIn = true
                currentUser = transaction.user

                if (transaction.chatsIds.isNotEmpty())
                    sendRequest(FetchChatsByIdsRequest(transaction.chatsIds))
            } else if (transaction is StatusResponse)
                errorCallback(transaction.message)
        }

        when (transaction) {
            is ChatResponse -> handleNewChats(listOf(transaction))
            is DeleteChatResponse -> chats.removeIf { it.chatId == transaction.chatId }
            is FetchAccountsResponse -> users.addAll(transaction.accounts)
            is FetchChatMessagesResponse -> handleNewMessages(transaction.messages)
            is FetchChatsResponse -> handleNewChats(transaction.chats)
            is MessageDeleteResponse -> messages.removeIf { it.messageId == transaction.messageId && it.chatId == transaction.chatId }
            is MessageEditResponse -> {
                val message = messages.find { it.messageId == transaction.messageId && it.chatId == transaction.chatId }
                message?.message = transaction.newBuffer
                message?.edited = true
            }

            is MessageResponse -> handleNewMessages(listOf(transaction))
            is StatusResponse -> errorCallback(transaction.message)
            else -> {}// not needed to handler requests :)
        }
        transactionCallback(transaction)
    }

    private fun handleNewMessages(messages: Iterable<MessageResponse>) {
        this.messages.addAll(messages)

        val usersToFetch = buildList {
            for (message in messages) {
                if (users.none { it.id == message.ownerId })
                    add(message.ownerId)
            }
        }

        sendRequest(FetchAccountsByIdsRequest(usersToFetch))
    }

    private fun handleNewChats(chats: Iterable<ChatResponse>) {
        this.chats.addAll(chats)

        for (chat in chats)
            sendRequest(FetchChatMessagesRequest(chat.chatId, 0, 15))
    }

}