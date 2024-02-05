package uchat.misc

import uchat.message.transactions.B64EncryptedPrivateKey
import uchat.message.transactions.B64EncryptedSymmetric
import uchat.message.transactions.B64PublicKey
import java.security.Key
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
object Utils {
    private const val DEFAULT_CIPHER_ALGO = "RSA/ECB/PKCS1Padding"

    fun splitJsons(string: String): List<String> {
        return string.split("\n").filter { it.isNotBlank() || it.isNotEmpty() }.toList()
    }

    fun encrypt(data: ByteArray, key: Key, transformation: String): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray, key: Key, transformation: String): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(data)
    }

    fun decryptSymmetic(data: ByteArray, key: Key, transformation: String, keyAlgo: String): SecretKey {
        return SecretKeySpec(decrypt(data, key, transformation), keyAlgo)
    }

    fun encryptMessageWithSymmetric(message: String, symmetric: Key): ByteArray {
        return encrypt(message.toByteArray(), symmetric, "AES")
    }

    fun decryptSymmetricKey(
        encryptedSymmetric: ByteArray,
        encryptedKey: ByteArray,
        key: Key
    ): Key {
        val encryptedPrivateKey = decrypt(encryptedKey, key, DEFAULT_CIPHER_ALGO).decodeB64().asPrivateKey()
        return decryptSymmetic(encryptedSymmetric, encryptedPrivateKey, DEFAULT_CIPHER_ALGO, "AES")
    }

    fun decryptMessage(
        data: ByteArray,
        symmetric: Key
    ): String {
        val decrypted = decrypt(data.decodeB64(), symmetric, "AES")
        return String(decrypted)
    }
}

fun ByteArray.asPublicKey(): Key {
    return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(this))
}

fun ByteArray.asPrivateKey(): Key {
    return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(this))
}

fun ByteArray.asSymmetric() : SecretKey {
    return SecretKeySpec(this, "AES")
}

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.encodeB64(): ByteArray {
    return Base64.encodeToByteArray(this)
}

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.decodeB64(): ByteArray {
    return Base64.decode(this)
}