import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import uchat.client.SocketChatImpl
import uchat.message.Account
import uchat.message.transactions.*
import uchat.misc.Utils
import uchat.misc.asPrivateKey
import uchat.misc.decodeB64
import uchat.misc.encodeB64
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import kotlin.io.encoding.ExperimentalEncodingApi

fun generateKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048)
    return keyPairGenerator.generateKeyPair()
}

private val defaultRoundedShape = RoundedCornerShape(15.dp)
private val socketChat = SocketChatImpl("192.168.0.59", 8080, ::println, {}) // а мне похуй (пока что)

private lateinit var privateKey: Key

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun loginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.background(Color.LightGray, defaultRoundedShape),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Username:")
            TextField(username, { username = it })
            Text("Password:")
            TextField(password, { password = it })

            Button({
                FileInputStream("private_key.pk").use {
                    privateKey = it.readBytes().decodeB64().asPrivateKey()
                    println("Loaded")
                    println(privateKey)
                }
            }) {
                Text("Load private key")
            }
            Button({
                socketChat.sendRequest(LoginRequest(username, password, null))
                FileInputStream("private_key.pk").use {
                    privateKey = it.readBytes().decodeB64().asPrivateKey()
                    println("Loaded")
                    println(privateKey)
                }
            }) {
                Text("Login")
            }
            Button({
                val keyPair = generateKeyPair()
                socketChat.sendRequest(
                    LoginRequest(
                        username,
                        password,
                        keyPair.public.encoded.encodeB64()
                    )
                )

                println("Publickey: ${String(keyPair.public.encoded.encodeB64())}")

                privateKey = keyPair.private
                FileOutputStream("private_key.pk").use {
                    it.write(keyPair.private.encoded.encodeB64())
                }
            }) {
                Text("Register")
            }
        }
    }
}

@Composable
fun chatSelectable(chat: ChatResponse, onClick: () -> Unit) {
    Button(onClick, Modifier.background(Color.LightGray, defaultRoundedShape).padding(10.dp).fillMaxWidth()) {
        Text(chat.name)
    }
}

@Composable
fun userAddComponent(user: Account, idsList: MutableList<Long>) {
    Row(Modifier.background(Color.LightGray, defaultRoundedShape).padding(10.dp).fillMaxWidth()) {
        Text(user.username)
        if (idsList.contains(user.id)) Icon(Icons.Filled.Delete, "Delete from chat", modifier = Modifier.clickable {
            idsList.remove(user.id)
        })
        else Icon(Icons.Filled.Add, "Add to chat", modifier = Modifier.clickable {
            idsList.add(user.id)
        })
    }
}

@Composable
fun mainScreen() {
    var currentChat by remember { mutableStateOf<ChatResponse?>(null) }
    val chats = mutableStateListOf<ChatResponse>().also {
        it.addAll(socketChat.chats)
    }

    val messages = mutableStateListOf<MessageResponse>().also {
        it.addAll(socketChat.messages)
    }

    val users = mutableStateListOf<Account>().also {
        it.addAll(socketChat.users)
    }

    socketChat.transactionCallback = { transaction ->
        when (transaction) {
            is ChatResponse, is FetchChatsResponse, is DeleteChatResponse -> {
                chats.clear()
                chats.addAll(socketChat.chats)
            }

            is MessageResponse, is MessageEditResponse, is MessageDeleteResponse, is FetchChatMessagesRequest -> {
                messages.clear()
                messages.addAll(socketChat.messages)
            }

            is FetchAccountsResponse -> {
                users.clear()
                users.addAll(socketChat.users)
            }

            else -> {}
        }
    }

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            Modifier.background(Color.DarkGray).fillMaxHeight().width(200.dp).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var createChatDropdown by remember { mutableStateOf(false) }
            val participantsIds = remember { mutableStateListOf<Long>() }
            Button({ createChatDropdown = !createChatDropdown }) {
                Text("Create new chat")
            }

            Window({ createChatDropdown = !createChatDropdown }, visible = createChatDropdown) {
                Column(
                    Modifier.padding(10.dp).fillMaxSize(),
                    Arrangement.spacedBy(10.dp),
                    Alignment.CenterHorizontally,
                ) {
                    Column(
                        Modifier.background(Color.DarkGray, defaultRoundedShape).padding(10.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        var chatName by remember { mutableStateOf("") }
                        TextField(chatName, { chatName = it })
                        Button({
                            createChatDropdown = false
                            socketChat.sendRequest(CreateChatRequest(chatName, participantsIds.toSet(), null, null))
                        }) {
                            Text("Create chat!")
                        }
                    }
                    Column(
                        Modifier.background(Color.DarkGray, defaultRoundedShape).padding(10.dp),
                        Arrangement.spacedBy(10.dp),
                        Alignment.CenterHorizontally,
                    ) {
                        var showParticipantsFetch by remember { mutableStateOf(false) }
                        Text("Add participants", modifier = Modifier.fillMaxWidth().clickable {
                            showParticipantsFetch = !showParticipantsFetch
                        })
                        AnimatedVisibility(showParticipantsFetch) {
                            Column(
                                Modifier.padding(10.dp), Arrangement.spacedBy(10.dp)
                            ) {
                                var participantName by remember { mutableStateOf("") }
                                TextField(participantName, {
                                    participantName = it
                                })
                                Button({
                                    socketChat.sendRequest(FetchAccountsRequest(participantName, 0, 10))
                                }) {
                                    Text("fetch")
                                }
                                users.asSequence().filter { it.username.contains(participantName, true) }
                                    .forEach { user ->
                                        userAddComponent(user, participantsIds)
                                    }
                            }
                        }
                    }
                }
            }
            Column(
                Modifier.padding(10.dp)
            ) {
                chats.forEach { chat ->
                    chatSelectable(chat) { currentChat = chat }
                }
            }
        }
        Column(
            Modifier.fillMaxSize().padding(10.dp), Arrangement.spacedBy(5.dp), Alignment.CenterHorizontally
        ) {
            val chat = currentChat

            if (chat == null) Text("No chat selected")
            else {
                Text(chat.name)
                Column(
                    Modifier.background(Color.LightGray).fillMaxWidth().fillMaxHeight(0.85f)
                ) {
                    messages.asSequence().filter { it.chatId == chat.chatId }.forEach { message ->
                        Row(Modifier.fillMaxWidth()) {
                            val owner = users.find { user -> user.id == message.ownerId }

                            val symmetric = Utils.decryptSymmetricKey(
                                chat.decryptionKeys.find { it.keyId == message.keyId }!!.encryptedSymmetric.decodeB64(),
                                privateKey
                            )

                            val decrypted = Utils.decryptMessage(message.message, symmetric)

                            Text("$decrypted ${owner?.username ?: ""}")
                            if (owner == socketChat.currentUser) Icon(Icons.Filled.Delete,
                                "Delete message",
                                Modifier.clickable {
                                    socketChat.sendRequest(MessageDeleteRequest(message.messageId))
                                })
                        }
                    }
                }
                Row(Modifier.fillMaxSize().background(Color.LightGray).padding(0.dp, 10.dp)) {
                    var textMessage by remember { mutableStateOf("") }
                    TextField(textMessage, { textMessage = it })
                    Button({
                        val usedSymmetric = chat.decryptionKeys.lastOrNull()!!

                        val symmetric = Utils.decryptSymmetricKey(
                            usedSymmetric.encryptedSymmetric.decodeB64(),
                            privateKey
                        )
                        val encrypted = Utils.encryptMessageWithSymmetric(textMessage, symmetric).encodeB64()
                        socketChat.sendRequest(
                            MessageRequest(
                                chat.chatId,
                                usedSymmetric.keyId,
                                MessageType.TEXT,
                                encrypted
                            )
                        )
                    }) {
                        Text("Send message")
                    }
                }
            }
        }
    }
}

fun main() {
    Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
    application {
        Window(onCloseRequest = ::exitApplication) {
            MaterialTheme(colors = lightColors()) {
                var isLoggedIn by remember { mutableStateOf(false) }
                socketChat.transactionCallback = {
                    if (it is LoginResponse) isLoggedIn = true
                }

                if (isLoggedIn) mainScreen()
                else loginScreen()
            }
        }
    }

}


