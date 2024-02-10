package uchat.message

import uchat.message.transactions.PublicKey

data class Account(
    val username: String,
    val publicKey: PublicKey,
    var id: Long,
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