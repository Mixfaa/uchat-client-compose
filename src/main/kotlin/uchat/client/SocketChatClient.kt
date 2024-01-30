package uchat.client

import kotlinx.coroutines.*
import uchat.message.transactions.TransactionBase
import uchat.message.transactions.deserializeTransaction
import uchat.misc.Utils
import java.net.InetAddress
import java.net.Socket

abstract class SocketChatClient(
    address: String,
    port: Int,
    protected val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : AutoCloseable {
    private val socket = Socket(InetAddress.getByName(address), port)

    init {
        coroutineScope.launch {
            val inputStream = socket.getInputStream()
            val messageBuilder = StringBuilder()
            while (!socket.isClosed) {
                try {
                    var lastDelimiterIndex: Int
                    do {
                        var availableToRead = 0
                        while (availableToRead <= 0) {
                            availableToRead = inputStream.available()
                            delay(50)
                        }

                        messageBuilder.append(String(inputStream.readNBytes(availableToRead)))
                        lastDelimiterIndex = messageBuilder.lastIndexOf('\n')
                    } while (lastDelimiterIndex == -1)

                    val message = messageBuilder.substring(0, lastDelimiterIndex + 1)
                    val cleared = messageBuilder.removeRange(0, lastDelimiterIndex + 1)
                    messageBuilder.clear()
                    messageBuilder.append(cleared)

                    for (json in Utils.splitJsons(message)) {
                        val transaction = deserializeTransaction(json).getOrNull()
                        if (transaction != null)
                            handleTransaction(transaction)
                    }

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    close()
                }
            }
        }
    }

    abstract fun handleTransaction(transaction: TransactionBase)

    fun sendRequest(request: TransactionBase) = coroutineScope.launch {
        socket.getOutputStream().write(request.serialized)
    }

    override fun close() {
        coroutineScope.cancel()
        socket.close()
    }
}