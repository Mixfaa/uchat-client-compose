package uchat.message

import uchat.message.transactions.B64PublicKey

data class Account(
    val username: String,
    var id: Long,
    val publicKey: B64PublicKey,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}