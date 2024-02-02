package uchat.client

import kotlinx.coroutines.*
import uchat.message.transactions.TransactionBase
import uchat.message.transactions.deserializeTransaction
import uchat.misc.Utils
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.OutputStream
import java.net.InetAddress
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.*

abstract class SocketChatClient(
    address: String,
    port: Int,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : AutoCloseable {
    private val socket: SSLSocket
    private val outputStream: OutputStream
    private val inputReader: BufferedReader

    init {
        val keyStore = KeyStore.getInstance("JKS")
        keyStore.load(FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray())

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray())

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, SecureRandom())

        val sslSocketFactory = sslContext.socketFactory as SSLSocketFactory
        socket = sslSocketFactory.createSocket(InetAddress.getByName(address), port) as SSLSocket

        try {
            socket.startHandshake()
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
        outputStream = socket.outputStream
        inputReader = socket.inputStream.bufferedReader()
        socket.tcpNoDelay = true

        coroutineScope.launch {
            while (!socket.isClosed) {
                try {
                    val message = inputReader.readLine()

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
        outputStream.write(request.serialized)
    }

    override fun close() {
        coroutineScope.cancel()
        socket.close()
    }

    companion object {
        private const val KEYSTORE_PATH =
            "C:\\Users\\mishu\\programming_projects\\java_kotlin\\uchat-client-compose\\keystore\\uchat-keystore.jks"
        private const val KEYSTORE_PASSWORD = "semnadcat"
    }
}